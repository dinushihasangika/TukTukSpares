import java.util.*;

public class InventoryManager {
    private List<Part> parts;

    public InventoryManager(List<Part> parts) {
        this.parts = parts;
    }

    public List<Part> getParts() { return parts; }

    // Add a new part
    public boolean addPart(Part newPart) {
        // Check for duplicate code
        for (Part p : parts) {
            if (p.getCode().equalsIgnoreCase(newPart.getCode())) {
                return false; // duplicate
            }
        }
        parts.add(newPart);
        return true;
    }

    // Delete a part by code
    public boolean deletePart(String code) {
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).getCode().equalsIgnoreCase(code)) {
                parts.remove(i);
                return true;
            }
        }
        return false;
    }

    // Find a part by code
    public Part findPart(String code) {
        for (Part p : parts) {
            if (p.getCode().equalsIgnoreCase(code)) {
                return p;
            }
        }
        return null;
    }

    // Get low stock parts below threshold
    public List<Part> getLowStockParts(int threshold) {
        List<Part> lowStock = new ArrayList<>();
        for (Part p : parts) {
            if (p.getQuantity() <= threshold) {
                lowStock.add(p);
            }
        }
        return lowStock;
    }

    // Get total inventory value
    public double getTotalValue() {
        double total = 0;
        for (Part p : parts) {
            total += p.getPrice() * p.getQuantity();
        }
        return total;
    }
}