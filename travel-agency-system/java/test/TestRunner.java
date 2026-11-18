import travelagency.*;
import java.time.LocalDate;

/** Plain-Java test harness (no external libraries). Prints: ID | technique | input | expected | actual | PASS/FAIL */
public class TestRunner {
    static int pass = 0, fail = 0;
    static final LocalDate TODAY = LocalDate.of(2026, 11, 16);

    static void check(String id, String technique, String input, String expected, String actual) {
        boolean ok = expected.equals(actual);
        if (ok) pass++; else fail++;
        System.out.println(id + " | " + technique + " | " + input + " | " + expected + " | " + actual + " | " + (ok ? "PASS" : "FAIL"));
    }

    interface Action { Object run() throws BookingException; }

    static String outcome(Action a) {
        try { Object o = a.run(); return o == null ? "OK" : o.toString(); }
        catch (BookingException e) { return "ERROR: " + e.getMessage(); }
    }

    static TravelAgency fresh() throws BookingException {
        TravelAgency a = new TravelAgency();
        a.registerCustomer("Arun Kumar", "9876543210", "arun@example.com");
        a.addPackage("Goa Beach Escape", "Goa", 4, 12000, 10);
        return a;
    }

    public static void main(String[] args) throws Exception {
        LocalDate trip = LocalDate.of(2026, 12, 20);
        TravelAgency a = fresh();

        // Customer registration - equivalence partitioning
        check("TC-01", "Black box / EP", "Register valid customer", "Customer[2] Meena Devi | 9123456780 | meena@example.com",
              outcome(() -> a.registerCustomer("Meena Devi", "9123456780", "meena@example.com")));
        check("TC-02", "Black box / EP", "Phone has 9 digits", "ERROR: Phone must be exactly 10 digits.",
              outcome(() -> a.registerCustomer("X Y", "912345678", "x@example.com")));
        check("TC-03", "Black box / EP", "Email without @", "ERROR: Invalid email address.",
              outcome(() -> a.registerCustomer("X Y", "9123456789", "xexample.com")));
        check("TC-04", "Black box / EP", "Duplicate email", "ERROR: Email already registered.",
              outcome(() -> a.registerCustomer("Other", "9123456789", "ARUN@example.com")));
        check("TC-05", "Black box / EP", "Blank name", "ERROR: Customer name is required.",
              outcome(() -> a.registerCustomer("  ", "9123456789", "b@example.com")));

        // Package management
        check("TC-06", "Black box / EP", "Add package with price 0", "ERROR: Price must be positive.",
              outcome(() -> a.addPackage("Bad", "Goa", 3, 0, 5)));
        check("TC-07", "Black box / EP", "Add package with capacity 0", "ERROR: Capacity must be positive.",
              outcome(() -> a.addPackage("Bad", "Goa", 3, 5000, 0)));

        // Booking and amount calculation
        Booking b = a.bookPackage(1, 1, 3, trip, TODAY);
        check("TC-08", "Black box / functional", "3 travelers x Rs.12000", "36000.0", String.valueOf(b.getTotalAmount()));
        check("TC-09", "Black box / functional", "New booking status", "CONFIRMED", b.getStatus().name());
        check("TC-10", "Black box / functional", "Payment created with booking", "PENDING", a.getPayment(b.getBookingId()).getStatus().name());

        // Capacity - boundary value analysis (capacity 10, 3 already booked => 7 left)
        check("TC-11", "Black box / BVA", "Seats left after 3 of 10 booked", "7", outcome(() -> a.availableSeats(1, trip)));
        check("TC-12", "Black box / BVA", "Book exactly 7 (boundary = capacity)", "OK",
              outcome(() -> { a.bookPackage(1, 1, 7, trip, TODAY); return null; }));
        check("TC-13", "Black box / BVA", "Book 1 more when full (capacity+1)", "ERROR: Only 0 seat(s) left for Goa Beach Escape on 2026-12-20.",
              outcome(() -> a.bookPackage(1, 1, 1, trip, TODAY)));
        check("TC-14", "Black box / BVA", "Zero travelers", "ERROR: Number of travelers must be at least 1.",
              outcome(() -> a.bookPackage(1, 1, 0, trip, TODAY)));
        check("TC-15", "Black box / EP", "Same package, different date still free", "10",
              outcome(() -> a.availableSeats(1, LocalDate.of(2026, 12, 27))));
        check("TC-16", "Black box / EP", "Travel date equal to booking date", "ERROR: Travel date must be after the booking date.",
              outcome(() -> a.bookPackage(1, 1, 1, TODAY, TODAY)));
        check("TC-17", "Black box / EP", "Unknown customer id 99", "ERROR: Customer 99 not found.",
              outcome(() -> a.bookPackage(99, 1, 1, trip, TODAY)));

        // Payment
        check("TC-18", "Black box / functional", "Pay booking 1 by UPI", "PAID",
              outcome(() -> a.makePayment(1, "UPI", TODAY).getStatus()));
        check("TC-19", "Black box / functional", "Pay the same booking twice", "ERROR: Booking is already paid.",
              outcome(() -> a.makePayment(1, "CARD", TODAY)));
        check("TC-20", "Black box / EP", "Invalid payment method BITCOIN", "ERROR: Payment method must be one of " + TravelAgency.PAYMENT_METHODS,
              outcome(() -> a.makePayment(2, "BITCOIN", TODAY)));

        // Cancellation + refund boundaries (BVA on 15 / 7 days)
        check("TC-21", "Black box / BVA", "Refund % at 15 days before travel", "0.9", String.valueOf(TravelAgency.refundPercent(trip.minusDays(15), trip)));
        check("TC-22", "Black box / BVA", "Refund % at 14 days before travel", "0.5", String.valueOf(TravelAgency.refundPercent(trip.minusDays(14), trip)));
        check("TC-23", "Black box / BVA", "Refund % at 7 days before travel", "0.5", String.valueOf(TravelAgency.refundPercent(trip.minusDays(7), trip)));
        check("TC-24", "Black box / BVA", "Refund % at 6 days before travel", "0.0", String.valueOf(TravelAgency.refundPercent(trip.minusDays(6), trip)));
        check("TC-25", "Black box / functional", "Cancel paid booking 1 (34 days ahead)", "32400.0",
              outcome(() -> a.cancelBooking(1, "Change of plans", TODAY).getRefundAmount()));
        check("TC-26", "Black box / functional", "Seats freed after cancellation (10-7 booked)", "3",
              outcome(() -> a.availableSeats(1, trip)));
        check("TC-27", "Black box / functional", "Cancel same booking again", "ERROR: Booking 1 is already cancelled.",
              outcome(() -> a.cancelBooking(1, "again", TODAY)));
        check("TC-28", "Black box / functional", "Cancel unpaid booking 2 -> payment status", "CANCELLED",
              outcome(() -> { a.cancelBooking(2, "No funds", TODAY); return a.getPayment(2).getStatus(); }));
        check("TC-29", "Black box / functional", "Cannot pay for cancelled booking", "ERROR: Cannot pay for a cancelled booking.",
              outcome(() -> a.makePayment(2, "UPI", TODAY)));

        // Revenue report (white box: branch coverage of netRevenue PAID / REFUNDED / PENDING / CANCELLED)
        TravelAgency r = fresh();
        r.bookPackage(1, 1, 2, trip, TODAY); r.makePayment(1, "UPI", TODAY);          // PAID 24000
        r.bookPackage(1, 1, 1, trip, TODAY); r.makePayment(2, "CARD", TODAY);         // refunded
        r.cancelBooking(2, "x", TODAY);                                               // 90% refund -> keeps 1200
        r.bookPackage(1, 1, 1, trip, TODAY);                                          // PENDING -> 0
        check("TC-30", "White box / branch", "Net revenue: PAID + REFUNDED + PENDING", "25200.0", String.valueOf(r.totalRevenue()));
        check("TC-31", "White box / branch", "Destination popularity counts only confirmed", "{Goa=3}", String.valueOf(r.travelersByDestination()));

        System.out.println("\nSUMMARY: " + pass + " passed, " + fail + " failed, " + (pass + fail) + " total");
        if (fail > 0) System.exit(1);
    }
}
