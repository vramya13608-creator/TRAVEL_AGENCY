# Normalization (1NF → 2NF → 3NF)

## Unnormalized Relation

`BOOKING_RAW(booking_id, customer_id, customer_name, phone, email, package_id, package_name, destination_name, country, duration_days, price, max_capacity, num_travelers, booking_date, travel_date, status, total_amount, payment_id, payment_method, payment_status, payment_date, refund_amount)`

**Assumed raw input:** One customer may enter several phone numbers in one record, e.g. `phone = "9876543210, 9123456780"`.

## 1NF – Atomic Values

- Every column must contain a single value (atomic value).
- Multi-valued phone numbers are stored separately.
- One row represents one booking.
- Primary key: `booking_id`.

## 2NF – Remove Partial Dependencies

For explanation, consider the composite candidate key `(customer_id, package_id, travel_date)`.

- `customer_id → customer_name, phone, email`
- `package_id → package_name, destination_id, duration_days, price, max_capacity`

These attributes depend only on part of the composite key. Therefore, separate them into:

- `CUSTOMER(customer_id, customer_name, phone, email)`
- `TRAVEL_PACKAGE(package_id, package_name, destination_id, duration_days, price, max_capacity)`
- `BOOKING(booking_id, customer_id, package_id, num_travelers, booking_date, travel_date, status, total_amount)`

**Note:** The actual BOOKING table uses `booking_id` as its single-column primary key, so it has no partial dependency. The composite key is used only to explain the concept.

## 3NF – Remove Transitive Dependencies

- `package_id → destination_id → destination_name, country`
- Destination details depend on `destination_id`, not directly on the package. So create `DESTINATION(destination_id, destination_name, country)`.
- Payment details are separated into `PAYMENT(payment_id, booking_id, payment_method, payment_status, payment_date, amount)`.

## Final 3NF Schema

| Relation | Primary Key | Foreign Keys |
|---|---|---|
| CUSTOMER | customer_id | - |
| DESTINATION | destination_id | - |
| TRAVEL_PACKAGE | package_id | destination_id |
| BOOKING | booking_id | customer_id, package_id |
| PAYMENT | payment_id | booking_id |

Every non-key attribute depends on the key, the whole key, and nothing but the key.
