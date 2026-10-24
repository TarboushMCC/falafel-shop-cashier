package falafelshop;

public class CashRegister {
    private final double openingBalance;
    private double cashSales;
    private double cardSales;
    private double digitalSales;
    private double refunds;

    public CashRegister(double openingBalance) {
        this.openingBalance = Math.max(0, openingBalance);
    }

    public void addSale(Order order) {
        if (order == null || order.getPaymentMethod() == null) {
            return;
        }

        double amount = order.getTotal();
        switch (order.getPaymentMethod()) {
            case CASH:
                cashSales += amount;
                break;
            case CARD:
                cardSales += amount;
                break;
            case DIGITAL:
                digitalSales += amount;
                break;
            default:
                break;
        }
    }

    public void addRefund(double refundAmount) {
        if (refundAmount > 0) {
            refunds += refundAmount;
        }
    }

    public double getOpeningBalance() {
        return roundToTwo(openingBalance);
    }

    public double getCashSales() {
        return roundToTwo(cashSales);
    }

    public double getCardSales() {
        return roundToTwo(cardSales);
    }

    public double getDigitalSales() {
        return roundToTwo(digitalSales);
    }

    public double getRefunds() {
        return roundToTwo(refunds);
    }

    public double getClosingEstimate() {
        return roundToTwo(openingBalance + cashSales - refunds);
    }

    public String getSummaryText() {
        String nl = System.lineSeparator();
        return "CASH REGISTER SUMMARY" + nl
            + "Opening Cash: $" + format(getOpeningBalance()) + nl
            + "Cash Sales: $" + format(getCashSales()) + nl
            + "Card Sales: $" + format(getCardSales()) + nl
            + "Digital Sales: $" + format(getDigitalSales()) + nl
            + "Refunds: $" + format(getRefunds()) + nl
            + "Closing Cash Estimate: $" + format(getClosingEstimate());
    }

    private double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String format(double value) {
        return String.format("%.2f", value);
    }
}
