package falafelshop;

public enum PaymentMethod {
    CASH("Cash"),
    CARD("Card"),
    DIGITAL("Digital");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public boolean isCash() {
        return this == CASH;
    }

    @Override
    public String toString() {
        return label;
    }
}
