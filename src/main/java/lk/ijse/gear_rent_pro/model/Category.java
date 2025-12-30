package lk.ijse.gear_rent_pro.model;

public class Category {
    private int id;
    private String name;
    private String description;
    private double basePriceFactor;
    private double weekendMultiplier;
    private double defaultLateFeePerDay;
    private boolean active = true;

    public Category() {}

    public Category(int id, String name, String description, double basePriceFactor, double weekendMultiplier, double defaultLateFeePerDay) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.basePriceFactor = basePriceFactor;
        this.weekendMultiplier = weekendMultiplier;
        this.defaultLateFeePerDay = defaultLateFeePerDay;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getBasePriceFactor() { return basePriceFactor; }
    public void setBasePriceFactor(double basePriceFactor) { this.basePriceFactor = basePriceFactor; }
    public double getWeekendMultiplier() { return weekendMultiplier; }
    public void setWeekendMultiplier(double weekendMultiplier) { this.weekendMultiplier = weekendMultiplier; }
    public double getDefaultLateFeePerDay() { return defaultLateFeePerDay; }
    public void setDefaultLateFeePerDay(double defaultLateFeePerDay) { this.defaultLateFeePerDay = defaultLateFeePerDay; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "Category{" + "id=" + id + ", name='" + name + '\'' + '}';
    }
}
