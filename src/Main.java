import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Part> parts = FileParser.loadParts("data/inventory_legacy.txt");
        for (Part p : parts) {
            System.out.println(p.getCode() + " | " + p.getName()
                    + " | " + p.getCategory()
                    + " | Rs." + p.getPrice());
        }

        System.out.println("--- DEALERS ---");
        List<Dealer> dealers = FileParser.loadDealers("data/dealers_legacy.txt");
        for (Dealer d : dealers) {
            System.out.println(d.getCode() + " | " + d.getName()
                    + " | " + d.getLocation());
        }
    }
}