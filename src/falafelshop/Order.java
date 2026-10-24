package falafelshop;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final int orderId;
    private final LocalDateTime dateTime;
    private final List<OrderItem> items;

    private String customerName;
    private String customerPhone;
    private OrderType orderType;

    private DiscountType discountType;
    private double discountValue;

    private double subtotal;
    private double discountAmount;
    private double tax;
    private double total;

    private PaymentMethod paymentMethod;
    private double amountPaid;
    private double change;

    private OrderStatus status;

    public Order(int orderId, LocalDateTime dateTime) {
        this.orderId = orderId;
        this.dateTime = dateTime;
        this.items = new ArrayList<>();
        this.customerName = "";
        this.customerPhone = "";
        this.orderType = OrderType.TAKEAWAY;
        this.discountType = DiscountType.NONE;
        this.discountValue = 0;
        this.status = OrderStatus.PENDING;
    }

    public int getOrderId() {
        return orderId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName == null ? "" : customerName.trim();
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone == null ? "" : customerPhone.trim();
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType == null ? OrderType.TAKEAWAY : orderType;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscount(DiscountType discountType, double discountValue) {
        this.discountType = discountType == null ? DiscountType.NONE : discountType;
        this.discountValue = Math.max(0, discountValue);
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getTax() {
        return tax;
    }

    public double getTotal() {
        return total;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public double getChange() {
        return change;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status == null ? OrderStatus.PENDING : status;
    }

    public void setPaymentDetails(PaymentMethod paymentMethod, double amountPaid, double change) {
        this.paymentMethod = paymentMethod;
        this.amountPaid = roundToTwo(amountPaid);
        this.change = roundToTwo(change);
    }

    public void addItem(ShopMenuItem menuItem) {
        for (OrderItem item : items) {
            if (item.getMenuItem().getId().equals(menuItem.getId())) {
                item.increaseQuantity();
                return;
            }
        }
        items.add(new OrderItem(menuItem));
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

    public void increaseQuantity(int index) {
        if (index >= 0 && index < items.size()) {
            items.get(index).increaseQuantity();
        }
    }

    public void decreaseQuantity(int index) {
        if (index >= 0 && index < items.size()) {
            if (items.get(index).getQuantity() > 1) {
                items.get(index).decreaseQuantity();
            } else {
                items.remove(index);
            }
        }
    }

    public void setItemNote(int index, String note) {
        if (index >= 0 && index < items.size()) {
            items.get(index).setNote(note);
        }
    }

    public void calculateTotals(double taxRate) {
        subtotal = 0;
        for (OrderItem item : items) {
            subtotal += item.calculateLineTotal();
        }
        subtotal = roundToTwo(subtotal);

        switch (discountType) {
            case FIXED:
                discountAmount = discountValue;
                break;
            case PERCENT:
                discountAmount = subtotal * (discountValue / 100.0);
                break;
            case NONE:
            default:
                discountAmount = 0;
                break;
        }

        if (discountAmount > subtotal) {
            discountAmount = subtotal;
        }

        double taxableAmount = Math.max(0, subtotal - discountAmount);
        tax = taxableAmount * Math.max(0, taxRate);
        total = taxableAmount + tax;

        discountAmount = roundToTwo(discountAmount);
        tax = roundToTwo(tax);
        total = roundToTwo(total);
    }

    private double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
