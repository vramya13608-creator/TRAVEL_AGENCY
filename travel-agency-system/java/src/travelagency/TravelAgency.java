package travelagency;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

/** Core business logic: registration, packages, booking, capacity, payment, cancellation, reports. */
public class TravelAgency {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^\\d{10}$");
    public static final Set<String> PAYMENT_METHODS = Set.of("UPI", "CARD", "CASH", "NET_BANKING");

    private final Map<Integer, Customer> customers = new LinkedHashMap<>();
    private final Map<Integer, TravelPackage> packages = new LinkedHashMap<>();
    private final Map<Integer, Booking> bookings = new LinkedHashMap<>();
    private final Map<Integer, Payment> payments = new LinkedHashMap<>();   // keyed by bookingId
    private int nextCustomerId = 1, nextPackageId = 1, nextBookingId = 1, nextPaymentId = 1;
    private Database db;                                                    // optional persistence

    public void setDatabase(Database db) { this.db = db; }

    // ---------- Customer registration ----------
    public Customer registerCustomer(String name, String phone, String email) throws BookingException {
        if (name == null || name.isBlank()) throw new BookingException("Customer name is required.");
        if (phone == null || !PHONE.matcher(phone).matches()) throw new BookingException("Phone must be exactly 10 digits.");
        if (email == null || !EMAIL.matcher(email).matches()) throw new BookingException("Invalid email address.");
        for (Customer c : customers.values())
            if (c.getEmail().equalsIgnoreCase(email)) throw new BookingException("Email already registered.");
        Customer c = new Customer(nextCustomerId++, name.trim(), phone, email.toLowerCase());
        customers.put(c.getCustomerId(), c);
        if (db != null) db.saveCustomer(c);
        return c;
    }

    // ---------- Package management ----------
    public TravelPackage addPackage(String packageName, String destination, int durationDays,
                                    double price, int maxCapacity) throws BookingException {
        if (packageName == null || packageName.isBlank() || destination == null || destination.isBlank())
            throw new BookingException("Package name and destination are required.");
        if (durationDays <= 0) throw new BookingException("Duration must be positive.");
        if (price <= 0) throw new BookingException("Price must be positive.");
        if (maxCapacity <= 0) throw new BookingException("Capacity must be positive.");
        TravelPackage p = new TravelPackage(nextPackageId++, packageName.trim(), destination.trim(),
                durationDays, price, maxCapacity);
        packages.put(p.getPackageId(), p);
        if (db != null) db.savePackage(p);
        return p;
    }

    public Customer getCustomer(int id) throws BookingException {
        Customer c = customers.get(id);
        if (c == null) throw new BookingException("Customer " + id + " not found.");
        return c;
    }

    public TravelPackage getPackage(int id) throws BookingException {
        TravelPackage p = packages.get(id);
        if (p == null) throw new BookingException("Package " + id + " not found.");
        return p;
    }

    public Booking getBooking(int id) throws BookingException {
        Booking b = bookings.get(id);
        if (b == null) throw new BookingException("Booking " + id + " not found.");
        return b;
    }

    // ---------- Availability ----------
    /** Seats left on a package for one departure date (cancelled bookings free their seats). */
    public int availableSeats(int packageId, LocalDate travelDate) throws BookingException {
        TravelPackage p = getPackage(packageId);
        int taken = 0;
        for (Booking b : bookings.values())
            if (b.isConfirmed() && b.getTravelPackage().getPackageId() == packageId
                    && b.getTravelDate().equals(travelDate))
                taken += b.getNumTravelers();
        return p.getMaxCapacity() - taken;
    }

    // ---------- Booking ----------
    public double calculateAmount(TravelPackage p, int travelers) { return p.getPrice() * travelers; }

    public Booking bookPackage(int customerId, int packageId, int travelers,
                               LocalDate travelDate, LocalDate bookingDate) throws BookingException {
        Customer c = getCustomer(customerId);
        TravelPackage p = getPackage(packageId);
        if (travelers <= 0) throw new BookingException("Number of travelers must be at least 1.");
        if (!travelDate.isAfter(bookingDate)) throw new BookingException("Travel date must be after the booking date.");
        int left = availableSeats(packageId, travelDate);
        if (travelers > left)
            throw new BookingException("Only " + left + " seat(s) left for " + p.getPackageName() + " on " + travelDate + ".");
        Booking b = new Booking(nextBookingId++, c, p, travelers, bookingDate, travelDate, calculateAmount(p, travelers));
        bookings.put(b.getBookingId(), b);
        Payment pay = new Payment(nextPaymentId++, b.getBookingId(), b.getTotalAmount());
        payments.put(b.getBookingId(), pay);
        if (db != null) { db.saveBooking(b); db.savePayment(pay); }
        return b;
    }

