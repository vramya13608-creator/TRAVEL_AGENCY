# Testing Documentation

## 1. Testing Techniques Used

| **Technique** | **Where Applied** |
|---|---|
| Unit Testing | Each business rule of `TravelAgency` tested separately using `java/test/TestRunner.java`. |
| Black Box – Equivalence Partitioning | Valid and invalid phone numbers, email addresses, payment methods, and unknown IDs. |
| Black Box – Boundary Value Analysis | Package capacity (exactly full and capacity + 1), zero travelers, and travel date validation. |
| White Box – Branch Coverage | Testing different booking and payment status conditions. |
| Integration Testing | Checking database connectivity and data flow between the application modules (Section 4). |
| System / Acceptance Testing | Running the application demo and checking report and chart generation. |

## 2. Test Cases with Expected and Actual Results

Actual results are based on the available test execution records in `docs/test_results.txt`.

| **ID** | **Technique** | **Input / Scenario** | **Expected Result** | **Actual Result** | **Status** |
|---|---|---|---|---|---|
| TC-01 | Black Box / EP | Register valid customer | Customer registered successfully | Customer[2] Meena Devi – Phone: 9123456780, Email: meena@example.com | PASS |
| TC-02 | Black Box / EP | Phone has 9 digits | ERROR: Phone must be exactly 10 digits. | ERROR: Phone must be exactly 10 digits. | PASS |
| TC-03 | Black Box / EP | Email without @ | ERROR: Invalid email address. | ERROR: Invalid email address. | PASS |
| TC-04 | Black Box / EP | Duplicate email | ERROR: Email already registered. | ERROR: Email already registered. | PASS |
| TC-05 | Black Box / EP | Blank name | ERROR: Customer name is required. | ERROR: Customer name is required. | PASS |
| TC-06 | Black Box / EP | Add package with price 0 | ERROR: Price must be positive. | ERROR: Price must be positive. | PASS |
| TC-07 | Black Box / EP | Add package with capacity 0 | ERROR: Capacity must be positive. | ERROR: Capacity must be positive. | PASS |
| TC-08 | Black Box / Functional | 3 travelers × Rs. 12000 | 36000.0 | 36000.0 | PASS |
| TC-09 | Black Box / Functional | New booking status | CONFIRMED | CONFIRMED | PASS |
| TC-10 | Black Box / Functional | Payment created with booking | PENDING | PENDING | PASS |
| TC-11 | Black Box / BVA | Seats left after 3 of 10 booked | 7 | 7 | PASS |
| TC-12 | Black Box / BVA | Book exactly 7 (capacity boundary) | OK | OK | PASS |
| TC-13 | Black Box / BVA | Book 1 more when capacity is full | ERROR: Only 0 seat(s) left for Goa Beach Escape on 2026-12-20. | ERROR: Only 0 seat(s) left for Goa Beach Escape on 2026-12-20. | PASS |
| TC-14 | Black Box / BVA | Zero travelers | ERROR: Number of travelers must be at least 1. | ERROR: Number of travelers must be at least 1. | PASS |
| TC-15 | Black Box / EP | Same package, different date still free | 10 | 10 | PASS |
| TC-16 | Black Box / EP | Travel date equal to booking date | ERROR: Travel date must be after the booking date. | ERROR: Travel date must be after the booking date. | PASS |
| TC-17 | Black Box / EP | Unknown customer ID 99 | ERROR: Customer 99 not found. | ERROR: Customer 99 not found. | PASS |
| TC-18 | Black Box / Functional | Pay booking 1 by UPI | PAID | PAID | PASS |
| TC-19 | Black Box / Functional | Pay the same booking twice | ERROR: Booking is already paid. | ERROR: Booking is already paid. | PASS |
| TC-20 | Black Box / EP | Invalid payment method BITCOIN | ERROR: Payment method must be one of [CASH, NET_BANKING, UPI, CARD]. | ERROR: Payment method must be one of [CASH, NET_BANKING, UPI, CARD]. | PASS |
| TC-21 | Black Box / Functional | Cannot pay for cancelled booking | ERROR: Cannot pay for a cancelled booking. | ERROR: Cannot pay for a cancelled booking. | PASS |
| TC-22 | White Box / Branch | Destination popularity counts only confirmed bookings | {Goa=3} | {Goa=3} | PASS |

**Summary of the test cases listed above: 22 passed, 0 failed, 22 total.**

## 3. How to Re-run

Run the following command from the project directory:

```bash
java -cp bin TestRunner
```

## 4. Integration / System Test Checklist

| **ID** | **Scenario** | **Expected Result** | **Result** |
|---|---|---|---|
| IT-01 | Run `sql/schema.sql` and `sql/sample_data.sql` in pgAdmin. | Five tables created and sample rows inserted without errors. | Verify in PostgreSQL using pgAdmin. |
| IT-02 | Run `python python/setup_database.py`. | Tables rebuilt and approximately 500 bookings loaded. | PASS on SQLite test copy (120 customers, 12 packages, 517 bookings); PostgreSQL verification pending. |
| IT-03 | Run `python python/analysis.py`. | 12 duplicates removed, 18 missing methods filled, and 5 PNG charts and CSV files generated. | PASS on SQLite test copy; PostgreSQL verification pending. |
| IT-04 | Run Java application with PostgreSQL JDBC driver, add data, restart, and list bookings. | Data persists and can be loaded again. | Verify on PostgreSQL. |
| IT-05 | Run Java application in `demo` mode. | Booking, capacity rejection, cancellation seat updates, and reports are displayed. | PASS. |
| IT-06 | Run `python python/server.py`, open the website, create a booking, and refresh. | Booking remains available after refreshing the website. | API PASS on SQLite test copy; PostgreSQL verification pending. |
| IT-07 | Submit a state that exceeds departure capacity through `/api/state`. | HTTP 409 response and no invalid state stored. | PASS. |
| IT-08 | Run dashboard logic and all six pages using the Node smoke test. | No errors, NaN values, or undefined values. | PASS (20 checks). |
