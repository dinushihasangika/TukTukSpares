import java.util.*;

public class InventoryManager {
    private List<Part> parts;

    public InventoryManager(List<Part> parts) {
        this.parts = parts;
    }

    public List<Part> getParts() { return parts; }

    // Add a new part
    public boolean addPart(Part newPart) {
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

    // Update an existing part
    public boolean updatePart(String code, String newName, String newBrand,
                              double newPrice, int newQty, String newCategory) {
        for (Part p : parts) {
            if (p.getCode().equalsIgnoreCase(code)) {
                p.setName(newName);
                p.setBrand(newBrand);
                p.setPrice(newPrice);
                p.setQuantity(newQty);
                p.setCategory(newCategory);
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

    // Manual bubble sort — grouped by category, then by part code ascending
    public List<Part> getSortedParts() {
        List<Part> sorted = new ArrayList<>(parts);
        int n = sorted.size();

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                Part a = sorted.get(j);
                Part b = sorted.get(j + 1);

                int catCompare = a.getCategory()
                        .compareToIgnoreCase(b.getCategory());

                boolean shouldSwap;
                if (catCompare > 0) {
                    // different category — sort by category
                    shouldSwap = true;
                } else if (catCompare == 0) {
                    // same category — sort by part code
                    shouldSwap = a.getCode()
                            .compareToIgnoreCase(b.getCode()) > 0;
                } else {
                    shouldSwap = false;
                }

                if (shouldSwap) {
                    sorted.set(j, b);
                    sorted.set(j + 1, a);
                }
            }
        }
        return sorted;
    }

    // Multi-criteria search — keyword + category + price range
    // Pass "" or null to ignore a filter. Pass -1 to ignore price limits.
    public List<Part> searchParts(String keyword, String category,
                                  double minPrice, double maxPrice) {
        List<Part> results = new ArrayList<>();

        for (Part p : parts) {

            // Filter 1 — keyword (checks name, code, brand)
            boolean keywordMatch = true;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim().toLowerCase();
                keywordMatch = p.getName().toLowerCase().contains(kw)
                        || p.getCode().toLowerCase().contains(kw)
                        || p.getBrand().toLowerCase().contains(kw);
            }

            // Filter 2 — category
            boolean categoryMatch = true;
            if (category != null && !category.trim().isEmpty()) {
                categoryMatch = p.getCategory()
                        .equalsIgnoreCase(category.trim());
            }

            // Filter 3 — price range
            boolean priceMatch = true;
            if (minPrice >= 0) {
                priceMatch = p.getPrice() >= minPrice;
            }
            if (maxPrice >= 0) {
                priceMatch = priceMatch && p.getPrice() <= maxPrice;
            }

            // All three filters must pass
            if (keywordMatch && categoryMatch && priceMatch) {
                results.add(p);
            }
        }
        return results;
    }
}