# Testing Documentation

## 1. Testing techniques used
| Technique | Where applied |
|---|---|
| Unit testing | Each business rule of `TravelAgency` tested in isolation (`java/test/TestRunner.java`) |
| Black box - equivalence partitioning | Valid/invalid phone, email, payment method, unknown IDs |
| Black box - boundary value analysis | Capacity (exactly full, capacity+1), refund thresholds (15, 14, 7, 6 days), zero travelers |
| White box - branch coverage | Net revenue branches PAID / REFUNDED / PENDING / CANCELLED |
| Integration testing | Java -> PostgreSQL -> Python flow (checklist in section 4) |
| System / acceptance testing | End-to-end `demo` run and chart generation |

## 2. Test cases with expected and actual results
Actual results are taken from a real run of `TestRunner` (`docs/test_results.txt`).

| ID | Technique | Input / scenario | Expected result | Actual result | Status |
|---|---|---|---|---|---|
| TC-01 | Black box / EP | Register valid customer | Customer[2] Meena Devi | 9123456780 | meena@example.com | Customer[2] Meena Devi | 9123456780 | meena@example.com | PASS |
| TC-02 | Black box / EP | Phone has 9 digits | ERROR: Phone must be exactly 10 digits. | ERROR: Phone must be exactly 10 digits. | PASS |
| TC-03 | Black box / EP | Email without @ | ERROR: Invalid email address. | ERROR: Invalid email address. | PASS |
| TC-04 | Black box / EP | Duplicate email | ERROR: Email already registered. | ERROR: Email already registered. | PASS |
| TC-05 | Black box / EP | Blank name | ERROR: Customer name is required. | ERROR: Customer name is required. | PASS |
| TC-06 | Black box / EP | Add package with price 0 | ERROR: Price must be positive. | ERROR: Price must be positive. | PASS |
| TC-07 | Black box / EP | Add package with capacity 0 | ERROR: Capacity must be positive. | ERROR: Capacity must be positive. | PASS |
| TC-08 | Black box / functional | 3 travelers x Rs.12000 | 36000.0 | 36000.0 | PASS |
| TC-09 | Black box / functional | New booking status | CONFIRMED | CONFIRMED | PASS |
| TC-10 | Black box / functional | Payment created with booking | PENDING | PENDING | PASS |
| TC-11 | Black box / BVA | Seats left after 3 of 10 booked | 7 | 7 | PASS |
| TC-12 | Black box / BVA | Book exactly 7 (boundary = capacity) | OK | OK | PASS |
| TC-13 | Black box / BVA | Book 1 more when full (capacity+1) | ERROR: Only 0 seat(s) left for Goa Beach Escape on 2026-12-20. | ERROR: Only 0 seat(s) left for Goa Beach Escape on 2026-12-20. | PASS |
| TC-14 | Black box / BVA | Zero travelers | ERROR: Number of travelers must be at least 1. | ERROR: Number of travelers must be at least 1. | PASS |
| TC-15 | Black box / EP | Same package, different date still free | 10 | 10 | PASS |
| TC-16 | Black box / EP | Travel date equal to booking date | ERROR: Travel date must be after the booking date. | ERROR: Travel date must be after the booking date. | PASS |
| TC-17 | Black box / EP | Unknown customer id 99 | ERROR: Customer 99 not found. | ERROR: Customer 99 not found. | PASS |
| TC-18 | Black box / functional | Pay booking 1 by UPI | PAID | PAID | PASS |
| TC-19 | Black box / functional | Pay the same booking twice | ERROR: Booking is already paid. | ERROR: Booking is already paid. | PASS |
| TC-20 | Black box / EP | Invalid payment method BITCOIN | ERROR: Payment method must be one of [CASH, NET_BANKING, UPI, CARD] | ERROR: Payment method must be one of [CASH, NET_BANKING, UPI, CARD] | PASS |
| TC-21 | Black box / BVA | Refund % at 15 days before travel | 0.9 | 0.9 | PASS |
| TC-22 | Black box / BVA | Refund % at 14 days before travel | 0.5 | 0.5 | PASS |
| TC-23 | Black box / BVA | Refund % at 7 days before travel | 0.5 | 0.5 | PASS |
| TC-24 | Black box / BVA | Refund % at 6 days before travel | 0.0 | 0.0 | PASS |
| TC-25 | Black box / functional | Cancel paid booking 1 (34 days ahead) | 32400.0 | 32400.0 | PASS |
| TC-26 | Black box / functional | Seats freed after cancellation (10-7 booked) | 3 | 3 | PASS |
| TC-27 | Black box / functional | Cancel same booking again | ERROR: Booking 1 is already cancelled. | ERROR: Booking 1 is already cancelled. | PASS |
| TC-28 | Black box / functional | Cancel unpaid booking 2 -> payment status | CANCELLED | CANCELLED | PASS |
| TC-29 | Black box / functional | Cannot pay for cancelled booking | ERROR: Cannot pay for a cancelled booking. | ERROR: Cannot pay for a cancelled booking. | PASS |
| TC-30 | White box / branch | Net revenue: PAID + REFUNDED + PENDING | 25200.0 | 25200.0 | PASS |
| TC-31 | White box / branch | Destination popularity counts only confirmed | {Goa=3} | {Goa=3} | PASS |

**Summary:** 31 passed, 0 failed, 31 total

## 3. How to re-run
```bash
java -cp bin TestRunner
```

## 4. Integration / system test checklist
| ID | Scenario | Expected | Result |
|---|---|---|---|
| IT-01 | Run `sql/schema.sql` then `sql/sample_data.sql` in pgAdmin | 5 tables created, sample rows inserted, no errors | Verify in pgAdmin (written for PostgreSQL; the build sandbox had no PostgreSQL server) |
| IT-02 | `python python/setup_database.py` | Tables rebuilt, about 500 bookings loaded | PASS on the SQLite test copy (120 customers, 12 packages, 517 bookings); verify on PostgreSQL |
| IT-03 | `python python/analysis.py` | 12 duplicates removed, 18 missing methods filled, 5 PNG charts and CSVs written | PASS (same pipeline, SQLite test copy); verify on PostgreSQL |
| IT-04 | Java app with the PostgreSQL JDBC jar: add data, restart, list bookings | Data persists and reloads | Verify on your machine |
| IT-05 | Java `demo` mode | Booking, capacity rejection, cancellation freed seats, reports printed | PASS |
| IT-06 | `python python/server.py`, open the site, make a booking, refresh | Booking is still there (loaded from the database) | API PASS over HTTP on the SQLite test copy; verify on PostgreSQL |
| IT-07 | Save a state that overbooks a departure via `/api/state` | HTTP 409, nothing stored | PASS |
| IT-08 | Dashboard logic and all six pages (Node smoke test, 20 checks) | No errors, no NaN/undefined | PASS |
