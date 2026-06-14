public class CartItem {
    private Part part;
    private int quantity;

    public CartItem(Part part, int quantity) {
        this.part = part;
        this.quantity = quantity;
    }

    public Part getPart() { return part; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getSubtotal() {
        double subtotal = part.getPrice() * quantity;
        if (quantity >= 3) {
            subtotal = subtotal * 0.95;
        }
        return subtotal;
    }
}