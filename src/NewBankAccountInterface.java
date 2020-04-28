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
            newBalance = Input.getDouble();

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

        return rowsUpdated;
    }

    private static int NewChecking(int customerID, Connection conn) {
        int rowsUpdated = 0;

        System.out.println("Please enter your initial balance");
        double newBalance = Input.getDouble();

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

        return rowsUpdated;
    }

    private static int createNewSavings(int customerID, double interestRate, double balance, Connection conn) {
        int rowsUpdated = 0;
        String[] returnID = {"account_id"};
        int newAccountID = 0;

        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO account (interest_rate, balance) VALUES (?, ?)", returnID);

            stmt.setDouble(1, interestRate);
            stmt.setDouble(2, balance);

            rowsUpdated += stmt.executeUpdate();

            ResultSet res = stmt.getGeneratedKeys();

            if (res.next()) {
                newAccountID = res.getInt(1);
            }

            stmt.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting into accounts");
        }

        //Add into owns_account table
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO owns_account (customer_id, account_id) VALUES (?, ?)");

            stmt.setInt(1, customerID);
            stmt.setInt(2, newAccountID);

            rowsUpdated += stmt.executeUpdate();

            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting into owns_account");
        }

        //Inserting into savings
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO savings (account_id, min_balance) VALUES (?, 100)");

            stmt.setInt(1, newAccountID);

            rowsUpdated += stmt.executeUpdate();

            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting into savings");
        }

        return rowsUpdated;
    }

    private static int createNewChecking(int customerID, double interestRate, double balance, Connection conn) {
        int rowsUpdated = 0;
        String[] returnID = {"account_id"};
        int newAccountID = 0;

        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO account (interest_rate, balance) VALUES (?, ?)", returnID);

            stmt.setDouble(1, interestRate);
            stmt.setDouble(2, balance);

            rowsUpdated += stmt.executeUpdate();

            ResultSet res = stmt.getGeneratedKeys();

            if (res.next()) {
                newAccountID = res.getInt(1);
            }

            stmt.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting into accounts");
        }

        //Add into owns_account table
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO owns_account (customer_id, account_id) VALUES (?, ?)");

            stmt.setInt(1, customerID);
            stmt.setInt(2, newAccountID);

            rowsUpdated += stmt.executeUpdate();

            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting into owns_account");
        }

        //Inserting into savings
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO checking (account_id) VALUES (?)");

            stmt.setInt(1, newAccountID);

            rowsUpdated += stmt.executeUpdate();

            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting into checking");
        }

        return rowsUpdated;
    }
}
