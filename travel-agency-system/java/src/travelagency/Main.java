package travelagency;

import java.time.LocalDate;
import java.util.Scanner;

/** Console application. Run with "demo" to execute a scripted walkthrough. */
public class Main {
    static final Scanner in = new Scanner(System.in);
    // Override with environment variables PG_URL, PG_USER, PG_PASSWORD
    static final String DB_URL = System.getenv().getOrDefault("PG_URL", "jdbc:postgresql://localhost:5432/travel_agency");
    static final String DB_USER = System.getenv().getOrDefault("PG_USER", "postgres");
    static final String DB_PASSWORD = System.getenv().getOrDefault("PG_PASSWORD", "");
    static final String SCHEMA = "sql/schema.sql";

    public static void main(String[] args) throws Exception {
        TravelAgency agency = new TravelAgency();
        Database db = Database.tryOpen(DB_URL, DB_USER, DB_PASSWORD, SCHEMA);
        if (db != null) {
            db.load(agency);
            agency.setDatabase(db);
            System.out.println("[Connected to PostgreSQL: " + DB_URL + "]");
        } else {
            System.out.println("[PostgreSQL not reachable (driver jar / credentials?) - running in memory only]");
        }
        if (args.length > 0 && args[0].equals("demo")) { demo(agency); return; }
        menu(agency);
        if (db != null) db.close();
    }

    static void menu(TravelAgency agency) {
        while (true) {
            System.out.println("\n===== TRAVEL AGENCY =====");
            System.out.println("1 Register customer   2 Add package    3 List packages");
            System.out.println("4 Check availability  5 Book package   6 Make payment");
            System.out.println("7 Cancel booking      8 Booking details 9 All bookings");
            System.out.println("10 Reports            0 Exit");
            System.out.print("Choice: ");
            String ch = in.nextLine().trim();
            try {
                switch (ch) {
                    case "1" -> {
                        Customer c = agency.registerCustomer(ask("Name"), ask("Phone (10 digits)"), ask("Email"));
                        System.out.println("Registered: " + c);
                    }
                    case "2" -> {
                        TravelPackage p = agency.addPackage(ask("Package name"), ask("Destination"),
                                Integer.parseInt(ask("Duration (days)")), Double.parseDouble(ask("Price per person")),
                                Integer.parseInt(ask("Max capacity per departure")));
                        System.out.println("Added: " + p);
                    }
                    case "3" -> agency.getPackages().forEach(System.out::println);
                    case "4" -> {
                        int left = agency.availableSeats(Integer.parseInt(ask("Package ID")), LocalDate.parse(ask("Travel date (YYYY-MM-DD)")));
                        System.out.println("Seats available: " + left);
                    }
                    case "5" -> {
                        Booking b = agency.bookPackage(Integer.parseInt(ask("Customer ID")), Integer.parseInt(ask("Package ID")),
                                Integer.parseInt(ask("Travelers")), LocalDate.parse(ask("Travel date (YYYY-MM-DD)")), LocalDate.now());
                        System.out.printf("Booked! ID %d, amount Rs.%.2f (payment pending)%n", b.getBookingId(), b.getTotalAmount());
                    }
                    case "6" -> System.out.println(agency.makePayment(Integer.parseInt(ask("Booking ID")),
                            ask("Method (UPI/CARD/CASH/NET_BANKING)"), LocalDate.now()));
                    case "7" -> System.out.println(agency.cancelBooking(Integer.parseInt(ask("Booking ID")),
                            ask("Reason"), LocalDate.now()));
                    case "8" -> agency.printBookingDetails(Integer.parseInt(ask("Booking ID")));
                    case "9" -> agency.printAllBookings();
                    case "10" -> agency.printReports();
                    case "0" -> { System.out.println("Goodbye."); return; }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (BookingException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
        }
    }

    static String ask(String label) { System.out.print(label + ": "); return in.nextLine().trim(); }

    static void demo(TravelAgency a) throws BookingException {
        LocalDate today = LocalDate.of(2026, 11, 16);
        Customer c1 = a.registerCustomer("Arun Kumar", "9876543210", "arun.demo@example.com");
        Customer c2 = a.registerCustomer("Meena Devi", "9123456780", "meena.demo@example.com");
        TravelPackage goa = a.addPackage("Goa Beach Escape", "Goa", 4, 12000, 10);
        TravelPackage ooty = a.addPackage("Ooty Hill Retreat", "Ooty", 3, 8000, 6);
        System.out.println(c1 + "\n" + c2 + "\n" + goa + "\n" + ooty);

        Booking b1 = a.bookPackage(c1.getCustomerId(), goa.getPackageId(), 4, LocalDate.of(2026, 12, 20), today);
        a.makePayment(b1.getBookingId(), "UPI", today);
        Booking b2 = a.bookPackage(c2.getCustomerId(), ooty.getPackageId(), 5, LocalDate.of(2026, 12, 5), today);
        System.out.println("\nSeats left on Ooty 5 Dec: " + a.availableSeats(ooty.getPackageId(), LocalDate.of(2026, 12, 5)));
        try {
            a.bookPackage(c1.getCustomerId(), ooty.getPackageId(), 2, LocalDate.of(2026, 12, 5), today);
        } catch (BookingException e) { System.out.println("Rejected: " + e.getMessage()); }

        a.makePayment(b2.getBookingId(), "CARD", today);
        a.cancelBooking(b2.getBookingId(), "Change of plans", today);
        System.out.println("\nSeats left on Ooty 5 Dec after cancellation: " + a.availableSeats(ooty.getPackageId(), LocalDate.of(2026, 12, 5)));
        System.out.println();
        a.printAllBookings();
        a.printReports();
    }
}
