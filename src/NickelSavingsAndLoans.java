import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

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
        Connection conn = null;

        Console console = System.console();
        Scanner scan = new Scanner(System.in);

        //Asking for database credentials for authentication
        while(true) {
            try {
                String user = "";
                String password = "";

                System.out.println("Please enter username:");
                user = scan.nextLine();

                System.out.println("Please enter password for " + user + ":");
                password = String.valueOf(console.readPassword());
                conn = DriverManager.getConnection(
                        "jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", user, password);

                break;
            } catch (SQLException e) {
                System.out.println("Authentication failed, please try again");
            }
        }

        try {
            conn.close();
        } catch (SQLException ex) {
            System.out.println("problem disconncting");
        }

        //Prompt user to Main Menu
        System.out.println("Main Menu");
        System.out.println("Please enter the number corresponding to your role");
        System.out.println("[1] Bank Management");
        System.out.println("[2] Customer");

        while(true) {

        }
    }
}
