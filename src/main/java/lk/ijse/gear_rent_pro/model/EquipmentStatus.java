package lk.ijse.gear_rent_pro.model;

public enum EquipmentStatus {
    AVAILABLE("Available"),
    RESERVED("Reserved"),
    RENTED("Rented"),
    UNDER_MAINTENANCE("Under Maintenance");

    private final String displayName;

    EquipmentStatus(String displayName) {
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
