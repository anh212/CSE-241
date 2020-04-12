import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.function.DoubleToIntFunction;
import java.util.regex.Pattern;

public class Input {

    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    private static String readInput() {
        while (true) {
            try {
                String line = in.readLine();
                return line.trim();
            } catch (IOException e) {
                System.out.println("There was an error reading your input");
            }
        }
    }

    public static int getInt() {
        while(true) {
            try {
                String input = readInput();
                int number = Integer.parseInt(input);

                return number;
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid integer");
            }
        }
    }

    public static double getDouble() {
        while(true) {
            try {
                String input = readInput();
                double number = Double.parseDouble(input);

                return number;
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid double");
            }
        }
    }

    public static String getString() {
        return readInput();
    }

    public static String getPhoneNumber() {
        System.out.println("Please enter a phone number with the following format: XXX-XXX-XXXX");

        while(true) {
            String phoneNum = readInput();

            //Matching XXX-XXX-XXXX Format through regex expression
            if (phoneNum.matches("(?:\\d{3}-){2}\\d{4}")) {
                return phoneNum;
            }

            System.out.println("The phone number is in the wrong format: Please try again");

        }
    }

    public static String getDate() {
        System.out.println("Please enter a date with the following format: MM/DD/YYYY");

        while (true) {
            String date = readInput();

            //Matching mm/dd/yyyy date format using regex expression
            if (date.matches("^(1[0-2]|0[1-9])/(3[01]|[12][0-9]|0[1-9])/[0-9]{4}$")) {
                return date;
            }

            System.out.println("The date is in the wrong format: Please try again");
        }
    }
}
