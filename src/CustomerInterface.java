import java.sql.*;

public class CustomerInterface {
    private static Customer currentCustomer;

    //Main interface that home interface interacts with for anything customer related
    public static void Interface(Connection conn) {
        while(true) {

            //Prompt user to Main Menu
            System.out.println("Customer Interface");
            System.out.println("Please enter the number corresponding to your action");
            System.out.println("[1] Create new account");
            System.out.println("[2] Login to account");
            System.out.println("[3] Exit Interface");

            String input = Input.getString();

            //Go to interface that user selects
            switch(input) {
                case "1":
                    BankManagementInterface.Interface(conn);
                    break;
                case "2":
                    if (currentCustomer != null) {
                        System.out.println("Already logged in");
                    } else {
                        Login(conn);
                        AccountInterface.Interface(currentCustomer, conn);
                    }
                    Input.clearConsole();
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

    private static void NewAccount(Connection conn) {

    }

    private static void makeNewAccount(String firstName, String lastName, String phoneNumber, String dateOfBirth, Connection conn ) {

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
