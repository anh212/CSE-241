import java.sql.*;

public class CustomerInterface {
    private static Customer currentCustomer;

    //Main interface that home interface interacts with for anything customer related
    public static void Interface(Connection conn) {
        while(true) {

            //Prompt user to Main Menu
            System.out.println("Customer Interface");
            System.out.println("Please enter the number corresponding to your action");
            System.out.println("[1] Create new customer account");
            System.out.println("[2] Login to account");
            System.out.println("[3] Exit Interface");

            String input = Input.getString();

            //Go to interface that user selects
            switch(input) {
                case "1":
                    NewCustomerAccount(conn);
                    break;
                case "2":
                    if (currentCustomer != null) {
                        System.out.println("Already logged in");
                    } else {
                        Login(conn);
                        AccountInterface.Interface(currentCustomer, conn);
                    }
                    currentCustomer = null;
                    break;
                case "3":
                    //remove information about customer after they log out
                    currentCustomer = null;
                    return;
                default:
                    System.out.println("Invalid command");
            }
        }
    }


//
//    //Login for customer
//    public static void CustomerLogin(Connection conn) {
//        while(true) {
//
//        }
//    }

    private static void Login(Connection conn) {
        while(true) {
            System.out.println("Please enter you first name");
            String firstName = Input.getString();

            System.out.println("Please enter your last name");
            String lastName = Input.getString();

            System.out.println("Please enter you phone number");
            String phoneNumber = Input.getPhoneNumber();

            System.out.println("Please enter your date of birth");
            String dob = Input.getDate();

            if(!verifyLogin(firstName, lastName, phoneNumber, dob, conn)) {
                System.out.println("Login invalid: Please try again");
            } else {
                //Figure out how to get customer_id when making new customer
                int ID = getCustomerID(firstName, lastName, phoneNumber, dob, conn);
                currentCustomer = new Customer(ID, firstName, lastName, phoneNumber, dob, getBranchID(ID, conn));
                System.out.println("Welcome " + firstName + " " + lastName + "ID: " + currentCustomer.getId());
                break;
            }
        }
    }

    private static boolean verifyLogin(String firstName, String lastName, String phoneNumber, String dateOfBirth, Connection conn) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT first_name, last_name, phone_number, date_of_birth " +
                    "FROM customer WHERE first_name LIKE ? AND last_name LIKE ? AND phone_number LIKE ? AND date_of_birth LIKE ?");

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, phoneNumber);
            stmt.setString(4, dateOfBirth);

            ResultSet res = stmt.executeQuery();

            if (res.next()) {
                stmt.close();
                return true;
            }

            stmt.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error preparedStatement for Customer.verifyLogin");
        }

        return false;
    }

    private static int NewCustomerAccount(Connection conn) {
        int rowsUpdated = 0;

        System.out.println("Please enter you first name");
        String firstName = Input.getString();

        System.out.println("Please enter your last name");
        String lastName = Input.getString();

        System.out.println("Please enter your phone number");
        String phoneNum = Input.getPhoneNumber();

        //check for duplicate phone_number
        if(validatePhoneNumber(phoneNum, conn)) {
            System.out.println("This phone number already exists");
            return 0;
        }

        System.out.println("Please enter you birth date");
        String dob = Input.getDate();

        //Insert into customer table
        rowsUpdated += makeNewCustomerAccount(firstName, lastName, phoneNum, dob, conn);
        System.out.println("Welcome new customer!");

        return rowsUpdated;
    }

    private static int makeNewCustomerAccount(String firstName, String lastName, String phoneNumber, String dateOfBirth, Connection conn ) {
        int rowsUpdated = 0;
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO customer (first_name, last_name, phone_number, date_of_birth) VALUES (?, ?, ?, ?)");

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, phoneNumber);
            stmt.setString(4, dateOfBirth);

            rowsUpdated += stmt.executeUpdate();

            conn.commit();
            stmt.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error inserting new customer");

            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
                System.out.println("Error rolling back database after trying to insert customer");
            }
        }

        return rowsUpdated;
    }

    private static boolean validatePhoneNumber(String phoneNumber, Connection conn) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT phone_number FROM customer WHERE phone_number = ?");

            stmt.setString(1, phoneNumber);

            ResultSet res = stmt.executeQuery();

            if (res.next()) return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error getting phone numbers");
        }

        return false;
    }

    private static int getCustomerID(String firstName, String lastName, String phoneNumber, String dateOfBirth, Connection conn) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT customer_id FROM customer " +
                    "WHERE first_name LIKE ? AND last_name LIKE ? AND phone_number LIKE ? AND date_of_birth LIKE ?");

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, phoneNumber);
            stmt.setString(4, dateOfBirth);

            ResultSet res = stmt.executeQuery();

            if (res.next()) {
                int id = res.getInt("customer_id");
                stmt.close();
                return id;
            }

            stmt.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error getting customer ID");
        }

        //If customerID does not exist
        return 0;
    }

    private static int getBranchID(int customerID, Connection conn) {
        int branchID = 0;

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT branch_id FROM has_customers WHERE customer_id = ? ");
            stmt.setInt(1, customerID);

            ResultSet res = stmt.executeQuery();

            if (res.next()) {
                branchID = res.getInt("branch_id");
            }
            stmt.close();
            res.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error getting branch ID");
        }
        return branchID;
    }
}
