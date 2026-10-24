package falafelshop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class OrderManager {
    private final List<Order> orders;
    private int nextOrderId;

    public OrderManager() {
        this.orders = new ArrayList<>();
        this.nextOrderId = 1001;
    }

    public Order createNewOrder() {
        return new Order(nextOrderId++, LocalDateTime.now());
    }

    public void addOrder(Order order) {
        if (order == null) {
            return;
        }

        if (findById(order.getOrderId()) == null) {
            orders.add(order);
        }
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public Order findById(int orderId) {
        for (Order order : orders) {
            if (order.getOrderId() == orderId) {
                return order;
            }
        }
        return null;
    }

    public List<Order> searchOrders(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(orders);
        }

        String normalized = query.trim().toLowerCase(Locale.ROOT);
        List<Order> results = new ArrayList<>();

        for (Order order : orders) {
            String orderId = String.valueOf(order.getOrderId());
            String customerName = order.getCustomerName() == null ? "" : order.getCustomerName().toLowerCase(Locale.ROOT);

            if (orderId.contains(normalized) || customerName.contains(normalized)) {
                results.add(order);
            }
        }

        return results;
    }

    public void exportOrderHistory(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Falafel House - Daily Order History");
            writer.println("=================================");
            for (Order order : orders) {
                writer.println("Order #" + order.getOrderId()
                    + " | Customer: " + safe(order.getCustomerName())
                    + " | Type: " + order.getOrderType()
                    + " | Total: $" + String.format("%.2f", order.getTotal())
                    + " | Payment: " + (order.getPaymentMethod() == null ? "N/A" : order.getPaymentMethod())
                    + " | Status: " + order.getStatus());
            }
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Walk-in" : value;
    }
}
