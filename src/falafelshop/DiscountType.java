package falafelshop;

public enum DiscountType {
    NONE("None"),
    FIXED("Fixed Amount"),
    PERCENT("Percentage");

    private final String label;

    DiscountType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
