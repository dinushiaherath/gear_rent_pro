package lk.ijse.gear_rent_pro.model;

public enum UserRole {
    ADMIN("Admin"),
    BRANCH_MANAGER("Branch Manager"),
    STAFF("Staff");

    private final String displayName;

    UserRole(String displayName) {
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
