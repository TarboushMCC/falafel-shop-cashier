package falafelshop;

public enum OrderType {
    DINE_IN("Dine-in"),
    TAKEAWAY("Takeaway"),
    DELIVERY_PICKUP("Delivery Pickup");

    private final String label;

    OrderType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
