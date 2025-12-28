package lk.ijse.gear_rent_pro.model;

import java.time.LocalDate;

public class Reservation {
    private int id;
    private String reservationId;
    private int equipmentId;
    private int customerId;
    private int branchId;
    private LocalDate startDate;
    private LocalDate endDate;

    public Reservation() {}

    public Reservation(int id, String reservationId, int equipmentId, int customerId, int branchId, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.reservationId = reservationId;
        this.equipmentId = equipmentId;
        this.customerId = customerId;
        this.branchId = branchId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }
    public int getEquipmentId() { return equipmentId; }
    public void setEquipmentId(int equipmentId) { this.equipmentId = equipmentId; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
