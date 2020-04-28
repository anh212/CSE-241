import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class AccountInterface {
    public static void Interface(Customer customer, Connection conn) {
        while (true) {
            //Prompt user to Main Menu
            System.out.println("Customer Options");
            System.out.println("Please enter the number corresponding to your action");
            System.out.println("[1] Make a deposit");
            System.out.println("[2] Make a withdrawal");
            System.out.println("[3] Make a purchase");
            System.out.println("[4] Open a new bank account");
            System.out.println("[5] Exit");

            String input = Input.getString();

            //Go to interface that user selects
            switch(input) {
                case "1":
                    DepositWithdrawalInterface.Deposit(customer, conn);
                    break;
                case "2":
                    DepositWithdrawalInterface.Withdrawal(customer, conn);
                    break;
                case "3":
                    MakePurchase(customer.getId(), conn);
                    break;
                case "4":
                    NewBankAccountInterface.Interface(customer, conn);
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid command");
            }
        }
    }

    private static int MakePurchase(int customerID, Connection conn) {
        int rowsUpdated = 0;

        System.out.println("Please choose method of payment. Please type \"debit\" or \"credit\"");

        //Ask for credit or debit
        String debitOrCredit = null;

        while(true) {
            debitOrCredit = Input.getString();

            if(debitOrCredit.equals("debit") || debitOrCredit.equals("credit")) break;
            System.out.println("Invalid option: Please try again");
        }

        System.out.println("Here are your available cards");
        List<Integer> cardIDs = null;
        if (debitOrCredit.equals("debit")) {
            cardIDs = printCards(customerID, "debit", conn);
            if (cardIDs.isEmpty()) return 0;

            //Ask to choose which card they would like to use
            System.out.println("Please choose the card you would like to make a purchase with");
            int cardID;

            while(true) {
                cardID = Input.getInt();

                if(cardIDs.contains(cardID)) break;
                System.out.println("Invalid option: Please try again");
            }

            System.out.println("Please enter the cost of the purchase");
            Double purchaseAmount = Input.getDouble();

            //If there are sufficient funds then continue on with withdrawal
            if(!validateDebitPurchase(purchaseAmount, cardID, conn)) {
                System.out.println("Insufficient Funds!");
                return 0;
            }

            System.out.println("Please enter the vendor name");
            String vendorName = Input.getString();

            //Make insertion into purchase and debit_purchase and make method for withdrawal
            int accountID = getAccountIDFromDebitCard(cardID, conn);

            rowsUpdated += debitPurchase(accountID, cardID, vendorName, purchaseAmount, conn);
        } else if (debitOrCredit.equals("credit")) {
            cardIDs = printCards(customerID, "credit", conn);
            if (cardIDs.isEmpty()) return 0;

            //Ask to choose which card they would like to use
            System.out.println("Please choose the card you would like to make a purchase with");
            int cardID;

            while(true) {
                cardID = Input.getInt();

                if(cardIDs.contains(cardID)) break;
                System.out.println("Invalid option: Please try again");
            }

            System.out.println("Please enter the cost of the purchase");
            Double purchaseAmount = Input.getDouble();

            //Validate credit purchase to make sure it is not above credit_limit
            if(!validateCreditPurchase(purchaseAmount, cardID, conn)) {
                System.out.println("Amount exceeds remaining credit_limit!");
                return 0;
            }

            System.out.println("Please enter the vendor name");
            String vendorName = Input.getString();

            //credit purchase
            rowsUpdated += creditPurchase(cardID, vendorName, purchaseAmount, conn);
        }
        System.out.println("Thank you for your purchase!");

        return rowsUpdated;
    }

    private static boolean validateDebitPurchase(double amount, int cardID, Connection conn) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM debit_card NATURAL JOIN " +
                    "account NATURAL JOIN customer WHERE debit_id = ?");

            stmt.setInt(1, cardID);

            ResultSet res = stmt.executeQuery();

            if(res.isBeforeFirst()) {
                if (res.next()) {
                    double balance = res.getDouble("balance");
                    if (balance - amount < 0) {
                        return false;
                    }
                }
            }
            stmt.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error validating debit purchase");
        }

        return true;
    }

    private static boolean validateCreditPurchase(double amount, int cardID, Connection conn) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM credit_card WHERE credit_id = ?");

            stmt.setInt(1, cardID);

            ResultSet res = stmt.executeQuery();

            if(res.isBeforeFirst()) {
                if (res.next()) {
                    double credit_limit = res.getDouble("credit_limit");
                    double balance = res.getDouble("balance");
                    if ((credit_limit - balance) - amount < 0) {
                        return false;
                    }
                }
            }
            stmt.close();
            res.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error validating credit purchase");
        }

        return true;
    }

    private static int debitPurchase(int accountID, int cardID, String vendorName, double amount, Connection conn) {
        //Get new vendor ID to insert into purchases table
        int rowsUpdated = 0;
        int newVendorID = 0;
        int newPurchaseID = 0;

        //Get new vendor ID
        try {
            PreparedStatement getNewVendorID = conn.prepareStatement("SELECT MAX(vendor_id) AS vendor_id FROM purchases");

            ResultSet res = getNewVendorID.executeQuery();

            if (res.next()) {
                newVendorID = res.getInt("vendor_id") + 1;
            }

            getNewVendorID.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting new purchase");
        }

        //Insert new row into purchases table
        String[] returnID = {"purchase_id"};
        try {
            PreparedStatement insertPurchase = conn.prepareStatement("INSERT INTO purchases (vendor_id, vendor_name, amount, purchase_date) VALUES (?, ?, ?, ?)", returnID);

            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date date = new Date();
            String currDate = dateFormat.format(date);

            insertPurchase.setInt(1, newVendorID);
            insertPurchase.setString(2, vendorName);
            insertPurchase.setDouble(3, amount);
            insertPurchase.setString(4, currDate);

            rowsUpdated += insertPurchase.executeUpdate();

            ResultSet res = insertPurchase.getGeneratedKeys();

            if(res.next()) {
                newPurchaseID = res.getInt(1);
            }

            insertPurchase.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting into purchases table");
        }

        //Insert into debit_purchase table
        try {
            PreparedStatement insertDebitPurchase = conn.prepareStatement("INSERT INTO debit_purchase (purchase_id, debit_id) VALUES (?, ?)");

            insertDebitPurchase.setInt(1, newPurchaseID);
            insertDebitPurchase.setInt(2, cardID);

            rowsUpdated += insertDebitPurchase.executeUpdate();

            insertDebitPurchase.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting into debit_purchase");
        }

        //Withdraw amount from checking account
        try {
            PreparedStatement withdraw = conn.prepareStatement("UPDATE account SET balance = balance - ? WHERE account_id = ?");

            withdraw.setDouble(1, amount);
            withdraw.setInt(2, accountID);

            rowsUpdated += withdraw.executeUpdate();

            withdraw.close();
        } catch (SQLException ex) {
            System.out.println("Error withdrawing from checking account");
        }

        return rowsUpdated;
    }

    private static int creditPurchase(int cardID, String vendorName, double amount, Connection conn) {
        int rowsUpdated = 0;
        int newVendorID = 0;
        int newPurchaseID = 0;

        //Get new vendor ID
        try {
            PreparedStatement getNewVendorID = conn.prepareStatement("SELECT MAX(vendor_id) AS vendor_id FROM purchases");

            ResultSet res = getNewVendorID.executeQuery();

            if (res.next()) {
                newVendorID = res.getInt("vendor_id") + 1;
            }

            getNewVendorID.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting new purchase");
        }

        //Insert new row into purchases table
        String[] returnID = {"purchase_id"};
        try {
            PreparedStatement insertPurchase = conn.prepareStatement("INSERT INTO purchases (vendor_id, vendor_name, amount, purchase_date) VALUES (?, ?, ?, ?)", returnID);

            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date date = new Date();
            String currDate = dateFormat.format(date);

            insertPurchase.setInt(1, newVendorID);
            insertPurchase.setString(2, vendorName);
            insertPurchase.setDouble(3, amount);
            insertPurchase.setString(4, currDate);

            rowsUpdated += insertPurchase.executeUpdate();

            ResultSet res = insertPurchase.getGeneratedKeys();

            if(res.next()) {
                newPurchaseID = res.getInt(1);
            }

            insertPurchase.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting into purchases table");
        }

        //Insert into credit_purchase table
        try {
            PreparedStatement insertCreditPurchase = conn.prepareStatement("INSERT INTO credit_purchase (purchase_id, credit_id) VALUES (?, ?)");

            insertCreditPurchase.setInt(1, newPurchaseID);
            insertCreditPurchase.setInt(2, cardID);

            rowsUpdated += insertCreditPurchase.executeUpdate();

            insertCreditPurchase.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting into debit_purchase");
        }

        //Update balance for credit card
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE credit_card SET balance = balance + ? WHERE credit_id = ?");

            stmt.setDouble(1, amount);
            stmt.setInt(2, cardID);

            rowsUpdated += stmt.executeUpdate();

            stmt.close();
        } catch (SQLException ex) {
            System.out.println("Error updating balance for credit card");
        }

        return rowsUpdated;
    }

    private static List<Integer> printCards(int customerID, String type, Connection conn) {
        List<Integer> cards = null;
        if (type.equals("debit")) cards = printDebitCards(customerID, conn);
        else if (type.equals("credit")) cards = printCreditCards(customerID, conn);

        return cards;
    }

    private static List<Integer> printDebitCards(int customerID, Connection conn) {
        List<Integer> debitIDs = new ArrayList<>();
        try {
            PreparedStatement debit = conn.prepareStatement("SELECT * FROM account " +
                    "JOIN owns_account ON account.account_id = owns_account.account_id " +
                    "JOIN customer ON customer.customer_id = owns_account.customer_id " +
                    "JOIN debit_card ON debit_card.account_id = account.account_id " +
                    "WHERE customer.customer_id = ?");

            debit.setInt(1, customerID);

            ResultSet res = debit.executeQuery();

            System.out.printf("%-19s%-19s%-19s\n", "Debit ID", "Balance", "Checking Account ID");

            if (!res.isBeforeFirst()) {
                System.out.println("No debit cards available");
                return debitIDs;
            }

            while(res.next()) {
                int debitID = res.getInt("debit_id");
                double balance = res.getDouble("balance");
                int checkingAccountID = res.getInt("account_id");

                debitIDs.add(debitID);

                System.out.printf("%-19s%-19s%-19s\n", debitID, balance, checkingAccountID);
            }
            debit.close();
            res.close();
            System.out.println();

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error printing available cards");
        }

        return debitIDs;
    }

    private static List<Integer> printCreditCards(int customerID, Connection conn) {
        List<Integer> creditIDs = new ArrayList<>();

        try {
            PreparedStatement credit = conn.prepareStatement("SELECT * FROM credit_card NATURAL JOIN customer WHERE customer_id = ?");
            credit.setInt(1, customerID);

            ResultSet res = credit.executeQuery();

            System.out.printf("%-19s%-19s%-19s%-19s\n", "Credit ID", "Balance", "Credit Limit", "Balance Due");

            if (!res.isBeforeFirst()) {
                System.out.println("No credit cards available");
                return creditIDs;
            }

            while(res.next()) {
                int creditID = res.getInt("credit_id");
                double creditLimit = res.getDouble("credit_limit");
                double balance = res.getDouble("balance");
                double balanceDue = res.getDouble("balance_due");

                creditIDs.add(creditID);

                System.out.printf("%-19s%-19s%-19s%-19s\n", creditID, balance, creditLimit, balanceDue);

            }
            credit.close();
            res.close();

            System.out.println();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error printing out credit cards");
        }

        return creditIDs;
    }

    private static int getAccountIDFromDebitCard(int cardID, Connection conn) {
        int accountID = 0;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM debit_card NATURAL JOIN account NATURAL JOIN customer WHERE debit_id = ?");
            stmt.setInt(1, cardID);

            ResultSet res = stmt.executeQuery();

            if (res.next()) {
                accountID = res.getInt("account_id");
            }

            stmt.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error getting account ID from debit card");
        }

        return accountID;
    }
}
