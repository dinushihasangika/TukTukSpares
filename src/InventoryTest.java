import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.*;

public class InventoryTest {

    private InventoryManager manager;
    private List<Part> testParts;

    @Before
    public void setUp() {
        testParts = new ArrayList<>();
        testParts.add(new Part("P001", "Bajaj Piston", "Bajaj",
                4500.0, 15, "Engine", "2023-10-12", ""));
        testParts.add(new Part("P002", "Brake Pad", "TVS",
                1250.0, 8, "Brakes", "2023-05-12", ""));
        testParts.add(new Part("P003", "Spark Plug", "NGK",
                850.0, 50, "Electrical", "2024-01-05", ""));
        testParts.add(new Part("P004", "Headlight Bulb", "Local",
                450.0, 30, "Electrical", "2023-11-20", ""));
        testParts.add(new Part("P005", "Canopy Cover", "Local",
                8500.0, 5, "Bodywork", "2023-08-15", ""));
        manager = new InventoryManager(testParts);
    }

    // ── ADD PART TESTS ───────────────────────────────────────────────────────

    @Test
    public void testAddNewPart() {
        Part newPart = new Part("P010", "New Part", "Brand",
                500.0, 10, "Engine", "", "");
        boolean result = manager.addPart(newPart);
        assertTrue("New part should be added successfully", result);
        assertEquals("Total parts should be 6", 6, manager.getParts().size());
    }

    @Test
    public void testAddDuplicatePart() {
        Part duplicate = new Part("P001", "Duplicate", "Brand",
                100.0, 1, "Engine", "", "");
        boolean result = manager.addPart(duplicate);
        assertFalse("Duplicate code should be rejected", result);
        assertEquals("Total parts should still be 5",
                5, manager.getParts().size());
    }

    // ── DELETE PART TESTS ────────────────────────────────────────────────────

    @Test
    public void testDeleteExistingPart() {
        boolean result = manager.deletePart("P001");
        assertTrue("Existing part should be deleted", result);
        assertEquals("Total parts should be 4", 4, manager.getParts().size());
    }

    @Test
    public void testDeleteNonExistentPart() {
        boolean result = manager.deletePart("P999");
        assertFalse("Non-existent part should return false", result);
        assertEquals("Total parts should still be 5",
                5, manager.getParts().size());
    }

    // ── UPDATE PART TESTS ────────────────────────────────────────────────────

    @Test
    public void testUpdateExistingPart() {
        boolean result = manager.updatePart(
                "P001", "Updated Piston", "Bajaj", 5000.0, 20, "Engine");
        assertTrue("Update should succeed", result);
        Part updated = manager.findPart("P001");
        assertEquals("Name should be updated",
                "Updated Piston", updated.getName());
        assertEquals("Price should be updated", 5000.0, updated.getPrice(), 0.01);
        assertEquals("Quantity should be updated", 20, updated.getQuantity());
    }

    @Test
    public void testUpdateNonExistentPart() {
        boolean result = manager.updatePart(
                "P999", "Ghost Part", "None", 100.0, 1, "Engine");
        assertFalse("Update of non-existent part should return false", result);
    }

    // ── LOW STOCK TESTS ──────────────────────────────────────────────────────

    @Test
    public void testLowStockDetection() {
        List<Part> lowStock = manager.getLowStockParts(10);
        // P002 (8) and P005 (5) are below threshold of 10
        assertEquals("Should find 2 low stock items", 2, lowStock.size());
    }

    @Test
    public void testLowStockWithZeroQuantity() {
        manager.addPart(new Part("P006", "Empty Part", "Brand",
                100.0, 0, "Engine", "", ""));
        List<Part> lowStock = manager.getLowStockParts(10);
        // P002 (8), P005 (5), P006 (0)
        assertEquals("Should find 3 low stock items", 3, lowStock.size());
    }

    @Test
    public void testLowStockThresholdZero() {
        List<Part> lowStock = manager.getLowStockParts(0);
        assertEquals("No parts should be at or below 0 stock",
                0, lowStock.size());
    }

    // ── SEARCH TESTS ─────────────────────────────────────────────────────────

    @Test
    public void testSearchByKeyword() {
        List<Part> results = manager.searchParts("Spark", "", -1, -1);
        assertEquals("Should find 1 part with keyword Spark", 1, results.size());
        assertEquals("P003", results.get(0).getCode());
    }

    @Test
    public void testSearchByCategory() {
        List<Part> results = manager.searchParts("", "Electrical", -1, -1);
        assertEquals("Should find 2 Electrical parts", 2, results.size());
    }

    @Test
    public void testSearchByPriceRange() {
        List<Part> results = manager.searchParts("", "", 400.0, 1300.0);
        // P002 (1250), P003 (850), P004 (450) are in range
        assertEquals("Should find 3 parts in price range", 3, results.size());
    }

