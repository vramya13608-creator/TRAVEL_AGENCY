# Normalization (1NF -> 2NF -> 3NF)

## Unnormalized relation
BOOKING_RAW(booking_id, customer_id, customer_name, phone, email, package_id, package_name, destination_name, country, duration_days, price, max_capacity, num_travelers, booking_date, travel_date, status, total_amount, payment_id, payment_method, payment_status, payment_date, refund_amount)

Assumed raw input: one customer may enter several phone numbers/packages in one record, e.g. `phone = "9876543210, 9123456780"`.

## 1NF - atomic values, no repeating groups
- Every column holds a single value (multi-valued phone numbers split to separate rows/atomic column; one row per booking).
- Primary key: `booking_id`.

## 2NF - remove partial dependencies
Key is a single column (`booking_id`), so partial dependency appears only if a composite key is used. Using the natural composite key (customer_id, package_id, travel_date) for booking:
- customer_id -> customer_name, phone, email
- package_id -> package_name, destination_name, duration_days, price, max_capacity

These depend on only part of the key, so they move out:
- CUSTOMER(customer_id, name, phone, email)
- TRAVEL_PACKAGE(package_id, package_name, destination_name, duration_days, price, max_capacity)
- BOOKING(booking_id, customer_id, package_id, travel_date, ...)

## 3NF - remove transitive dependencies
- package_id -> destination_name -> country  (country depends on destination, not on the package) -> split into DESTINATION(destination_id, destination_name, country).
- booking_id -> payment_id -> payment_method, payment_status, payment_date: payment facts describe the payment, not the booking -> PAYMENT(payment_id, booking_id, ...).

## Final 3NF schema
| Relation | Primary key | Foreign keys |
|---|---|---|
| CUSTOMER | customer_id | - |
| DESTINATION | destination_id | - |
| TRAVEL_PACKAGE | package_id | destination_id -> DESTINATION |
| BOOKING | booking_id | customer_id -> CUSTOMER, package_id -> TRAVEL_PACKAGE |
| PAYMENT | payment_id | booking_id -> BOOKING |

Every non-key attribute depends on "the key, the whole key, and nothing but the key".

**Design note:** `booking.total_amount` is stored (price x travelers at the time of booking) deliberately, so later price changes do not alter historical bookings. It is a snapshot, not redundant derived data.
