package travelagency;

import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;

/**
 * Optional JDBC persistence for PostgreSQL. Needs the postgresql JDBC driver jar on the classpath.
 * If the driver or file is unavailable, tryOpen() returns null and the app runs in memory.
 */
public class Database implements AutoCloseable {
    private final Connection conn;

    private Database(Connection conn) { this.conn = conn; }

    public static Database tryOpen(String url, String user, String password, String schemaFile) {
        try {
            Connection c = DriverManager.getConnection(url, user, password);
            Database d = new Database(c);
            d.runScript(Paths.get(schemaFile));
            return d;
        } catch (Exception e) {
            System.err.println("[DB] " + e.getMessage());
            return null;
        }
    }

    private void runScript(Path file) throws Exception {
        String sql = Files.readString(file);
        try (Statement st = conn.createStatement()) {
            for (String part : sql.split(";")) {
                String s = part.replaceAll("(?m)^\\s*--.*$", "").trim();
                if (!s.isEmpty()) st.execute(s);
            }
        }
    }

    private int destinationId(String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO destination(destination_name) VALUES (?) ON CONFLICT (destination_name) DO NOTHING")) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT destination_id FROM destination WHERE destination_name=?")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public void saveCustomer(Customer c) {
        run("INSERT INTO customer(customer_id,name,phone,email) VALUES (?,?,?,?) ON CONFLICT (customer_id) DO UPDATE SET name=EXCLUDED.name, phone=EXCLUDED.phone, email=EXCLUDED.email",
                c.getCustomerId(), c.getName(), c.getPhone(), c.getEmail());
    }

    public void savePackage(TravelPackage p) {
        try {
            run("INSERT INTO travel_package(package_id,package_name,destination_id,duration_days,price,max_capacity) VALUES (?,?,?,?,?,?) ON CONFLICT (package_id) DO UPDATE SET package_name=EXCLUDED.package_name, destination_id=EXCLUDED.destination_id, duration_days=EXCLUDED.duration_days, price=EXCLUDED.price, max_capacity=EXCLUDED.max_capacity",
                    p.getPackageId(), p.getPackageName(), destinationId(p.getDestination()),
                    p.getDurationDays(), p.getPrice(), p.getMaxCapacity());
        } catch (SQLException e) { warn(e); }
    }

    public void saveBooking(Booking b) {
        run("INSERT INTO booking(booking_id,customer_id,package_id,num_travelers,booking_date,travel_date,status,total_amount,cancellation_date,cancellation_reason,refund_amount) VALUES (?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (booking_id) DO UPDATE SET status=EXCLUDED.status, cancellation_date=EXCLUDED.cancellation_date, cancellation_reason=EXCLUDED.cancellation_reason, refund_amount=EXCLUDED.refund_amount",
                b.getBookingId(), b.getCustomer().getCustomerId(), b.getTravelPackage().getPackageId(),
                b.getNumTravelers(), b.getBookingDate(), b.getTravelDate(),
                b.getStatus().name(), b.getTotalAmount(),
                b.getCancellationDate(),
                b.getCancellationReason(), b.getRefundAmount());
    }

    public void savePayment(Payment p) {
        run("INSERT INTO payment(payment_id,booking_id,amount,payment_method,payment_status,payment_date,refund_amount) VALUES (?,?,?,?,?,?,?) ON CONFLICT (payment_id) DO UPDATE SET payment_method=EXCLUDED.payment_method, payment_status=EXCLUDED.payment_status, payment_date=EXCLUDED.payment_date, refund_amount=EXCLUDED.refund_amount",
                p.getPaymentId(), p.getBookingId(), p.getAmount(), p.getMethod(), p.getStatus().name(),
                p.getPaymentDate(), p.getRefundAmount());
    }

    private void run(String sql, Object... args) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) ps.setNull(i + 1, Types.NULL); else ps.setObject(i + 1, args[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) { warn(e); }
    }

    private void warn(SQLException e) { System.err.println("[DB warning] " + e.getMessage()); }

    /** Load saved rows into the agency so work continues where it stopped. */
    public void load(TravelAgency agency) throws SQLException {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM customer ORDER BY customer_id");
            while (rs.next()) agency.restoreCustomer(new Customer(rs.getInt("customer_id"),
                    rs.getString("name"), rs.getString("phone"), rs.getString("email")));
            rs = st.executeQuery("SELECT p.*, d.destination_name FROM travel_package p JOIN destination d USING(destination_id) ORDER BY package_id");
            while (rs.next()) agency.restorePackage(new TravelPackage(rs.getInt("package_id"), rs.getString("package_name"),
                    rs.getString("destination_name"), rs.getInt("duration_days"), rs.getDouble("price"), rs.getInt("max_capacity")));
            rs = st.executeQuery("SELECT * FROM booking ORDER BY booking_id");
            while (rs.next()) {
                LocalDate cd = rs.getObject("cancellation_date", LocalDate.class);
                agency.restoreBooking(new Booking(rs.getInt("booking_id"),
                        agency.getCustomerOrNull(rs.getInt("customer_id")), agency.getPackageOrNull(rs.getInt("package_id")),
                        rs.getInt("num_travelers"), rs.getObject("booking_date", LocalDate.class),
                        rs.getObject("travel_date", LocalDate.class), rs.getDouble("total_amount"),
                        Booking.Status.valueOf(rs.getString("status")), cd,
                        rs.getString("cancellation_reason"), rs.getDouble("refund_amount")));
            }
            rs = st.executeQuery("SELECT * FROM payment ORDER BY payment_id");
            while (rs.next()) {
                LocalDate pd = rs.getObject("payment_date", LocalDate.class);
                agency.restorePayment(new Payment(rs.getInt("payment_id"), rs.getInt("booking_id"), rs.getDouble("amount"),
                        rs.getString("payment_method"), Payment.Status.valueOf(rs.getString("payment_status")),
                        pd, rs.getDouble("refund_amount")));
            }
        }
    }

    @Override
    public void close() throws SQLException { conn.close(); }
}
