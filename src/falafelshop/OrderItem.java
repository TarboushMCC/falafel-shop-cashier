package falafelshop;

public class OrderItem {
    private final ShopMenuItem menuItem;
    private int quantity;
    private String note;

    public OrderItem(ShopMenuItem menuItem) {
        this.menuItem = menuItem;
        this.quantity = 1;
        this.note = "";
    }

    public ShopMenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? "" : note.trim();
    }

    public void increaseQuantity() {
        quantity++;
    }

    public void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
        }
    }

    public double calculateLineTotal() {
        return roundToTwo(menuItem.getPrice() * quantity);
    }

    private double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