    @Test
    public void testSearchMultiCriteria() {
        // Electrical parts between Rs.400 and Rs.900
        List<Part> results = manager.searchParts("", "Electrical", 400.0, 900.0);
        // P003 (850) and P004 (450) match
        assertEquals("Should find 2 Electrical parts in price range",
                2, results.size());
    }

    @Test
    public void testSearchNoResults() {
        List<Part> results = manager.searchParts("XYZ999", "", -1, -1);
        assertEquals("Should find 0 results for unknown keyword",
                0, results.size());
    }

    // ── SORT TESTS ───────────────────────────────────────────────────────────

    @Test
    public void testSortedByCategory() {
        List<Part> sorted = manager.getSortedParts();
        // First part should be Bodywork (alphabetically first)
        assertEquals("First category should be Bodywork",
                "Bodywork", sorted.get(0).getCategory());
    }

    @Test
    public void testSortedByCodeWithinCategory() {
        List<Part> sorted = manager.getSortedParts();
        // Find Electrical parts and check they are ordered by code
        List<String> electricalCodes = new ArrayList<>();
        for (Part p : sorted) {
            if (p.getCategory().equalsIgnoreCase("Electrical")) {
                electricalCodes.add(p.getCode());
            }
        }
        // P003 should come before P004
        assertTrue("P003 should come before P004",
                electricalCodes.indexOf("P003") < electricalCodes.indexOf("P004"));
    }

    // ── CART / DISCOUNT TESTS ────────────────────────────────────────────────

    @Test
    public void testBulkDiscountApplied() {
        // qty >= 3 should get 5% discount
        CartItem item = new CartItem(
                manager.findPart("P001"), 3); // 4500 * 3 = 13500
        double expected = 13500 * 0.95; // 12825.0
        assertEquals("Bulk discount should give Rs.12825",
                expected, item.getSubtotal(), 0.01);
    }

    @Test
    public void testNoBulkDiscountBelow3() {
        CartItem item = new CartItem(
                manager.findPart("P001"), 2); // 4500 * 2 = 9000, no discount
        assertEquals("No discount for qty < 3",
                9000.0, item.getSubtotal(), 0.01);
    }

    @Test
    public void testTotalValue() {
        double expected = (4500 * 15) + (1250 * 8) +
                (850 * 50) + (450 * 30) + (8500 * 5);
        assertEquals("Total inventory value should match",
                expected, manager.getTotalValue(), 0.01);
    }

    // ── DIRTY DATA PARSING TESTS ─────────────────────────────────────────────

    @Test
    public void testParserLoadsAllParts() {
        List<Part> parts = FileParser.loadParts("data/inventory_legacy.txt");
        assertEquals("Should load 10 parts from legacy file",
                10, parts.size());
    }

    @Test
    public void testParserHandlesMixedDelimiters() {
        List<Part> parts = FileParser.loadParts("data/inventory_legacy.txt");
        // P002 uses pipe delimiter — should still parse correctly
        Part p002 = null;
        for (Part p : parts) {
            if (p.getCode().equals("P002")) { p002 = p; break; }
        }
        assertNotNull("P002 should be parsed despite pipe delimiter", p002);
        assertEquals("P002 price should be 1250.0", 1250.0, p002.getPrice(), 0.01);
    }

    @Test
    public void testParserHandlesCurrencyPrefix() {
        List<Part> parts = FileParser.loadParts("data/inventory_legacy.txt");
        // P001 has "Rs. 4500.00" — should parse to 4500.0
        Part p001 = null;
        for (Part p : parts) {
            if (p.getCode().equals("P001")) { p001 = p; break; }
        }
        assertNotNull("P001 should be parsed", p001);
        assertEquals("P001 price should be 4500.0", 4500.0, p001.getPrice(), 0.01);
    }

    @Test
    public void testParserNormalisesCategory() {
        List<Part> parts = FileParser.loadParts("data/inventory_legacy.txt");
        // P004 has "electrical" lowercase — should become "Electrical"
        Part p004 = null;
        for (Part p : parts) {
            if (p.getCode().equals("P004")) { p004 = p; break; }
        }
        assertNotNull("P004 should be parsed", p004);
        assertEquals("Category should be capitalised",
                "Electrical", p004.getCategory());
    }

    @Test
    public void testParserHandlesMissingBrand() {
        List<Part> parts = FileParser.loadParts("data/inventory_legacy.txt");
        // P003 and P010 have missing brand — should default to "Unknown"
        Part p003 = null;
        for (Part p : parts) {
            if (p.getCode().equals("P003")) { p003 = p; break; }
        }
        assertNotNull("P003 should be parsed", p003);
        assertEquals("Missing brand should default to Unknown",
                "Unknown", p003.getBrand());
    }
}
