package lk.ijse.gear_rent_pro.model;

public class Equipment {
    private int id;
    private String equipmentId; // unique external id
    private int categoryId;
    private String brand;
    private String model;
    private int purchaseYear;
    private double baseDailyPrice;
    private double securityDeposit;
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;
    private int branchId;

    public Equipment() {}

    public Equipment(int id, String equipmentId, int categoryId, String brand, String model, int purchaseYear, double baseDailyPrice, double securityDeposit, int branchId) {
        this.id = id;
        this.equipmentId = equipmentId;
        this.categoryId = categoryId;
        this.brand = brand;
        this.model = model;
        this.purchaseYear = purchaseYear;
        this.baseDailyPrice = baseDailyPrice;
        this.securityDeposit = securityDeposit;
        this.branchId = branchId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getPurchaseYear() { return purchaseYear; }
    public void setPurchaseYear(int purchaseYear) { this.purchaseYear = purchaseYear; }
    public double getBaseDailyPrice() { return baseDailyPrice; }
    public void setBaseDailyPrice(double baseDailyPrice) { this.baseDailyPrice = baseDailyPrice; }
    public double getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(double securityDeposit) { this.securityDeposit = securityDeposit; }
    public EquipmentStatus getStatus() { return status; }
    public void setStatus(EquipmentStatus status) { this.status = status; }
    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }

    @Override
    public String toString() {
        return "Equipment{" + "id=" + id + ", equipmentId='" + equipmentId + '\'' + ", brand='" + brand + '\'' + ", model='" + model + '\'' + '}';
    }
}
