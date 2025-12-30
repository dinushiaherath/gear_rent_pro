package lk.ijse.gear_rent_pro.model;

public enum MembershipLevel {
    REGULAR("Regular"),
    SILVER("Silver"),
    GOLD("Gold");

    private final String displayName;

    MembershipLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
