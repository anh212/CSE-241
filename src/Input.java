import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Input {

    private static Scanner scanner = new Scanner(System.in);

    public static int getInt() {
        while(true) {
            try {
                int output = scanner.nextInt();
                scanner.nextLine();

                return output;
            } catch (InputMismatchException ex) {
                System.out.println("Please enter a valid integer");
            }
        }
    }

    public static double getDouble() {
        while(true) {
            try {
                double output = scanner.nextDouble();
                scanner.nextLine();

                return output;
            } catch (InputMismatchException ex) {
                System.out.println("Please enter a valid double");
            }
        }
    }

    public static String getString() {
        while(true) {
            try {
                String output = scanner.nextLine();
                scanner.nextLine();

                return output;
            } catch (NoSuchElementException ex) {
                System.out.println("Error reading new line, please try again");
            }
        }
    }

    public static String getPhoneNumber() {
        while(true) {
            try {
                String phoneNum = scanner.nextLine();

                //Matching XXX-XXX-XXXX Format through regex expression
                if (phoneNum.matches("\\\\d{3}[-\\\\.\\\\s]\\\\d{3}[-\\\\.\\\\s]\\\\d{4}")) {
                    return phoneNum;
                }

            } catch (NoSuchElementException ex) {
                System.out.println("Error reading phone number, please try again");
            }
        }
    }

    public static String getDate() {
        while (true) {
            try {
                String date = scanner.nextLine();

                //Matching mm/dd/yyyy date format using regex expression
                if (date.matches(" ^(1[0-2]|0[1-9])/(3[01]|[12][0-9]|0[1-9])/[0-9]{4}$")) {
                    return date;
                }
            } catch (NoSuchElementException ex) {
                System.out.println("Error reading date, please try again");
            }
        }
    }
}
