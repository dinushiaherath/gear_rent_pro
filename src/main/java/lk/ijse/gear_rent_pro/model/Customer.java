package lk.ijse.gear_rent_pro.model;

import java.util.Objects;

public class Customer {
    private int id;
    private String customerId; // external id
    private String name;
    private String nicOrPassport;
    private String contactNo;
    private String email;
    private String address;
    private MembershipLevel membership = MembershipLevel.REGULAR;

    public Customer() {}

    public Customer(int id, String customerId, String name, String nicOrPassport, String contactNo, String email, String address) {
        this.id = id;
        this.customerId = customerId;
        this.name = name;
        this.nicOrPassport = nicOrPassport;
        this.contactNo = contactNo;
        this.email = email;
        this.address = address;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNicOrPassport() { return nicOrPassport; }
    public void setNicOrPassport(String nicOrPassport) { this.nicOrPassport = nicOrPassport; }
    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public MembershipLevel getMembership() { return membership; }
    public void setMembership(MembershipLevel membership) { this.membership = membership; }

    @Override
    public String toString() {
        return "Customer{" + "id=" + id + ", name='" + name + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }
}
