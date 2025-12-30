package lk.ijse.gear_rent_pro.model;

public enum PaymentStatus {
    PAID("Paid"),
    PARTIALLY_PAID("Partially Paid"),
    UNPAID("Unpaid");

    private final String displayName;

    PaymentStatus(String displayName) {
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
