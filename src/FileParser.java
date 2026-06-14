import java.io.*;
import java.util.*;

public class FileParser {

    public static List<Part> loadParts(String filePath) {
        List<Part> parts = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                line = line.replace("|", ",").replace(";", ",");
                String[] fields = line.split(",");

                for (int i = 0; i < fields.length; i++) {
                    fields[i] = fields[i].trim();
                }

                if (fields.length < 6) continue;

                String code = fields[0];
                String name = fields[1];
                String brand = fields[2].isEmpty() ? "Unknown" : fields[2];

                String priceStr = fields[3].trim();
                priceStr = priceStr.replaceAll("(?i)rs\\.?", "").trim();
                priceStr = priceStr.replaceAll("[^0-9.]", "");

                int firstDot = priceStr.indexOf(".");
                if (firstDot != -1) {
                    priceStr = priceStr.substring(0, firstDot + 1)
                            + priceStr.substring(firstDot + 1).replace(".", "");
                }
                double price = 0;
                if (!priceStr.isEmpty()) {
                    price = Double.parseDouble(priceStr);
                }

                int quantity = 0;
                try {
                    quantity = Integer.parseInt(fields[4].trim());
                } catch (NumberFormatException e) {
                    quantity = 0;
                }

                String category = fields[5].trim().toLowerCase();
                category = Character.toUpperCase(category.charAt(0))
                        + category.substring(1);

                String date = fields.length > 6 ? fields[6].trim() : "";
                String image = fields.length > 7 ? fields[7].trim() : "";

                parts.add(new Part(code, name, brand, price,
                        quantity, category, date, image));
            }
            reader.close();

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        return parts;
    }

    public static List<Dealer> loadDealers(String filePath) {
        List<Dealer> dealers = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                line = line.replace("|", ",").replace(";", ",");
                String[] fields = line.split(",");

                for (int i = 0; i < fields.length; i++) {
                    fields[i] = fields[i].trim();
                }

                if (fields.length < 3) continue;

                String code = fields[0];
                String name = fields[1];
                String phone = fields.length > 2 && !fields[2].isEmpty() ? fields[2] : "N/A";
                String location = fields.length > 3 && !fields[3].isEmpty() ? fields[3] : "Unknown";

                dealers.add(new Dealer(code, name, phone, location));
            }
            reader.close();

        } catch (IOException e) {
            System.out.println("Error reading dealers: " + e.getMessage());
        }

        return dealers;
    }

}