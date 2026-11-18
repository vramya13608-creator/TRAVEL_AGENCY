-- Q1. Full booking details (customer + package + destination + payment)
SELECT b.booking_id, c.name AS customer, p.package_name, d.destination_name,
       b.num_travelers, b.booking_date, b.travel_date, b.status,
       b.total_amount, pay.payment_status
FROM booking b
JOIN customer c        ON c.customer_id = b.customer_id
JOIN travel_package p  ON p.package_id = b.package_id
JOIN destination d     ON d.destination_id = p.destination_id
JOIN payment pay       ON pay.booking_id = b.booking_id
ORDER BY b.booking_id;

-- Q2. Seats still available per package and departure date
SELECT p.package_id, p.package_name, b.travel_date,
       p.max_capacity - SUM(b.num_travelers) AS seats_left
FROM travel_package p
JOIN booking b ON b.package_id = p.package_id AND b.status = 'CONFIRMED'
GROUP BY p.package_id, b.travel_date
ORDER BY p.package_id, b.travel_date;

-- Q3. Departures that are overbooked (should return zero rows)
SELECT p.package_id, b.travel_date, SUM(b.num_travelers) AS booked, p.max_capacity
FROM booking b JOIN travel_package p ON p.package_id = b.package_id
WHERE b.status = 'CONFIRMED'
GROUP BY p.package_id, b.travel_date
HAVING SUM(b.num_travelers) > p.max_capacity;

-- Q4. Destination popularity (confirmed travelers)
SELECT d.destination_name, COUNT(*) AS bookings, SUM(b.num_travelers) AS travelers
FROM booking b
JOIN travel_package p ON p.package_id = b.package_id
JOIN destination d    ON d.destination_id = p.destination_id
WHERE b.status = 'CONFIRMED'
GROUP BY d.destination_name
ORDER BY travelers DESC;

-- Q5. Revenue per package (net of refunds)
SELECT p.package_name,
       SUM(CASE WHEN pay.payment_status = 'PAID' THEN pay.amount
                WHEN pay.payment_status = 'REFUNDED' THEN pay.amount - pay.refund_amount
                ELSE 0 END) AS net_revenue
FROM payment pay
JOIN booking b        ON b.booking_id = pay.booking_id
JOIN travel_package p ON p.package_id = b.package_id
GROUP BY p.package_name
ORDER BY net_revenue DESC;

-- Q6. Monthly booking trend (by travel month)
SELECT strftime('%Y-%m', travel_date) AS travel_month, COUNT(*) AS bookings
FROM booking WHERE status = 'CONFIRMED'
GROUP BY travel_month ORDER BY travel_month;

-- Q7. Cancellation report
SELECT b.booking_id, c.name, b.cancellation_date, b.cancellation_reason, b.refund_amount
FROM booking b JOIN customer c ON c.customer_id = b.customer_id
WHERE b.status = 'CANCELLED';

-- Q8. Customers with pending payments
SELECT c.name, c.phone, b.booking_id, pay.amount
FROM payment pay
JOIN booking b  ON b.booking_id = pay.booking_id
JOIN customer c ON c.customer_id = b.customer_id
WHERE pay.payment_status = 'PENDING' AND b.status = 'CONFIRMED';