    // ---------- Payment ----------
    public Payment makePayment(int bookingId, String method, LocalDate date) throws BookingException {
        Booking b = getBooking(bookingId);
        if (!b.isConfirmed()) throw new BookingException("Cannot pay for a cancelled booking.");
        if (method == null || !PAYMENT_METHODS.contains(method.toUpperCase()))
            throw new BookingException("Payment method must be one of " + PAYMENT_METHODS);
        Payment pay = payments.get(bookingId);
        if (pay.getStatus() == Payment.Status.PAID) throw new BookingException("Booking is already paid.");
        pay.markPaid(method.toUpperCase(), date);
        if (db != null) db.savePayment(pay);
        return pay;
    }

    public Payment getPayment(int bookingId) { return payments.get(bookingId); }

    // ---------- Cancellation ----------
    /** Refund policy: 15+ days before travel = 90%, 7-14 days = 50%, under 7 days = 0%. */
    public static double refundPercent(LocalDate cancelDate, LocalDate travelDate) {
        long days = ChronoUnit.DAYS.between(cancelDate, travelDate);
        if (days >= 15) return 0.90;
        if (days >= 7) return 0.50;
        return 0.0;
    }

    public Booking cancelBooking(int bookingId, String reason, LocalDate cancelDate) throws BookingException {
        Booking b = getBooking(bookingId);
        if (!b.isConfirmed()) throw new BookingException("Booking " + bookingId + " is already cancelled.");
        if (cancelDate.isAfter(b.getTravelDate())) throw new BookingException("Cannot cancel after the travel date.");
        Payment pay = payments.get(bookingId);
        double refund = 0;
        if (pay.getStatus() == Payment.Status.PAID) {
            refund = Math.round(pay.getAmount() * refundPercent(cancelDate, b.getTravelDate()) * 100.0) / 100.0;
            pay.refund(refund);
        } else {
            pay.cancel();
        }
        b.cancel(cancelDate, reason == null || reason.isBlank() ? "Not specified" : reason, refund);
        if (db != null) { db.saveBooking(b); db.savePayment(pay); }
        return b;
    }

    // ---------- Reports ----------
    public void printBookingDetails(int bookingId) throws BookingException {
        Booking b = getBooking(bookingId);
        System.out.println(b);
        System.out.println("   " + payments.get(bookingId));
    }

    public void printAllBookings() {
        if (bookings.isEmpty()) { System.out.println("No bookings yet."); return; }
        for (Booking b : bookings.values()) System.out.println(b);
    }

    public Map<String, Integer> travelersByDestination() {
        Map<String, Integer> m = new TreeMap<>();
        for (Booking b : bookings.values())
            if (b.isConfirmed()) m.merge(b.getTravelPackage().getDestination(), b.getNumTravelers(), Integer::sum);
        return m;
    }

    public Map<String, Double> revenueByPackage() {
        Map<String, Double> m = new TreeMap<>();
        for (Booking b : bookings.values())
            m.merge(b.getTravelPackage().getPackageName(), payments.get(b.getBookingId()).netRevenue(), Double::sum);
        return m;
    }

    public double totalRevenue() {
        double t = 0;
        for (Payment p : payments.values()) t += p.netRevenue();
        return t;
    }

    public void printReports() {
        System.out.println("\n=== Package Booking Report ===");
        for (TravelPackage p : packages.values()) {
            int count = 0, travelers = 0, cancelled = 0;
            for (Booking b : bookings.values()) {
                if (b.getTravelPackage().getPackageId() != p.getPackageId()) continue;
                if (b.isConfirmed()) { count++; travelers += b.getNumTravelers(); } else cancelled++;
            }
            System.out.printf("%-28s confirmed=%d travelers=%d cancelled=%d%n", p.getPackageName(), count, travelers, cancelled);
        }
        System.out.println("\n=== Destination Popularity (confirmed travelers) ===");
        travelersByDestination().entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .forEach(e -> System.out.printf("%-20s %d%n", e.getKey(), e.getValue()));
        System.out.println("\n=== Booking Revenue (net of refunds) ===");
        revenueByPackage().forEach((k, v) -> System.out.printf("%-28s Rs.%.2f%n", k, v));
        System.out.printf("TOTAL REVENUE: Rs.%.2f%n", totalRevenue());
    }

    public Collection<Customer> getCustomers() { return customers.values(); }
    public Collection<TravelPackage> getPackages() { return packages.values(); }
    public Collection<Booking> getBookings() { return bookings.values(); }

    Customer getCustomerOrNull(int id) { return customers.get(id); }
    TravelPackage getPackageOrNull(int id) { return packages.get(id); }

    // ---------- Used by Database.load() to restore saved state ----------
    void restoreCustomer(Customer c) { customers.put(c.getCustomerId(), c); nextCustomerId = Math.max(nextCustomerId, c.getCustomerId() + 1); }
    void restorePackage(TravelPackage p) { packages.put(p.getPackageId(), p); nextPackageId = Math.max(nextPackageId, p.getPackageId() + 1); }
    void restoreBooking(Booking b) { bookings.put(b.getBookingId(), b); nextBookingId = Math.max(nextBookingId, b.getBookingId() + 1); }
    void restorePayment(Payment p) { payments.put(p.getBookingId(), p); nextPaymentId = Math.max(nextPaymentId, p.getPaymentId() + 1); }
}
