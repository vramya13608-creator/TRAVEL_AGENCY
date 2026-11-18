"""Retrieve booking data from the database, clean it, analyse it and draw the five required charts."""
import warnings
from pathlib import Path
import db
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import seaborn as sns

ROOT = Path(__file__).resolve().parent.parent
warnings.filterwarnings("ignore", message="pandas only supports SQLAlchemy")
OUT = Path(__file__).resolve().parent / "output"
OUT.mkdir(exist_ok=True)
sns.set_theme(style="whitegrid")
SEASONS = {12: "Winter", 1: "Winter", 2: "Winter", 3: "Summer", 4: "Summer", 5: "Summer",
           6: "Monsoon", 7: "Monsoon", 8: "Monsoon", 9: "Monsoon", 10: "Post-monsoon", 11: "Post-monsoon"}
MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]

QUERY = """
SELECT b.booking_id, b.customer_id, b.package_id, p.package_name, d.destination_name AS destination,
       b.num_travelers, b.booking_date, b.travel_date, b.status, b.total_amount,
       b.refund_amount, pay.payment_status, pay.payment_method, pay.amount AS paid_amount
FROM booking b
JOIN travel_package p ON p.package_id = b.package_id
JOIN destination d    ON d.destination_id = p.destination_id
JOIN payment pay      ON pay.booking_id = b.booking_id
"""


def retrieve():
    con = db.connect()
    try:
        df = pd.read_sql_query(QUERY, con)
    finally:
        con.close()
    for col in ("total_amount", "refund_amount", "paid_amount"):   # PostgreSQL returns NUMERIC as Decimal
        df[col] = df[col].astype(float)
    return df


def clean(df, log):
    log.append(f"Rows retrieved from database: {len(df)}")
    key = ["customer_id", "package_id", "num_travelers", "booking_date", "travel_date", "status"]
    dup = df.duplicated(subset=key, keep="first").sum()
    df = df.drop_duplicates(subset=key, keep="first").copy()
    log.append(f"Duplicate bookings removed: {dup}")
    miss = df["payment_method"].isna() & df["payment_status"].isin(["PAID", "REFUNDED"])
    log.append(f"Missing payment_method on paid/refunded rows: {int(miss.sum())} (filled with 'UNKNOWN')")
    df.loc[miss, "payment_method"] = "UNKNOWN"
    df["payment_method"] = df["payment_method"].fillna("NOT PAID")
    df["booking_date"] = pd.to_datetime(df["booking_date"])
    df["travel_date"] = pd.to_datetime(df["travel_date"])
    bad = (df["travel_date"] <= df["booking_date"]).sum()
    df = df[df["travel_date"] > df["booking_date"]]
    log.append(f"Rows with travel date not after booking date removed: {bad}")
    df["travel_month"] = df["travel_date"].dt.month
    df["travel_ym"] = df["travel_date"].dt.to_period("M").dt.to_timestamp()
    df["season"] = df["travel_month"].map(SEASONS)
    df["net_revenue"] = df.apply(lambda r: r.paid_amount if r.payment_status == "PAID"
                                 else (r.paid_amount - r.refund_amount if r.payment_status == "REFUNDED" else 0), axis=1)
    log.append(f"Rows after cleaning: {len(df)}")
    return df


def save(fig, name):
    fig.tight_layout()
    fig.savefig(OUT / name, dpi=140)
    plt.close(fig)


def main():
    log = []
    raw = retrieve()
    raw.to_csv(OUT / "bookings_raw.csv", index=False)
    df = clean(raw, log)
    df.to_csv(OUT / "bookings_clean.csv", index=False)
    ok = df[df["status"] == "CONFIRMED"]

    pop = ok.groupby("destination")["num_travelers"].sum().sort_values(ascending=False)
    fig, ax = plt.subplots(figsize=(8, 5))
    sns.barplot(x=pop.values, y=pop.index, hue=pop.index, legend=False, palette="viridis", ax=ax)
    ax.set(title="Most Popular Destinations (confirmed travelers)", xlabel="Travelers", ylabel="")
    save(fig, "1_popular_destinations_bar.png")

    share = ok.groupby("destination").size()
    fig, ax = plt.subplots(figsize=(7, 7))
    ax.pie(share, labels=share.index, autopct="%1.1f%%", startangle=90, colors=sns.color_palette("pastel"))
    ax.set_title("Booking Distribution by Destination")
    save(fig, "2_booking_distribution_pie.png")

    trend = ok.groupby("travel_ym").size()
    fig, ax = plt.subplots(figsize=(10, 5))
    ax.plot(trend.index, trend.values, marker="o")
    ax.set(title="Monthly Booking Trend (by travel month)", xlabel="Month", ylabel="Confirmed bookings")
    fig.autofmt_xdate()
    save(fig, "3_monthly_trend_line.png")

    rev = df.groupby("package_name")["net_revenue"].sum().sort_values(ascending=False)
    fig, ax = plt.subplots(figsize=(9, 6))
    sns.barplot(x=rev.values, y=rev.index, hue=rev.index, legend=False, palette="crest", ax=ax)
    ax.set(title="Net Revenue by Travel Package (after refunds)", xlabel="Revenue (Rs.)", ylabel="")
    save(fig, "4_revenue_by_package_bar.png")

    heat = ok.pivot_table(index="destination", columns="travel_month", values="booking_id",
                          aggfunc="count", fill_value=0).reindex(columns=range(1, 13), fill_value=0)
    heat.columns = MONTHS
    fig, ax = plt.subplots(figsize=(11, 5.5))
    sns.heatmap(heat, annot=True, fmt="d", cmap="YlOrRd", ax=ax)
    ax.set(title="Seasonal Booking Pattern (destination x travel month)", xlabel="", ylabel="")
    save(fig, "5_seasonal_heatmap.png")

    # ---- observations (computed, not hard-coded) ----
    peak = ok.groupby("travel_month").size()
    cancel_rate = (df["status"] == "CANCELLED").mean() * 100
    best_month = {d: MONTHS[heat.loc[d].values.argmax()] for d in heat.index}
    top_pkg = ok.groupby("package_name").size().sort_values(ascending=False)
    lines = ["# Analysis and Observations (auto-generated by analysis.py)", "", "## Data cleaning log"]
    lines += [f"- {x}" for x in log]
    lines += ["", "## Key findings",
              f"- Most popular destination: **{pop.index[0]}** ({int(pop.iloc[0])} travelers); least popular: **{pop.index[-1]}** ({int(pop.iloc[-1])}).",
              f"- Busiest travel month overall: **{MONTHS[peak.idxmax() - 1]}** ({int(peak.max())} bookings); quietest: **{MONTHS[peak.idxmin() - 1]}** ({int(peak.min())}).",
              f"- Most booked package: **{top_pkg.index[0]}** ({int(top_pkg.iloc[0])} bookings).",
              f"- Highest net revenue package: **{rev.index[0]}** (Rs.{rev.iloc[0]:,.0f}); total net revenue Rs.{rev.sum():,.0f}.",
              f"- Cancellation rate: **{cancel_rate:.1f}%** of bookings.",
              "- Peak month per destination: " + ", ".join(f"{k} -> {v}" for k, v in best_month.items()) + ".",
              "", "## Seasonal summary (confirmed bookings)", ok.groupby("season").size().to_frame("bookings").to_markdown()]
    (OUT / "observations.md").write_text("\n".join(lines) + "\n")
    print("\n".join(lines))


if __name__ == "__main__":
    main()
