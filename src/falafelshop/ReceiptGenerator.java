package falafelshop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;

public class ReceiptGenerator {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ReceiptGenerator() {
    }

    public static String generateReceipt(Order order, String shopName, String cashierName) {
        StringBuilder sb = new StringBuilder();
        String nl = System.lineSeparator();

        sb.append(shopName).append(nl);
        sb.append("Order #").append(order.getOrderId()).append(nl);
        sb.append("Date: ").append(order.getDateTime().format(DATE_TIME_FORMATTER)).append(nl);
        sb.append("Cashier: ").append(cashierName == null || cashierName.isBlank() ? "Unknown" : cashierName).append(nl);
        sb.append("Order Type: ").append(order.getOrderType()).append(nl);

        if (!order.getCustomerName().isBlank()) {
            sb.append("Customer: ").append(order.getCustomerName()).append(nl);
        }
        if (!order.getCustomerPhone().isBlank()) {
            sb.append("Phone: ").append(order.getCustomerPhone()).append(nl);
        }

        sb.append("----------------------------------------").append(nl);
        for (OrderItem item : order.getItems()) {
            sb.append(item.getMenuItem().getName())
              .append(" x").append(item.getQuantity())
              .append(" @ $").append(String.format("%.2f", item.getMenuItem().getPrice()))
              .append(" = $").append(String.format("%.2f", item.calculateLineTotal()))
              .append(nl);

            if (!item.getNote().isBlank()) {
                sb.append("   Note: ").append(item.getNote()).append(nl);
            }
        }

        sb.append("----------------------------------------").append(nl);
        sb.append("Subtotal: $").append(String.format("%.2f", order.getSubtotal())).append(nl);
        sb.append("Discount: -$").append(String.format("%.2f", order.getDiscountAmount())).append(nl);
        sb.append("Tax: $").append(String.format("%.2f", order.getTax())).append(nl);
        sb.append("Total: $").append(String.format("%.2f", order.getTotal())).append(nl);
        sb.append("Payment Method: ").append(order.getPaymentMethod() == null ? "N/A" : order.getPaymentMethod()).append(nl);
        sb.append("Amount Paid: $").append(String.format("%.2f", order.getAmountPaid())).append(nl);
        sb.append("Change: $").append(String.format("%.2f", order.getChange())).append(nl);
        sb.append("Status: ").append(order.getStatus()).append(nl);
        sb.append("----------------------------------------").append(nl);
        sb.append("Thank you for choosing ").append(shopName).append("!").append(nl);

        return sb.toString();
    }

    public static void saveReceiptToFile(String receiptText, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.print(receiptText);
        }
    }
}
