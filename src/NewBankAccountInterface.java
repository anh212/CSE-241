import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NewBankAccountInterface {
    public static void Interface(Customer customer, Connection conn) {
        while (true) {
            System.out.println("New Bank Account Options");
            System.out.println("Please enter the number corresponding to your action");
            System.out.println("[1] Create new savings account");
            System.out.println("[2] Create new checking account");
            System.out.println("[3] Exit");

            String input = Input.getString();

            //Go to interface that user selects
            switch(input) {
                case "1":
                    NewSavings(customer.getId(), conn);
                    break;
                case "2":
                    NewChecking(customer.getId(), conn);
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid command");
            }
        }
    }

    private static int NewSavings(int customerID, Connection conn) {
        int rowsUpdated = 0;

        System.out.println("Please enter your initial balance");
        double newBalance;

        //Check that newBalance > min_balance ($100)
        while(true) {
            newBalance = Input.getDouble(1000000);

            if (newBalance >= 100) break;
            System.out.println("Need minimum balance of $100!");
        }

        System.out.println("Please enter your interest rate (Less than 100%): Please enter a decimal value");
        double interestRate;

        while(true) {
            interestRate = Input.getDouble();

            if (interestRate < 100) break;
            System.out.println("Interest rate must be less than 100%!");
        }

        rowsUpdated += createNewSavings(customerID, interestRate, newBalance, conn);
        System.out.println("Thanks for making a new savings account!");

        return rowsUpdated;
    }

    private static int NewChecking(int customerID, Connection conn) {
        int rowsUpdated = 0;

        System.out.println("Please enter your initial balance");
        double newBalance = Input.getDouble(1000000);

        System.out.println("Please enter your interest rate (Less than 100%): Please enter a decimal value");
        double interestRate;

        while(true) {
            interestRate = Input.getDouble();

            if (interestRate >= 100) {
                System.out.println("Interest rate must be less than 100%!");
            } else {
                break;
            }
        }

        rowsUpdated += createNewChecking(customerID, interestRate, newBalance, conn);
        System.out.println("Thanks for making a new checking account!");

        return rowsUpdated;
    }

    private static int createNewSavings(int customerID, double interestRate, double balance, Connection conn) {
        int rowsUpdated = 0;
        String[] returnID = {"account_id"};
        int newAccountID = 0;

        try {
            //Inserting into account table
            PreparedStatement account = conn.prepareStatement("INSERT INTO account (interest_rate, balance) VALUES (?, ?)", returnID);
            account.setDouble(1, interestRate);
            account.setDouble(2, balance);
            rowsUpdated += account.executeUpdate();
            ResultSet res = account.getGeneratedKeys();

            if (res.next()) {
                newAccountID = res.getInt(1);
            }

            account.close();
            res.close();

            //Inserting into own_account table
            PreparedStatement own_account = conn.prepareStatement("INSERT INTO owns_account (customer_id, account_id) VALUES (?, ?)");
            own_account.setInt(1, customerID);
            own_account.setInt(2, newAccountID);
            rowsUpdated += own_account.executeUpdate();
            own_account.close();

            //Inserting into savings table
            PreparedStatement savings = conn.prepareStatement("INSERT INTO savings (account_id, min_balance) VALUES (?, 100)");
            savings.setInt(1, newAccountID);
            rowsUpdated += savings.executeUpdate();
            savings.close();

            conn.commit();
        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                System.out.println("Error rolling back while inserting new savings account");
            }
            System.out.println("Error inserting into accounts");
        }
        return rowsUpdated;
    }

    private static int createNewChecking(int customerID, double interestRate, double balance, Connection conn) {
        int rowsUpdated = 0;
        String[] returnID = {"account_id"};
        int newAccountID = 0;

        try {
            //Inserting into account table
            PreparedStatement account = conn.prepareStatement("INSERT INTO account (interest_rate, balance) VALUES (?, ?)", returnID);
            account.setDouble(1, interestRate);
            account.setDouble(2, balance);
            rowsUpdated += account.executeUpdate();
            ResultSet res = account.getGeneratedKeys();

            if (res.next()) {
                newAccountID = res.getInt(1);
            }

            account.close();
            res.close();

            //Inserting into owns_account table
            PreparedStatement owns_account = conn.prepareStatement("INSERT INTO owns_account (customer_id, account_id) VALUES (?, ?)");
            owns_account.setInt(1, customerID);
            owns_account.setInt(2, newAccountID);
            rowsUpdated += owns_account.executeUpdate();
            owns_account.close();

            //Inserting into checking table
            PreparedStatement checking = conn.prepareStatement("INSERT INTO checking (account_id) VALUES (?)");
            checking.setInt(1, newAccountID);
            rowsUpdated += checking.executeUpdate();
            checking.close();

            conn.commit();
        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                System.out.println("Error rolling back while inserting new checking account");
            }
            System.out.println("Error inserting new checking account");
        }

        return rowsUpdated;
    }
}
