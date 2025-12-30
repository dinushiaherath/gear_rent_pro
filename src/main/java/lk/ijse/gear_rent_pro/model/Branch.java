package lk.ijse.gear_rent_pro.model;

public class Branch {
    private int id;
    private String code;
    private String name;
    private String address;
    private String contact;

    public Branch() {}

    public Branch(int id, String code, String name, String address, String contact) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.address = address;
        this.contact = contact;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    @Override
    public String toString() {
        return "Branch{" + "id=" + id + ", code='" + code + '\'' + ", name='" + name + '\'' + '}';
    }
}
