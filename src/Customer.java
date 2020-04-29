public class Customer {
    private int id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String dateOfBirth;

    public Customer(int id, String firstName, String lastName, String phoneNumber, String dateOfBirth) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
    }

    public int getId() {
        if (this.id != 0) {
            return this.id;
        }
        return 0;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getDateOfBirth() {
        return this.dateOfBirth;
    }
}
