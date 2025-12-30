package lk.ijse.gear_rent_pro.model;

import java.time.LocalDate;

public class Rental {
    private int id;
    private String rentalId;
    private int equipmentId;
    private int customerId;
    private int branchId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double calculatedRentalAmount;
    private double securityDeposit;
    private double membershipDiscount;
    private double longRentalDiscount;
    private double finalPayableAmount;
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    private RentalStatus rentalStatus = RentalStatus.ACTIVE;
    // return/damage fields
    private LocalDate actualReturnDate;
    private String damageDescription;
    private double damageCharge;
    private double lateFee;

    public Rental() {}

    // getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRentalId() { return rentalId; }
    public void setRentalId(String rentalId) { this.rentalId = rentalId; }
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
    public double getCalculatedRentalAmount() { return calculatedRentalAmount; }
    public void setCalculatedRentalAmount(double calculatedRentalAmount) { this.calculatedRentalAmount = calculatedRentalAmount; }
    public double getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(double securityDeposit) { this.securityDeposit = securityDeposit; }
    public double getMembershipDiscount() { return membershipDiscount; }
    public void setMembershipDiscount(double membershipDiscount) { this.membershipDiscount = membershipDiscount; }
    public double getLongRentalDiscount() { return longRentalDiscount; }
    public void setLongRentalDiscount(double longRentalDiscount) { this.longRentalDiscount = longRentalDiscount; }
    public double getFinalPayableAmount() { return finalPayableAmount; }
    public void setFinalPayableAmount(double finalPayableAmount) { this.finalPayableAmount = finalPayableAmount; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public RentalStatus getRentalStatus() { return rentalStatus; }
    public void setRentalStatus(RentalStatus rentalStatus) { this.rentalStatus = rentalStatus; }
    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }
    public String getDamageDescription() { return damageDescription; }
    public void setDamageDescription(String damageDescription) { this.damageDescription = damageDescription; }
    public double getDamageCharge() { return damageCharge; }
    public void setDamageCharge(double damageCharge) { this.damageCharge = damageCharge; }
    public double getLateFee() { return lateFee; }
    public void setLateFee(double lateFee) { this.lateFee = lateFee; }
}
