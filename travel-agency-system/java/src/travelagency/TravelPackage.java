package travelagency;

public class TravelPackage {
    private final int packageId;
    private final String packageName;
    private final String destination;
    private final int durationDays;
    private final double price;        // price per traveler
    private final int maxCapacity;     // maximum travelers per departure (travel date)

    public TravelPackage(int packageId, String packageName, String destination,
                         int durationDays, double price, int maxCapacity) {
        this.packageId = packageId;
        this.packageName = packageName;
        this.destination = destination;
        this.durationDays = durationDays;
        this.price = price;
        this.maxCapacity = maxCapacity;
    }

    public int getPackageId() { return packageId; }
    public String getPackageName() { return packageName; }
    public String getDestination() { return destination; }
    public int getDurationDays() { return durationDays; }
    public double getPrice() { return price; }
    public int getMaxCapacity() { return maxCapacity; }

    @Override
    public String toString() {
        return String.format("Package[%d] %s | %s | %d days | Rs.%.2f per person | capacity %d",
                packageId, packageName, destination, durationDays, price, maxCapacity);
    }
}
