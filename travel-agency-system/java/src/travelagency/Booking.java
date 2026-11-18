package travelagency;

import java.time.LocalDate;

public class Booking {
    public enum Status { CONFIRMED, CANCELLED }

    private final int bookingId;
    private final Customer customer;
    private final TravelPackage travelPackage;
    private final int numTravelers;
    private final LocalDate bookingDate;
    private final LocalDate travelDate;
    private final double totalAmount;
    private Status status;
    private LocalDate cancellationDate;
    private String cancellationReason;
    private double refundAmount;

    public Booking(int bookingId, Customer customer, TravelPackage travelPackage, int numTravelers,
                   LocalDate bookingDate, LocalDate travelDate, double totalAmount) {
        this(bookingId, customer, travelPackage, numTravelers, bookingDate, travelDate,
                totalAmount, Status.CONFIRMED, null, null, 0);
    }

    public Booking(int bookingId, Customer customer, TravelPackage travelPackage, int numTravelers,
                   LocalDate bookingDate, LocalDate travelDate, double totalAmount,
                   Status status, LocalDate cancellationDate, String cancellationReason, double refundAmount) {
        this.bookingId = bookingId;
        this.customer = customer;
        this.travelPackage = travelPackage;
        this.numTravelers = numTravelers;
        this.bookingDate = bookingDate;
        this.travelDate = travelDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.cancellationDate = cancellationDate;
        this.cancellationReason = cancellationReason;
        this.refundAmount = refundAmount;
    }

    public int getBookingId() { return bookingId; }
    public Customer getCustomer() { return customer; }
    public TravelPackage getTravelPackage() { return travelPackage; }
    public int getNumTravelers() { return numTravelers; }
    public LocalDate getBookingDate() { return bookingDate; }
    public LocalDate getTravelDate() { return travelDate; }
    public double getTotalAmount() { return totalAmount; }
    public Status getStatus() { return status; }
    public LocalDate getCancellationDate() { return cancellationDate; }
    public String getCancellationReason() { return cancellationReason; }
    public double getRefundAmount() { return refundAmount; }
    public boolean isConfirmed() { return status == Status.CONFIRMED; }

    public void cancel(LocalDate date, String reason, double refund) {
        this.status = Status.CANCELLED;
        this.cancellationDate = date;
        this.cancellationReason = reason;
        this.refundAmount = refund;
    }

    @Override
    public String toString() {
        String s = String.format("Booking[%d] %s -> %s (%s) | %d traveler(s) | booked %s | travel %s | Rs.%.2f | %s",
                bookingId, customer.getName(), travelPackage.getPackageName(), travelPackage.getDestination(),
                numTravelers, bookingDate, travelDate, totalAmount, status);
        if (status == Status.CANCELLED) {
            s += String.format(" on %s (%s), refund Rs.%.2f", cancellationDate, cancellationReason, refundAmount);
        }
        return s;
    }
}
