import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DepositWithdrawalInterface {
    public static int Deposit(Customer customer, Connection conn) {
        return AccountDeposit(customer.getId(), conn);
    }

    private static int AccountDeposit(int customerID, Connection conn) {
        int rowsUpdated = 0;

        //Request for what kind of account they would like to deposit to
        System.out.println("Would you like to deposit to savings or checking account? Please type \"savings\" or \"checking\"");
        String savingsOrChecking = null;

        //Checking for correct input
        while(true) {
            savingsOrChecking = Input.getString();

            if (savingsOrChecking.equals("savings") || savingsOrChecking.equals("checking")) break;
            System.out.println("Invalid option: Please try again");
        }


        System.out.println("Here are your available accounts");
        List<Integer> accountIDs = printAccounts(customerID, savingsOrChecking, conn);

        if (accountIDs.isEmpty()) return 0;

        //Requesting which account they would like to deposit to
        System.out.println("Please type in the ID of the account you would like to deposit to");
        int accountID;

        //Checking for correct accountID
        while(true) {
            accountID = Input.getInt();

            if(accountIDs.contains(accountID)) break;
            System.out.println("Invalid option: Please try again");
        }

        //Request how much they would like to deposit from account
        System.out.println("Please enter much would you like to deposit");
        Double withdrawalAmount = Input.getDouble();

//        //If there are sufficient funds then continue on with deposit
//        if(!validateWithdrawal(withdrawalAmount, accountID, conn)) {
//            System.out.println("Insufficient Funds!");
//            return 0;
//        }

        System.out.println("Here are the branches you can deposit to");
        List<Integer> branchIDs = printAvailableBranches("teller", conn);

        //Requesting which branch they would like to withdraw
        System.out.println("Please type in the ID of the branch you would like to deposit to");
        int branchID;

        while(true) {
            branchID = Input.getInt();

            if(branchIDs.contains(branchID)) break;
            System.out.println("Invalid option: Please try again");
        }

//        System.out.println("Would you like to deposit to an ATM or a teller? Please type \"atm\" or \"teller\"");
//        String atmOrTeller = null;
//
//        while(true) {
//            atmOrTeller = Input.getString();
//
//            if(atmOrTeller.equals("atm") || atmOrTeller.equals("teller")) break;
//            System.out.println("Invalid option: Please try again");
//        }

//        //If user chooses checking account and atm, then check if the checking account has a debit card
//        if(atmOrTeller.equals("atm") && savingsOrChecking.equals("checking")) {
//            if(!checkDebitCard(accountID, conn)) {
//                System.out.println("This account is not associated with a checking account");
//                return 0;
//            }
//        }

        List<Integer> methodIDs = null;
        methodIDs = printTellers(branchID, conn);

        System.out.println("Please type in the ID of the payment method you would like to deposit with");
        int methodID;

        while(true) {
            methodID = Input.getInt();

            if(methodIDs.contains(methodID)) break;
            System.out.println("Invalid option: Please try again");
        }

        //Make deposit
        rowsUpdated += depositTransaction(accountID, withdrawalAmount, methodID, conn);

//        //If withdrawal from savings account, then we need to check whether balance < min_balance and delete account
//        if (savingsOrChecking.equals("savings")) {
//            if(checkSavingsMinBalanceConstraint(accountID, conn)) {
//                System.out.println("Due to your savings account being below the minimum balance, it has been closed");
//            }
//        }

        System.out.println("Deposit completed. Thank you!");

        //If customerID does not exist
        return rowsUpdated;
    }

    private static int depositTransaction(int accountID, double amount, int methodID, Connection conn) {
        int rowsUpdated = 0;
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE account SET balance = balance + ? WHERE account_id = ?");

            stmt.setDouble(1, amount);
            stmt.setInt(2, accountID);

            rowsUpdated += stmt.executeUpdate();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error making deposit to account");
        }

        int transID = 0;
        try {
            String[] returnId = { "trans_id" };
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO transactions (amount, month, day, year) VALUES (?, ?, ?, ?)", returnId);

            Calendar calendar = Calendar.getInstance();
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            int currentYear = calendar.get(Calendar.YEAR);

            stmt.setDouble(1, amount);
            stmt.setInt(2, currentMonth);
            stmt.setInt(3, currentDay);
            stmt.setInt(4, currentYear);

            rowsUpdated += stmt.executeUpdate();

            ResultSet res = stmt.getGeneratedKeys();

            if(res.next()) {
                transID = res.getInt(1);
            }

            stmt.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting deposit transaction");
        }

        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO deposit (trans_id, method_id, account_id) VALUES (?, ?, ?)");

            stmt.setInt(1, transID);
            stmt.setInt(2, methodID);
            stmt.setInt(3, accountID);

            rowsUpdated += stmt.executeUpdate();

            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting withdrawal transaction");
        }

        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO account_trans (trans_id) VALUES (?)");

            stmt.setInt(1, transID);

            rowsUpdated += stmt.executeUpdate();

            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting into account_trans");
        }

        return rowsUpdated;
    }

    public static int Withdrawal(Customer customer, Connection conn) {
        return AccountWithdrawal(customer.getId(), conn);
    }

    private static int AccountWithdrawal(int customerID, Connection conn) {
        int rowsUpdated = 0;

        //Request for what kind of account they would like to withdraw from
        System.out.println("Would you like to withdraw from savings or checking account? Please type \"savings\" or \"checking\"");
        String savingsOrChecking = null;

        //Checking for correct input
        while(true) {
            savingsOrChecking = Input.getString();

            if (savingsOrChecking.equals("savings") || savingsOrChecking.equals("checking")) break;
            System.out.println("Invalid option: Please try again");
        }


        System.out.println("Here are your available accounts");
        List<Integer> accountIDs = printAccounts(customerID, savingsOrChecking, conn);

        if (accountIDs.isEmpty()) return 0;

        //Requesting which account they would like to withdraw from
        System.out.println("Please type in the ID of the account you would like to withdraw from");
        int accountID;

        //Checking for correct accountID
        while(true) {
            accountID = Input.getInt();

            if(accountIDs.contains(accountID)) break;
            System.out.println("Invalid option: Please try again");
        }

        //Request how much they would like to withdraw from account
        System.out.println("Please enter much would you like to withdraw");
        Double withdrawalAmount = Input.getDouble();

        //If there are sufficient funds then continue on with withdrawal
        if(!validateWithdrawal(withdrawalAmount, accountID, conn)) {
            System.out.println("Insufficient Funds!");
            return 0;
        }

        System.out.println("Here are the branches you can withdraw from");
        List<Integer> branchIDs = printAvailableBranches("both", conn);

        //Requesting which branch they would like to withdraw
        System.out.println("Please type in the ID of the branch you would like to withdraw from");
        int branchID;

        while(true) {
            branchID = Input.getInt();

            if(branchIDs.contains(branchID)) break;
            System.out.println("Invalid option: Please try again");
        }

        System.out.println("Would you like to withdraw from an ATM or a teller? Please type \"atm\" or \"teller\"");
        String atmOrTeller = null;

        while(true) {
            atmOrTeller = Input.getString();

            if(atmOrTeller.equals("atm") || atmOrTeller.equals("teller")) break;
            System.out.println("Invalid option: Please try again");
        }

        //If user chooses checking account and atm, then check if the checking account has a debit card
        if(atmOrTeller.equals("atm") && savingsOrChecking.equals("checking")) {
            if(!checkDebitCard(accountID, conn)) {
                System.out.println("This account is not associated with a debit card");
                return 0;
            }
        }

        List<Integer> methodIDs = null;
        if(atmOrTeller.equals("atm")) {
            methodIDs =  printATMs(branchID, conn);
        } else if (atmOrTeller.equals("teller")) {
            methodIDs = printTellers(branchID, conn);
        }

        System.out.println("Please type in the ID of the payment method you would like to withdraw with");
        int methodID;

        while(true) {
            methodID = Input.getInt();

            if(methodIDs.contains(methodID)) break;
            System.out.println("Invalid option: Please try again");
        }

        //Make transaction
        rowsUpdated += withdrawalTransaction(accountID, withdrawalAmount, methodID, conn);

        //If withdrawal from savings account, then we need to check whether balance < min_balance and delete account
        if (savingsOrChecking.equals("savings")) {
            if(checkSavingsMinBalanceConstraint(accountID, conn)) {
                System.out.println("Due to your savings account being below the minimum balance, it has been closed");
            }
        }

        System.out.println("Withdrawal completed. Thank you!");

        //If customerID does not exist
        return rowsUpdated;
    }

    private static int withdrawalTransaction(int accountID, double amount, int methodID, Connection conn) {
        int rowsUpdated = 0;
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE account SET balance = balance - ? WHERE account_id = ?");

            stmt.setDouble(1, amount);
            stmt.setInt(2, accountID);

            rowsUpdated += stmt.executeUpdate();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error making withdrawal from account");
        }

        int transID = 0;
        try {
            String[] returnId = { "trans_id" };
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO transactions (amount, month, day, year) VALUES (?, ?, ?, ?)", returnId);

            Calendar calendar = Calendar.getInstance();
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            int currentYear = calendar.get(Calendar.YEAR);

            stmt.setDouble(1, amount);
            stmt.setInt(2, currentMonth);
            stmt.setInt(3, currentDay);
            stmt.setInt(4, currentYear);

            rowsUpdated += stmt.executeUpdate();

            ResultSet res = stmt.getGeneratedKeys();

            if(res.next()) {
                transID = res.getInt(1);
            }

            stmt.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting withdrawal transaction");
        }

        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO withdrawal (trans_id, method_id, account_id) VALUES (?, ?, ?)");

            stmt.setInt(1, transID);
            stmt.setInt(2, methodID);
            stmt.setInt(3, accountID);

            rowsUpdated += stmt.executeUpdate();

            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting withdrawal transaction");
        }

        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO account_trans (trans_id) VALUES (?)");

            stmt.setInt(1, transID);

            rowsUpdated += stmt.executeUpdate();

            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting into account_trans");
        }

        return rowsUpdated;
    }

    private static boolean validateWithdrawal(Double amount, int accountID, Connection conn) {
        //For withdrawal check if amount withdrawn will make balance < 0
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM account WHERE account_id = ?");
            stmt.setInt(1, accountID);

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
            System.out.println("Error validating withdrawal");
        }

        return true;
    }

    private static boolean checkSavingsMinBalanceConstraint(int accountID, Connection conn) {
        //Check if balance is below min balance for savings account
        //If it is then delete the account and return true
        //else return false
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT balance, min_balance FROM account NATURAL JOIN savings WHERE account_id = ?");

            stmt.setInt(1, accountID);

            ResultSet res = stmt.executeQuery();

            if (res.isBeforeFirst()) {
                if (res.next()) {
                    double balance = res.getDouble("balance");
                    double min_balance = res.getDouble("min_balance");

                    stmt.close();
                    res.close();

                    if (balance < min_balance) {
                        deleteAccount(accountID, conn);
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error comparing savings balance and min_balance");
        }

        return false;
    }

    //List valid branches available depending on if customer deposits or withdraws or pays loan
    //tranMethod options: "atm", "teller", "both'
    private static List<Integer> printAvailableBranches(String transMethod, Connection conn) {
        List<Integer> branchIDs = new ArrayList<>();

        try {
            PreparedStatement stmt = null;

            if (transMethod.equals("atm")) {
                stmt = conn.prepareStatement("SELECT * FROM branch WHERE branch_id IN " +
                        "(SELECT branch_id FROM trans_method JOIN atm ON trans_method.method_id = atm.method_id)");
            } else if (transMethod.equals("teller")) {
                stmt = conn.prepareStatement("SELECT * FROM branch WHERE branch_id IN " +
                        "(SELECT branch_id FROM trans_method JOIN teller ON trans_method.method_id = teller.method_id)");
            } else if (transMethod.equals("both")) {
                stmt = conn.prepareStatement("SELECT * FROM branch WHERE exists " +
                        "(SELECT method_id FROM trans_method WHERE branch_id = branch.branch_id MINUS SELECT method_id FROM atm)");
            }

            ResultSet res = stmt.executeQuery();

            System.out.printf("%-19s%-19s%-19s%-19s%-19s%-19s\n", "branch ID", "Street Number", "Street Name", "City", "State", "ZIP");

            while(res.next()) {
                int branchID = res.getInt("branch_id");
                String streetNum = res.getString("street_number");
                String streetName = res.getString("street_name");
                String city = res.getString("city");
                String state = res.getString("state");
                String zip = res.getString("zip");

                System.out.printf("%-19s%-19s%-19s%-19s%-19s%-19s\n", branchID, streetNum, streetName, city, state, zip);

                //Cache branchIDs so we can check if user input is in this set
                branchIDs.add(branchID);
            }
            stmt.close();
            res.close();
            System.out.println();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error printing branches");
        }

        return branchIDs;
    }

    private static List<Integer> printAccounts(int customerID, String choice, Connection conn) {
        List<Integer> accounts = null;
        if (choice.equals("savings")) {
            accounts = printSavingsAccounts(customerID, conn);
        } else if (choice.equals("checking")) {
            accounts = printCheckingAccounts(customerID, conn);
        }

        return accounts;
    }

    //Printing both checking and savings accounts for customer
    private static List<Integer> printCheckingAccounts(int customerID, Connection conn) {
        List<Integer> accountIDs = new ArrayList<>();

        try {
            PreparedStatement checking = conn.prepareStatement("SELECT * FROM " +
                    "account " +
                    "JOIN checking ON account.account_id = checking.account_id " +
                    "JOIN owns_account ON owns_account.account_id = account.account_id " +
                    "WHERE customer_id = ?");

            checking.setInt(1, customerID);

            //Printing checking accounts
            ResultSet res = checking.executeQuery();

            //Checking for empty set
            if(!res.isBeforeFirst()) {
                System.out.println("No checking accounts available");
                return accountIDs;
            }

            System.out.printf("%-19s%-19s%-19s\n", "AccountID", "Interest Rate", "Balance");
            while(res.next()) {
                int accountID = res.getInt("account_id");
                double interest = res.getDouble("interest_rate");
                double balance = res.getDouble("balance");

                accountIDs.add(accountID);

                System.out.printf("%-19s%-19s%-19s\n", accountID, interest, balance);
            }
            checking.close();
            res.close();
            System.out.println();


        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error printing checking accounts");
        }

        return accountIDs;
    }

    private static List<Integer> printSavingsAccounts(int customerID, Connection conn) {
        List<Integer> accountIDs = new ArrayList<>();

        try {
            PreparedStatement savings = conn.prepareStatement("SELECT * FROM " +
                    "account " +
                    "JOIN savings ON account.account_id = savings.account_id " +
                    "JOIN owns_account ON owns_account.account_id = account.account_id " +
                    "WHERE customer_id = ?");
            savings.setInt(1, customerID);

            System.out.printf("%-19s%-19s%-19s%-19s\n", "AccountID", "Interest Rate", "Balance", "Minimum Balance");

            ResultSet res = savings.executeQuery();

            if(!res.isBeforeFirst()) {
                System.out.println("No savings accounts available");
                return accountIDs;
            }

            while(res.next()) {
                int accountID = res.getInt("account_id");
                double interest = res.getDouble("interest_rate");
                double balance = res.getDouble("balance");
                double minBalance = res.getDouble("min_balance");

                accountIDs.add(accountID);

                System.out.printf("%-19s%-19s%-19s%-19s\n", accountID, interest, balance, minBalance);
            }
            savings.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error printing savings accounts");
        }

        return accountIDs;
    }

    private static int deleteAccount(int accountID, Connection conn) {
        int rowsUpdated = 0;
        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM account WHERE account_id = ?");

            stmt.setInt(1, accountID);

            rowsUpdated = stmt.executeUpdate();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error deleting account");
        }
        return rowsUpdated;
    }

    private static List<Integer> printTellers(int branchID, Connection conn) {
        List<Integer> tellerIDs = new ArrayList<>();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM trans_method " +
                    "JOIN teller ON trans_method.method_id = teller.method_id WHERE branch_id = ?");

            stmt.setInt(1, branchID);

            ResultSet res = stmt.executeQuery();

            if (!res.isBeforeFirst()) {
                System.out.println("No tellers available");
                return tellerIDs;
            }

            System.out.printf("%-19s%-19s\n", "Teller ID", "Name");

            while(res.next()) {
                int tellerID = res.getInt("method_id");
                String name = res.getString("name");

                tellerIDs.add(tellerID);
                System.out.printf("%-19s%-19s\n", tellerID, name);
            }

            stmt.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.printf("Error printing tellers");
        }

        return tellerIDs;
    }

    private static List<Integer> printATMs(int branchID, Connection conn) {
        List<Integer> atmIDs = new ArrayList<>();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM trans_method WHERE " +
                    "method_id IN (SELECT method_id FROM atm) AND branch_id = ?");

            stmt.setInt(1, branchID);

            ResultSet res = stmt.executeQuery();

            if (!res.isBeforeFirst()) {
                System.out.println("No ATMs available");
                return atmIDs;
            }

            System.out.printf("%-19s\n", "ATM");

            while(res.next()) {
                int atmID = res.getInt("method_id");

                atmIDs.add(atmID);
                System.out.printf("%-19s\n", atmID);
            }

            stmt.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.printf("Error printing ATMs");
        }

        return atmIDs;
    }

    private static boolean checkDebitCard(int accountID, Connection conn) {
        System.out.println(accountID);
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT debit_id FROM debit_card WHERE account_id = ?");

            stmt.setInt(1, accountID);

            ResultSet res = stmt.executeQuery();

            if(res.isBeforeFirst()) {
                System.out.println("Get true");
                return true;
            }

            stmt.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error checking for debit card existence");
        }
        return false;
    }
}
