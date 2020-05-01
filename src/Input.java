import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

public class Input {

    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    public static DecimalFormat decimalFormat = new DecimalFormat("#0.00");

    private static String readInput() {
        while (true) {
            try {
                String line = in.readLine();
                if (line.length() == 0) {
                    System.out.println("Input can't be empty");
                    continue;
                }
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

                if (number < 0) continue;

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

                if (number < 0) continue;

                return number;
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid double");
            }
        }
    }

    public static double getDouble(int max) {
        while(true) {
            try {
                String input = readInput();
                double number = Double.parseDouble(input);

                if (number <= 0 || number >= max) {
                    System.out.println("Your number is out of range: (0," + max + ")");
                    continue;
                }

                return number;
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid double");
            }
        }
    }

    public static String getString() {
        return readInput();
    }

    public static String getString(int maxLength) {
        String input;

        while (true) {
            input = getString();
            if (input.length() <= maxLength) return input;
            System.out.println("Input length needs to be less than or equal to " + maxLength);
        }
    }

    public static String getPhoneNumber() {
        System.out.println("Please enter a phone number with the following format: XXX-XXX-XXXX");

        while(true) {
            String phoneNum = readInput();

            //Matching XXX-XXX-XXXX Format through regex expression
            //Received from: https://stackoverflow.com/questions/42104546/java-regular-expressions-to-validate-phone-numbers
            if (phoneNum.matches("(?:\\d{3}-){2}\\d{4}")) {
                return phoneNum.trim();
            }

            System.out.println("The phone number is in the wrong format: Please try again");

        }
    }

    public static String getDate() {
        System.out.println("Please enter a date with the following format: MM/DD/YYYY");

        while (true) {
            String date = readInput();

            //Matching mm/dd/yyyy date format using regex expression
            //Received from: https://howtodoinjava.com/regex/java-regex-date-format-validation/
            if (date.matches("^(1[0-2]|0[1-9])/(3[01]|[12][0-9]|0[1-9])/[0-9]{4}$")) {
                return date.trim();
            }

            System.out.println("The date is in the wrong format: Please try again");
        }
    }

    public static String getStreetNum() {
        System.out.println("Please enter a maximum of 5 digits");

        while (true) {
            int streetNum = getInt();
            String streetNumString = String.valueOf(streetNum);

            if (streetNumString.length() <= 5) return streetNumString;

            System.out.println("Please input a street number that has a maximum of 5 digits");
        }
    }

    public static String getZipcode() {
        System.out.println("Please enter a 5 digit zipcode");

        while (true) {
            int zipcode = getInt();
            String zipString = String.valueOf(zipcode);

            if (zipString.length() == 5) return zipString;

            System.out.println("Please enter a zip code that is 5 digits");
        }
    }

    //Received from: https://stackoverflow.com/questions/2979383/java-clear-the-console
    public static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
