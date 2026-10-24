package falafelshop;

public class Payment {
    private final PaymentMethod paymentMethod;
    private double amountPaid;
    private double change;
    private boolean successful;
    private String message;

    public Payment(PaymentMethod paymentMethod, double amountPaid) {
        this.paymentMethod = paymentMethod;
        this.amountPaid = amountPaid;
        this.change = 0;
        this.successful = false;
        this.message = "";
    }

    public boolean processPayment(double totalDue) {
        double due = roundToTwo(totalDue);

        if (paymentMethod == null) {
            successful = false;
            message = "Please select a payment method.";
            return false;
        }

        if (paymentMethod.isCash()) {
            if (amountPaid < due) {
                successful = false;
                message = "Insufficient cash amount received.";
                return false;
            }
            change = roundToTwo(amountPaid - due);
            successful = true;
            message = "Cash payment accepted.";
            return true;
        }

        amountPaid = due;
        change = 0;
        successful = true;
        message = paymentMethod + " payment confirmed.";
        return true;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public double getAmountPaid() {
        return roundToTwo(amountPaid);
    }

    public double getChange() {
        return roundToTwo(change);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getMessage() {
        return message;
    }

    private double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
