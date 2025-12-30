package lk.ijse.gear_rent_pro.model;

public class MembershipConfig {
    private MembershipLevel level;
    private double discountPercent;

    public MembershipConfig() {}

    public MembershipConfig(MembershipLevel level, double discountPercent) {
        this.level = level;
        this.discountPercent = discountPercent;
    }

    public MembershipLevel getLevel() { return level; }
    public void setLevel(MembershipLevel level) { this.level = level; }
    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }
}
