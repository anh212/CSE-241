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
            try {
                String user = "";
                String password = "";

                System.out.println("Please enter username:");
                user = Input.getString();

                System.out.println("Please enter password for " + user + ":");
                password = String.valueOf(console.readPassword());
                conn = DriverManager.getConnection(
                        "jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", user, password);

                break;
            } catch (SQLException e) {
                System.out.println("Authentication failed, please try again");
            }
        }

        //If user causes SIGTERM to be triggered (Ex. pressing Ctrl-C to end program)
        //database will get disconnected
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//                try {
//                    conn.close();
//                    System.out.println("Database disconnected");
//                } catch (SQLException ex) {
//                    System.out.println("problem disconnecting");
//                }
//            }
//        });



        while(true) {
            //Prompt user to Main Menu
            System.out.println("Main Menu");
            System.out.println("Please enter the number corresponding to your role");
            System.out.println("[1] Bank Management");
            System.out.println("[2] Customer");
            System.out.println("[3] Exit Interface");

            String input = Input.getString();

            //Go to interface that user selects
            switch(input) {
                case "1":
                    BankManagementInterface.Interface(conn);
                    break;
                case "2":
                    CustomerInterface.Interface(conn);
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid command");
            }
        }
    }

    public void disconnect(Connection conn) {
        try {
            conn.close();
        } catch (SQLException ex) {
            System.out.println("problem disconnecting");
        }
    }
}
