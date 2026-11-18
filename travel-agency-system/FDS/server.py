"""Web server: serves web/index.html and a small JSON API backed by the PostgreSQL database.

Run:   python python/server.py        then open http://localhost:5000
Deploy: gunicorn --chdir python server:app   (set DATABASE_URL to your hosted PostgreSQL)
"""
import os
from pathlib import Path
from flask import Flask, jsonify, request, send_from_directory
import db
from db import q

WEB = Path(__file__).resolve().parent.parent / "web"
app = Flask(__name__, static_folder=None)

OVERBOOKED = """
SELECT b.package_id, b.travel_date FROM booking b JOIN travel_package p ON p.package_id = b.package_id
WHERE b.status = 'CONFIRMED'
GROUP BY b.package_id, b.travel_date, p.max_capacity
HAVING SUM(b.num_travelers) > p.max_capacity"""


def f(x):
    return None if x is None else float(x)


def s(x):
    return None if x is None else str(x)


@app.get("/")
def index():
    return send_from_directory(WEB, "index.html")


@app.get("/api/state")
def get_state():
    con = db.connect()
    try:
        c = [dict(id=r[0], name=r[1], phone=r[2], email=r[3]) for r in
             q(con, "SELECT customer_id, name, phone, email FROM customer ORDER BY 1")]
        p = [dict(id=r[0], name=r[1], dest=r[2], days=r[3], price=f(r[4]), cap=r[5]) for r in
             q(con, """SELECT p.package_id, p.package_name, d.destination_name, p.duration_days, p.price, p.max_capacity
                       FROM travel_package p JOIN destination d ON d.destination_id = p.destination_id ORDER BY 1""")]
        b = [dict(id=r[0], cid=r[1], pid=r[2], n=r[3], bd=s(r[4]), td=s(r[5]), st=r[6], amt=f(r[7]),
                  cd=s(r[8]), cr=r[9], rf=f(r[10]) or 0) for r in
             q(con, """SELECT booking_id, customer_id, package_id, num_travelers, booking_date, travel_date, status,
                       total_amount, cancellation_date, cancellation_reason, refund_amount FROM booking ORDER BY 1""")]
        y = [dict(id=r[0], bid=r[1], amt=f(r[2]), m=r[3], s=r[4], d=s(r[5]), rf=f(r[6]) or 0) for r in
             q(con, """SELECT payment_id, booking_id, amount, payment_method, payment_status, payment_date, refund_amount
                       FROM payment ORDER BY 1""")]
        return jsonify(c=c, p=p, b=b, y=y)
    finally:
        con.close()


def sync_ids(con, table, key, ids):
    """Delete rows that are no longer in the submitted state."""
    existing = {r[0] for r in q(con, f"SELECT {key} FROM {table}")}
    for i in existing - set(ids):
        q(con, f"DELETE FROM {table} WHERE {key} = ?", (i,))


@app.post("/api/state")
def save_state():
    st = request.get_json(force=True)
    con = db.connect()
    try:
        already_over = set(q(con, OVERBOOKED).fetchall())     # e.g. duplicate rows kept for the data-cleaning demo
        # remove deleted rows, children first
        sync_ids(con, "payment", "payment_id", [x["id"] for x in st["y"]])
        sync_ids(con, "booking", "booking_id", [x["id"] for x in st["b"]])
        sync_ids(con, "travel_package", "package_id", [x["id"] for x in st["p"]])
        sync_ids(con, "customer", "customer_id", [x["id"] for x in st["c"]])
        for d in {x["dest"] for x in st["p"]}:
            q(con, "INSERT INTO destination(destination_name) VALUES (?) ON CONFLICT (destination_name) DO NOTHING", (d,))
        dest = dict(q(con, "SELECT destination_name, destination_id FROM destination"))
        for x in st["c"]:
            q(con, """INSERT INTO customer(customer_id, name, phone, email) VALUES (?,?,?,?)
                      ON CONFLICT (customer_id) DO UPDATE SET name=EXCLUDED.name, phone=EXCLUDED.phone, email=EXCLUDED.email""",
              (x["id"], x["name"], x["phone"], x["email"]))
        for x in st["p"]:
            q(con, """INSERT INTO travel_package(package_id, package_name, destination_id, duration_days, price, max_capacity)
                      VALUES (?,?,?,?,?,?) ON CONFLICT (package_id) DO UPDATE SET package_name=EXCLUDED.package_name,
                      destination_id=EXCLUDED.destination_id, duration_days=EXCLUDED.duration_days,
                      price=EXCLUDED.price, max_capacity=EXCLUDED.max_capacity""",
              (x["id"], x["name"], dest[x["dest"]], x["days"], x["price"], x["cap"]))
        for x in st["b"]:
            q(con, """INSERT INTO booking(booking_id, customer_id, package_id, num_travelers, booking_date, travel_date,
                      status, total_amount, cancellation_date, cancellation_reason, refund_amount)
                      VALUES (?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (booking_id) DO UPDATE SET status=EXCLUDED.status,
                      cancellation_date=EXCLUDED.cancellation_date, cancellation_reason=EXCLUDED.cancellation_reason,
                      refund_amount=EXCLUDED.refund_amount""",
              (x["id"], x["cid"], x["pid"], x["n"], x["bd"], x["td"], x["st"], x["amt"], x["cd"], x["cr"], x["rf"] or 0))
        for x in st["y"]:
            q(con, """INSERT INTO payment(payment_id, booking_id, amount, payment_method, payment_status, payment_date, refund_amount)
                      VALUES (?,?,?,?,?,?,?) ON CONFLICT (payment_id) DO UPDATE SET payment_method=EXCLUDED.payment_method,
                      payment_status=EXCLUDED.payment_status, payment_date=EXCLUDED.payment_date,
                      refund_amount=EXCLUDED.refund_amount""",
              (x["id"], x["bid"], x["amt"], x["m"], x["s"], x["d"], x["rf"] or 0))
        if set(q(con, OVERBOOKED).fetchall()) - already_over:   # reject only NEW overbooking
            con.rollback()
            return jsonify(error="Rejected: a departure would exceed its maximum capacity."), 409
        con.commit()
        return jsonify(ok=True)
    except Exception as e:
        con.rollback()
        return jsonify(error="Database error: " + str(e).splitlines()[0]), 400
    finally:
        con.close()


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.getenv("PORT", 5000)), debug=False)
