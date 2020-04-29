import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NewLoanInterface {
    public static void Interface(Customer customer, Connection conn) {
        while (true) {
            //Prompt user to Main Menu
            System.out.println("Loan Options");
            System.out.println("Please enter the number corresponding to your action");
            System.out.println("[1] Make a new unsecured loan");
            System.out.println("[2] Make a new mortgage");
            System.out.println("[3] Exit");

            String input = Input.getString();

            //Go to interface that user selects
            switch(input) {
                case "1":
                    NewUnsecured(customer, conn);
                    break;
                case "2":
                    NewMortgage(customer, conn);
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid command");
            }
        }
    }

    private static int NewMortgage(Customer customer, Connection conn) {
        int customerID = customer.getId();
        int rowsUpdated = 0;

        System.out.println("Please enter the amount for your loan");
        double amount = Input.getDouble(100000);

        System.out.println("Please enter monthly payment");
        double monthlyPayment = Input.getDouble(100000);

        System.out.println("Please enter an interest rate");
        double interestRate;

        while(true) {
            interestRate = Input.getDouble();

            if (interestRate < 100) break;
            System.out.println("Interest rate must be less than 100%!");
        }

        System.out.println("Please enter address tied to the mortgage");

        System.out.println("Please enter a street number");
        String streetNum = Input.getStreetNum();

        System.out.println("Please enter a street name");
        String streetName = Input.getString(20);

        System.out.println("Please enter a city");
        String city = Input.getString(20);

        System.out.println("Please enter a state");
        String state = Input.getString(20);

        System.out.println("Please enter a zipcode");
        String zipcode = Input.getZipcode();

        if (validateMortgageAddress(streetNum, streetName, city, state, zipcode, conn)) {
            System.out.println("Cannot create another loan for the same address");
        } else {
            rowsUpdated += insertMortgage(customerID, amount, monthlyPayment, interestRate,
                    streetNum, streetName, city, state, zipcode, conn);

            System.out.println("Thank for making a new loan!");
        }

        return rowsUpdated;
    }

    private static int NewUnsecured(Customer customer, Connection conn) {
        int customerID = customer.getId();
        int rowsUpdated = 0;

        System.out.println("Please enter the amount for your loan");
        double amount = Input.getDouble(100000);

        System.out.println("Please enter monthly payment");
        double monthlyPayment = Input.getDouble(100000);

        System.out.println("Please enter an interest rate");
        double interestRate;

        while(true) {
            interestRate = Input.getDouble();

            if (interestRate < 100) break;
            System.out.println("Interest rate must be less than 100%!");
        }

        rowsUpdated += insertUnsecured(customerID, amount, monthlyPayment, interestRate,conn);
        System.out.println("Thank for making a new loan!");

        return rowsUpdated;
    }

    private static int insertUnsecured(int customerID, double amount, double monthlyPayment, double interestRate, Connection conn) {
        int rowsUpdated = 0;
        int newLoanID = 0;

        try {
            //Inserting into loan table
            String[] returnID = {"loan_id"};
            PreparedStatement loan = conn.prepareStatement("INSERT INTO loan (amount, monthly_payment, interest_rate) VALUES " +
                    "(?, ?, ?)", returnID);

            loan.setDouble(1, amount);
            loan.setDouble(2, monthlyPayment);
            loan.setDouble(3, interestRate);

            rowsUpdated += loan.executeUpdate();

            ResultSet res = loan.getGeneratedKeys();

            if (res.next()) {
                newLoanID = res.getInt(1);
            }

            loan.close();
            res.close();

            //Inserting into unsecured_loan table
            PreparedStatement unsecured = conn.prepareStatement("INSERT INTO unsecured_loan (loan_id) VALUES (?)");
            unsecured.setInt(1, newLoanID);
            rowsUpdated += unsecured.executeUpdate();

            //Insert into has_loan table
            PreparedStatement hasLoan = conn.prepareStatement("INSERT INTO has_loan (customer_id, loan_id) VALUES (?, ?)");
            hasLoan.setInt(1, customerID);
            hasLoan.setInt(2, newLoanID);
            rowsUpdated += hasLoan.executeUpdate();

            conn.commit();
        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
                System.out.println("Error rolling back while inserting new unsecured loan");
            }

            ex.printStackTrace();
            System.out.println("Error inserting new unsecured loan");
        }

        return rowsUpdated;
    }

    private static int insertMortgage(int customerID, double amount, double monthlyPayment, double interestRate,
                        String streetNum, String streetName, String city, String state, String zipcode, Connection conn) {

        int rowsUpdated = 0;
        int newLoanID = 0;

        try {
            //Inserting into loan table
            String[] returnID = {"loan_id"};
            PreparedStatement loan = conn.prepareStatement("INSERT INTO loan (amount, monthly_payment, interest_rate) VALUES " +
                    "(?, ?, ?)", returnID);

            loan.setDouble(1, amount);
            loan.setDouble(2, monthlyPayment);
            loan.setDouble(3, interestRate);

            rowsUpdated += loan.executeUpdate();

            ResultSet res = loan.getGeneratedKeys();

            if (res.next()) {
                newLoanID = res.getInt(1);
            }

            loan.close();
            res.close();

            //Inserting into mortgage table
            PreparedStatement mortgage = conn.prepareStatement("INSERT INTO mortgage (loan_id, street_number, " +
                    "street_name, city, state, zip) VALUES (?, ?, ?, ?, ?, ?)");
            mortgage.setInt(1, newLoanID);
            mortgage.setString(2, streetNum);
            mortgage.setString(3, streetName);
            mortgage.setString(4, city);
            mortgage.setString(5, state);
            mortgage.setString(6, zipcode);
            rowsUpdated += mortgage.executeUpdate();

            //Insert into has_loan table
            PreparedStatement hasLoan = conn.prepareStatement("INSERT INTO has_loan (customer_id, loan_id) VALUES (?, ?)");
            hasLoan.setInt(1, customerID);
            hasLoan.setInt(2, newLoanID);
            rowsUpdated += hasLoan.executeUpdate();

            conn.commit();
        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
                System.out.println("Error rolling back while inserting new mortgage");
            }

            ex.printStackTrace();
            System.out.println("Error inserting new mortgage");
        }

        return rowsUpdated;
    }

    private static boolean validateMortgageAddress(String streetNum, String streetName, String city,
                                    String state, String zipcode, Connection conn) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM mortgage WHERE street_number = ? AND street_name = ? AND " +
                    "city = ? AND state = ? AND zip = ?");

            stmt.setString(1, streetNum);
            stmt.setString(2, streetName);
            stmt.setString(3, city);
            stmt.setString(4, state);
            stmt.setString(5, zipcode);

            ResultSet res = stmt.executeQuery();

            if (res.next()) return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error trying to validate mortgage address");
        }
        return false;
    }

}
