"""Create data/travel_agency.db from the SQL scripts, then add bulk synthetic bookings."""
import random
from datetime import date, timedelta
from pathlib import Path
import db
from db import q, run_script

ROOT = Path(__file__).resolve().parent.parent

# month -> relative demand (1..12), per destination: models tourism seasons
SEASON = {
    "Goa":         [9, 8, 5, 2, 1, 1, 1, 2, 3, 6, 9, 10],
    "Ooty":        [3, 4, 8, 10, 10, 4, 3, 3, 4, 5, 3, 4],
    "Munnar":      [4, 5, 5, 4, 3, 7, 9, 9, 6, 5, 4, 6],
    "Manali":      [4, 3, 3, 8, 10, 10, 5, 3, 3, 4, 3, 5],
    "Jaipur":      [8, 8, 5, 2, 1, 1, 1, 2, 4, 8, 10, 10],
    "Kodaikanal":  [3, 4, 7, 9, 10, 5, 3, 3, 4, 4, 3, 4],
    "Andaman":     [7, 8, 8, 6, 3, 1, 1, 2, 3, 6, 8, 10],
    "Darjeeling":  [3, 4, 8, 10, 8, 3, 2, 2, 5, 9, 8, 4],
}
NEW_DEST = ["Kodaikanal", "Andaman", "Darjeeling"]
NEW_PACKAGES = [  # name, destination, days, price, capacity
    ("Kodaikanal Mist Trail", "Kodaikanal", 3, 7500, 12),
    ("Andaman Island Hopper", "Andaman", 6, 28000, 10),
    ("Darjeeling Tea Escape", "Darjeeling", 5, 16000, 10),
    ("Goa Party Weekend", "Goa", 3, 9000, 14),
    ("Manali Family Break", "Manali", 5, 14500, 12),
    ("Munnar Honeymoon", "Munnar", 4, 11000, 6),
    ("Jaipur Budget Tour", "Jaipur", 4, 10500, 14),
]
FIRST = ["Arjun","Priya","Vikram","Lakshmi","Ravi","Anita","Suresh","Deepa","Manoj","Kavya","Rahul","Sneha",
         "Naveen","Pooja","Harish","Nisha","Gokul","Revathi","Prakash","Swathi"]
LAST = ["Kumar","Devi","Raj","S","Babu","Nair","Iyer","Reddy","Pillai","Menon","Gupta","Sharma"]
REASONS = ["Change of plans","Health issue","Work commitment","Weather concerns","Found cheaper option","Family emergency"]
METHODS = ["UPI","CARD","NET_BANKING","CASH"]


def main(n_bookings=500, seed=42):
    random.seed(seed)
    if db.IS_SQLITE and db.SQLITE_FILE.exists():
        db.SQLITE_FILE.unlink()
    con = db.connect()
    db.reset_tables(con)
    run_script(con, db.SCHEMA.read_text())
    run_script(con, (ROOT / "sql" / "sample_data.sql").read_text())

    for d in NEW_DEST:
        q(con, "INSERT INTO destination(destination_name) VALUES (?)", (d,))
    dest_id = dict(q(con, "SELECT destination_name, destination_id FROM destination"))
    pid = q(con, "SELECT MAX(package_id) FROM travel_package").fetchone()[0]
    for name, dest, days, price, cap in NEW_PACKAGES:
        pid += 1
        q(con, "INSERT INTO travel_package VALUES (?,?,?,?,?,?)", (pid, name, dest_id[dest], days, price, cap))

    cid = q(con, "SELECT MAX(customer_id) FROM customer").fetchone()[0]
    for _ in range(115):
        cid += 1
        name = f"{random.choice(FIRST)} {random.choice(LAST)}"
        q(con, "INSERT INTO customer VALUES (?,?,?,?)",
                    (cid, name, str(random.randint(6000000000, 9999999999)), f"user{cid}@example.com"))

    packages = q(con, """SELECT p.package_id, d.destination_name, p.price, p.max_capacity
                              FROM travel_package p JOIN destination d USING(destination_id)""").fetchall()
    packages = [(a, b, float(c), d) for a, b, c, d in packages]    # PostgreSQL NUMERIC -> Decimal
    customers = [r[0] for r in q(con, "SELECT customer_id FROM customer")]
    seats = {}
    bid = q(con, "SELECT MAX(booking_id) FROM booking").fetchone()[0]
    payid = q(con, "SELECT MAX(payment_id) FROM payment").fetchone()[0]
    for p_id, dest, price, cap in packages:
        seats.setdefault(p_id, {})
    for r in q(con, "SELECT package_id, travel_date, num_travelers FROM booking WHERE status='CONFIRMED'"):
        d = str(r[1])   # PostgreSQL returns datetime.date
        seats[r[0]][d] = seats[r[0]].get(d, 0) + r[2]

    made, rows = 0, []
    while made < n_bookings:
        p_id, dest, price, cap = random.choice(packages)
        month = random.choices(range(1, 13), weights=SEASON[dest])[0]
        year = random.choice([2025, 2026])
        travel = date(year, month, random.randint(1, 28))
        booked = travel - timedelta(days=random.randint(10, 90))
        n = random.choice([1, 2, 2, 2, 3, 4, 5, 6])
        key = travel.isoformat()
        if seats[p_id].get(key, 0) + n > cap:       # enforce capacity rule
            continue
        cancelled = random.random() < 0.12
        status = "CANCELLED" if cancelled else "CONFIRMED"
        if not cancelled:
            seats[p_id][key] = seats[p_id].get(key, 0) + n
        total = price * n
        c_date = reason = None
        refund = 0
        if cancelled:
            c_date = booked + timedelta(days=random.randint(1, 9))
            if c_date >= travel:
                c_date = booked
            reason = random.choice(REASONS)
        bid += 1
        payid += 1
        pay_status, method, pay_date = "PAID", random.choice(METHODS), booked
        if cancelled:
            days_before = (travel - c_date).days
            pct = 0.9 if days_before >= 15 else 0.5 if days_before >= 7 else 0
            refund = round(total * pct, 2)
            pay_status = "REFUNDED"
        elif travel > date(2026, 11, 16) and random.random() < 0.3:
            pay_status, method, pay_date = "PENDING", None, None
        rows.append((bid, random.choice(customers), p_id, n, booked.isoformat(), key, status, total,
                     c_date.isoformat() if c_date else None, reason, refund,
                     payid, pay_status, method, pay_date.isoformat() if pay_date else None))
        made += 1

    # Deliberate data-quality problems so the FDS preprocessing step has real work to do
    for r in random.sample(rows, 12):                # duplicate booking records
        bid += 1; payid += 1
        rows.append((bid,) + r[1:11] + (payid,) + r[12:])
    rows = [list(r) for r in rows]
    for r in random.sample([r for r in rows if r[12] in ("PAID", "REFUNDED")], 18):
        r[13] = None                                 # missing payment method

    for r in rows:
        q(con, "INSERT INTO booking VALUES (?,?,?,?,?,?,?,?,?,?,?)", r[:11])
        amt = r[7]
        q(con, "INSERT INTO payment VALUES (?,?,?,?,?,?,?)",
                    (r[11], r[0], amt, r[13], r[12], r[14], r[10]))
    con.commit()
    for t in ("customer", "destination", "travel_package", "booking", "payment"):
        print(f"{t:15s}", q(con, f"SELECT COUNT(*) FROM {t}").fetchone()[0], "rows")
    con.close()
    print("Database ready:", "SQLite file" if db.IS_SQLITE else "PostgreSQL")


if __name__ == "__main__":
    main()
