import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class NickelSavingsAndLoans {
    public static void main(String[] args) {
        //Check if OJDBC driver exists
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException ex) {
            System.out.println("OJDBC Driver not found");
            System.exit(1);
        }

        //Main connection used through out application
        Connection conn;

        Console console = System.console();

        //Asking for database credentials for authentication
        while(true) {
            String user = "";
            String password = "";

            System.out.println("Please enter username:");
            user = Input.getString();

            System.out.println("Please enter password for " + user + ":");
            password = String.valueOf(console.readPassword());

            try {
                conn = DriverManager.getConnection(
                        "jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", user, password);
                conn.setAutoCommit(false);
                break;
            } catch (SQLException e) {
                System.out.println("Authentication failed, please try again");
            }
        }

        while(true) {
            //Prompt user to Main Menu
            Input.clearConsole();

            System.out.println("Welcome to Nickel Savings and Loans");

            System.out.println();
            System.out.println("-------------");
            System.out.println("| Main Menu |");
            System.out.println("-------------");
            System.out.println();

            System.out.println("Please enter the number corresponding to your role");
            System.out.println("[1] Customer");
            System.out.println("[2] Exit Interface");

            String input = Input.getString();

            //Go to interface that user selects
            switch(input) {
                case "1":
                    CustomerInterface.Interface(conn);
                    break;
                case "2":
                    disconnect(conn);
                    return;
                default:
                    System.out.println("Invalid command");
            }
        }
    }

    public static void disconnect(Connection conn) {
        try {
            conn.close();
        } catch (SQLException ex) {
            System.out.println("problem disconnecting");
        }
    }
}
