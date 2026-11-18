package travelagency;

import java.time.LocalDate;

public class Payment {
    public enum Status { PENDING, PAID, REFUNDED, CANCELLED }

    private final int paymentId;
    private final int bookingId;
    private final double amount;
    private String method;          // UPI, CARD, CASH, NET_BANKING
    private Status status;
    private LocalDate paymentDate;
    private double refundAmount;

    public Payment(int paymentId, int bookingId, double amount) {
        this(paymentId, bookingId, amount, null, Status.PENDING, null, 0);
    }

    public Payment(int paymentId, int bookingId, double amount, String method,
                   Status status, LocalDate paymentDate, double refundAmount) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.paymentDate = paymentDate;
        this.refundAmount = refundAmount;
    }

    public int getPaymentId() { return paymentId; }
    public int getBookingId() { return bookingId; }
    public double getAmount() { return amount; }
    public String getMethod() { return method; }
    public Status getStatus() { return status; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public double getRefundAmount() { return refundAmount; }

    public void markPaid(String method, LocalDate date) {
        this.method = method;
        this.paymentDate = date;
        this.status = Status.PAID;
    }

    public void refund(double refundAmount) {
        this.refundAmount = refundAmount;
        this.status = Status.REFUNDED;
    }

    public void cancel() { this.status = Status.CANCELLED; }

    /** Money the agency actually keeps from this payment. */
    public double netRevenue() {
        if (status == Status.PAID) return amount;
        if (status == Status.REFUNDED) return amount - refundAmount;
        return 0;
    }

    @Override
    public String toString() {
        return String.format("Payment[%d] booking %d | Rs.%.2f | %s | %s | refund Rs.%.2f",
                paymentId, bookingId, amount, status, method == null ? "-" : method, refundAmount);
    }
}
