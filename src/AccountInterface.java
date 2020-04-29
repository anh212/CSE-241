import java.sql.Connection;

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
            System.out.println("[5] Take out a new loan");
            System.out.println("[6] Exit");

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
                    PurchaseInterface.Interface(customer, conn);
                    break;
                case "4":
                    NewBankAccountInterface.Interface(customer, conn);
                    break;
                case "5":
                    NewLoanInterface.Interface(customer, conn);
                    break;
                case "6":
                    return;
                default:
                    System.out.println("Invalid command");
            }
        }
    }
}
