public class Dealer {
    private String code;
    private String name;
    private String phone;
    private String location;

    // Constructor
    public Dealer(String code, String name, String phone, String location) {
        this.code = code;
        this.name = name;
        this.phone = phone;
        this.location = location;
    }

    // Getters
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getLocation() { return location; }
}


