import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.*;

public class Main extends Application {

    private InventoryManager inventoryManager;
    private List<Dealer> dealers;

    @Override
    public void start(Stage primaryStage) {
        List<Part> parts = FileParser.loadParts("data/inventory_legacy.txt");
        dealers = FileParser.loadDealers("data/dealers_legacy.txt");
        inventoryManager = new InventoryManager(parts);

        TabPane tabPane = new TabPane();

        Tab inventoryTab = new Tab("Inventory");
        inventoryTab.setClosable(false);
        inventoryTab.setContent(buildInventoryTab());

        Tab searchTab = new Tab("Search");
        searchTab.setClosable(false);
        searchTab.setContent(buildSearchTab());

        Tab dealersTab = new Tab("Dealers");
        dealersTab.setClosable(false);
        dealersTab.setContent(buildDealersTab());

        Tab cartTab = new Tab("Point of Sale");
        cartTab.setClosable(false);
        cartTab.setContent(buildCartTab());

        tabPane.getTabs().addAll(inventoryTab, searchTab, dealersTab, cartTab);

        Scene scene = new Scene(tabPane, 950, 680);
        primaryStage.setTitle("Malabe Tuk-Tuk Spares Depot");
        primaryStage.setScene(scene);

        // Save to file when window is closed
        primaryStage.setOnCloseRequest(e -> {
            FileParser.saveParts("data/inventory_legacy.txt",
                    inventoryManager.getParts());
        });

        primaryStage.show();
    }

    // ─── INVENTORY TAB ───────────────────────────────────────────────────────
    private VBox buildInventoryTab() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10;");

        // Configurable low stock threshold
        TextField thresholdField = new TextField("10");
        thresholdField.setPrefWidth(60);
        Button applyThresholdBtn = new Button("Apply");

        Label lowStockLabel = new Label();
        lowStockLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        // TABLE — sorted by category then part code
        TableView<Part> table = new TableView<>();

        TableColumn<Part, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCode()));

        TableColumn<Part, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getName()));

        TableColumn<Part, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCategory()));

        TableColumn<Part, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(d ->
                new SimpleStringProperty("Rs." + d.getValue().getPrice()));

        TableColumn<Part, String> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(
                        d.getValue().getQuantity())));

        table.getColumns().addAll(codeCol, nameCol, catCol, priceCol, qtyCol);
        table.getItems().addAll(inventoryManager.getSortedParts());

        Label summaryLabel = new Label();

        // Refresh helpers
        Runnable refreshLowStock = () -> {
            int threshold = 10;
            try {
                threshold = Integer.parseInt(thresholdField.getText().trim());
            } catch (NumberFormatException ex) { threshold = 10; }

            List<Part> low = inventoryManager.getLowStockParts(threshold);
            if (!low.isEmpty()) {
                String warn = "LOW STOCK:  ";
                for (Part p : low) {
                    warn += p.getCode() + " (" + p.getQuantity() + ")   ";
                }
                lowStockLabel.setText(warn);
            } else {
                lowStockLabel.setText("All items sufficiently stocked.");
            }
        };

        Runnable refreshSummary = () ->
                summaryLabel.setText("Total Parts: "
                        + inventoryManager.getParts().size()
                        + "  |  Total Value: Rs."
                        + String.format("%.2f", inventoryManager.getTotalValue()));

        Runnable refreshTable = () -> {
            table.getItems().clear();
            table.getItems().addAll(inventoryManager.getSortedParts());
            refreshSummary.run();
            refreshLowStock.run();
        };

        refreshLowStock.run();
        refreshSummary.run();

        applyThresholdBtn.setOnAction(e -> refreshLowStock.run());

        HBox thresholdRow = new HBox(10,
                new Label("Low Stock Threshold:"), thresholdField,
                applyThresholdBtn);

        // Form fields
        TextField codeField     = new TextField();
        codeField.setPromptText("Code");
        TextField nameField     = new TextField();
        nameField.setPromptText("Name");
        TextField brandField    = new TextField();
        brandField.setPromptText("Brand");
        TextField priceField    = new TextField();
        priceField.setPromptText("Price");
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");

        Button addPartButton    = new Button("Add Part");
        Button updatePartButton = new Button("Update Part");
        Button deletePartButton = new Button("Delete Part");
        Label  formMessage      = new Label();

        // Click a row → fill the form
        table.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected != null) {
                        codeField.setText(selected.getCode());
                        nameField.setText(selected.getName());
                        brandField.setText(selected.getBrand());
                        priceField.setText(String.valueOf(selected.getPrice()));
                        quantityField.setText(String.valueOf(selected.getQuantity()));
                        categoryField.setText(selected.getCategory());
                    }
                });

        // ADD
        addPartButton.setOnAction(e -> {
            formMessage.setStyle("-fx-text-fill: red;");
            if (codeField.getText().trim().isEmpty()
                    || nameField.getText().trim().isEmpty()) {
                formMessage.setText("Code and Name are required!");
                return;
            }
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                int qty = Integer.parseInt(quantityField.getText().trim());
                if (price < 0 || qty < 0) {
                    formMessage.setText("Price and Quantity cannot be negative!");
                    return;
                }
                Part newPart = new Part(
                        codeField.getText().trim(),
                        nameField.getText().trim(),
                        brandField.getText().trim(),
                        price, qty,
                        categoryField.getText().trim(),
                        "", "");
                if (inventoryManager.addPart(newPart)) {
                    refreshTable.run();
                    AuditLogger.log("ADD_PART", newPart.getCode());
                    FileParser.saveParts("data/inventory_legacy.txt",
                            inventoryManager.getParts());
                    formMessage.setStyle("-fx-text-fill: green;");
                    formMessage.setText("Part added successfully!");
                } else {
                    formMessage.setText("Duplicate code! Part not added.");
                }
            } catch (NumberFormatException ex) {
                formMessage.setText("Invalid price or quantity!");
            }
        });

        // UPDATE
        updatePartButton.setOnAction(e -> {
            formMessage.setStyle("-fx-text-fill: red;");
            String code = codeField.getText().trim();
            if (code.isEmpty()) {
                formMessage.setText("Select a part from the table first!");
                return;
            }
            if (nameField.getText().trim().isEmpty()) {
                formMessage.setText("Name cannot be empty!");
                return;
            }
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                int qty = Integer.parseInt(quantityField.getText().trim());
                if (price < 0 || qty < 0) {
                    formMessage.setText("Price and Quantity cannot be negative!");
                    return;
                }
                boolean updated = inventoryManager.updatePart(
                        code,
                        nameField.getText().trim(),
                        brandField.getText().trim(),
                        price, qty,
                        categoryField.getText().trim());
                if (updated) {
                    refreshTable.run();
                    AuditLogger.log("UPDATE_PART", code);
                    FileParser.saveParts("data/inventory_legacy.txt",
                            inventoryManager.getParts());
                    formMessage.setStyle("-fx-text-fill: green;");
                    formMessage.setText("Part updated successfully!");
                } else {
                    formMessage.setText("Part not found!");
                }
            } catch (NumberFormatException ex) {
                formMessage.setText("Invalid price or quantity!");
            }
        });

        // DELETE
        deletePartButton.setOnAction(e -> {
            Part selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                formMessage.setStyle("-fx-text-fill: red;");
                formMessage.setText("Select a part to delete!");
                return;
            }
            inventoryManager.deletePart(selected.getCode());
            refreshTable.run();
            AuditLogger.log("DELETE_PART", selected.getCode());
            FileParser.saveParts("data/inventory_legacy.txt",
                    inventoryManager.getParts());
            formMessage.setStyle("-fx-text-fill: green;");
            formMessage.setText("Part deleted!");
        });

        HBox formRow1  = new HBox(10, codeField, nameField, brandField);
        HBox formRow2  = new HBox(10, priceField, quantityField, categoryField);
        HBox buttonRow = new HBox(10, addPartButton, updatePartButton,
                deletePartButton);

        vbox.getChildren().addAll(thresholdRow, lowStockLabel, table,
                summaryLabel, formRow1, formRow2, buttonRow, formMessage);
        return vbox;
    }

    // ─── SEARCH TAB ──────────────────────────────────────────────────────────
    private VBox buildSearchTab() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10;");

        Label title = new Label("Multi-Criteria Search");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        TextField keywordField  = new TextField();
        keywordField.setPromptText("Keyword (name / code / brand)");
        keywordField.setPrefWidth(220);

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(
                "", "Engine", "Electrical", "Bodywork", "Brakes");
        categoryCombo.setValue("");

        TextField minPriceField = new TextField();
        minPriceField.setPromptText("Min Price");
        minPriceField.setPrefWidth(100);

        TextField maxPriceField = new TextField();
        maxPriceField.setPromptText("Max Price");
        maxPriceField.setPrefWidth(100);

        Button searchButton = new Button("Search");
        Button clearButton  = new Button("Clear");
        Label  resultLabel  = new Label();

        TableView<Part> resultTable = new TableView<>();

        TableColumn<Part, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCode()));

        TableColumn<Part, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getName()));

        TableColumn<Part, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCategory()));

        TableColumn<Part, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(d ->
                new SimpleStringProperty("Rs." + d.getValue().getPrice()));

        TableColumn<Part, String> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(
                        d.getValue().getQuantity())));

        resultTable.getColumns().addAll(
                codeCol, nameCol, catCol, priceCol, qtyCol);

        searchButton.setOnAction(e -> {
            String keyword  = keywordField.getText().trim();
            String category = categoryCombo.getValue();
            double minPrice = -1, maxPrice = -1;

            try {
                if (!minPriceField.getText().trim().isEmpty())
                    minPrice = Double.parseDouble(minPriceField.getText().trim());
            } catch (NumberFormatException ex) {
                resultLabel.setStyle("-fx-text-fill: red;");
                resultLabel.setText("Invalid min price!");
                return;
            }
            try {
                if (!maxPriceField.getText().trim().isEmpty())
                    maxPrice = Double.parseDouble(maxPriceField.getText().trim());
            } catch (NumberFormatException ex) {
                resultLabel.setStyle("-fx-text-fill: red;");
                resultLabel.setText("Invalid max price!");
                return;
            }

            List<Part> results = inventoryManager.searchParts(
                    keyword, category, minPrice, maxPrice);
            resultTable.getItems().clear();
            resultTable.getItems().addAll(results);
            resultLabel.setStyle("-fx-text-fill: green;");
            resultLabel.setText("Found " + results.size() + " result(s).");
        });

        clearButton.setOnAction(e -> {
            keywordField.clear();
            categoryCombo.setValue("");
            minPriceField.clear();
            maxPriceField.clear();
            resultTable.getItems().clear();
            resultLabel.setText("");
        });

        HBox row1 = new HBox(10, new Label("Keyword:"), keywordField,
                new Label("Category:"), categoryCombo);
        HBox row2 = new HBox(10, new Label("Min Price:"), minPriceField,
                new Label("Max Price:"), maxPriceField);
        HBox row3 = new HBox(10, searchButton, clearButton);

        vbox.getChildren().addAll(title, row1, row2, row3, resultLabel,
                resultTable);
        return vbox;
    }

    // ─── DEALERS TAB ─────────────────────────────────────────────────────────
    private VBox buildDealersTab() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10;");

        Label title = new Label("Randomly Selected Dealers");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        List<Dealer> selected = new ArrayList<>();
        List<Dealer> temp = new ArrayList<>(dealers);
        Random random = new Random();

        while (selected.size() < 4 && !temp.isEmpty()) {
            int index = random.nextInt(temp.size());
            selected.add(temp.get(index));
            temp.remove(index);
        }

        // Bubble sort by location
        for (int i = 0; i < selected.size() - 1; i++) {
            for (int j = 0; j < selected.size() - 1 - i; j++) {
                if (selected.get(j).getLocation()
                        .compareToIgnoreCase(
                                selected.get(j + 1).getLocation()) > 0) {
                    Dealer tmp = selected.get(j);
                    selected.set(j, selected.get(j + 1));
                    selected.set(j + 1, tmp);
                }
            }
        }

        TableView<Dealer> table = new TableView<>();

        TableColumn<Dealer, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCode()));

        TableColumn<Dealer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getName()));

        TableColumn<Dealer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPhone()));

        TableColumn<Dealer, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getLocation()));

        table.getColumns().addAll(codeCol, nameCol, phoneCol, locationCol);
        table.getItems().addAll(selected);

        vbox.getChildren().addAll(title, table);
        return vbox;
    }

    // ─── CART TAB ────────────────────────────────────────────────────────────
    private VBox buildCartTab() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10;");

        List<CartItem> cart = new ArrayList<>();

        ComboBox<String> partCombo = new ComboBox<>();
        for (Part p : inventoryManager.getParts()) {
            partCombo.getItems().add(p.getCode() + " - " + p.getName());
        }
        partCombo.setPromptText("Select a part");

        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");

        Button addButton      = new Button("Add to Cart");
        Button checkoutButton = new Button("Checkout");

        TableView<CartItem> cartTable = new TableView<>();

        TableColumn<CartItem, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPart().getCode()));

        TableColumn<CartItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPart().getName()));

        TableColumn<CartItem, String> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(
                        d.getValue().getQuantity())));

        TableColumn<CartItem, String> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(d ->
                new SimpleStringProperty("Rs."
                        + String.format("%.2f", d.getValue().getSubtotal())));

        cartTable.getColumns().addAll(codeCol, nameCol, qtyCol, subtotalCol);

        Label totalLabel   = new Label("Total: Rs.0.00");
        Label messageLabel = new Label();

        // Recalculate total with discount logic
        Runnable recalcTotal = () -> {
            double total = 0;
            for (CartItem item : cart) {
                total += item.getSubtotal(); // bulk discount already inside
            }
            boolean hasEngine     = false;
            boolean hasElectrical = false;
            for (CartItem item : cart) {
                if (item.getPart().getCategory().equalsIgnoreCase("Engine"))
                    hasEngine = true;
                if (item.getPart().getCategory().equalsIgnoreCase("Electrical"))
                    hasElectrical = true;
            }
            if (hasEngine && hasElectrical) {
                total = total * 0.90;
                totalLabel.setText("Total: Rs." + String.format("%.2f", total)
                        + "   (10% synergy discount applied!)");
            } else {
                totalLabel.setText("Total: Rs." + String.format("%.2f", total));
            }
        };

        // ADD TO CART
        addButton.setOnAction(e -> {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("");
            String selectedItem = partCombo.getValue();
            if (selectedItem == null) {
                messageLabel.setText("Please select a part!");
                return;
            }
            String code = selectedItem.split(" - ")[0];
            Part part = inventoryManager.findPart(code);
            if (part == null) {
                messageLabel.setText("Part not found!");
                return;
            }
            int qty;
            try {
                qty = Integer.parseInt(qtyField.getText().trim());
            } catch (NumberFormatException ex) {
                messageLabel.setText("Invalid quantity!");
                return;
            }
            if (qty <= 0) {
                messageLabel.setText("Quantity must be greater than 0!");
                return;
            }

            // Check how much is already reserved in cart
            int alreadyInCart = 0;
            for (CartItem item : cart) {
                if (item.getPart().getCode().equals(code)) {
                    alreadyInCart = item.getQuantity();
                    break;
                }
            }
            if (qty + alreadyInCart > part.getQuantity()) {
                messageLabel.setText("Not enough stock! Available: "
                        + (part.getQuantity() - alreadyInCart));
                return;
            }

            boolean found = false;
            for (CartItem item : cart) {
                if (item.getPart().getCode().equals(code)) {
                    item.setQuantity(item.getQuantity() + qty);
                    found = true;
                    break;
                }
            }
            if (!found) cart.add(new CartItem(part, qty));

            cartTable.getItems().clear();
            cartTable.getItems().addAll(cart);
            recalcTotal.run();
            qtyField.clear();
        });

        // CHECKOUT
        checkoutButton.setOnAction(e -> {
            if (cart.isEmpty()) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Cart is empty!");
                return;
            }
            for (CartItem item : cart) {
                item.getPart().setQuantity(
                        item.getPart().getQuantity() - item.getQuantity());
                AuditLogger.log("CHECKOUT", item.getPart().getCode(),
                        item.getQuantity());
            }
            FileParser.saveParts("data/inventory_legacy.txt",
                    inventoryManager.getParts());

            cart.clear();
            cartTable.getItems().clear();
            totalLabel.setText("Total: Rs.0.00");
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Checkout successful! Stock updated.");
        });

        HBox inputRow = new HBox(10, partCombo, qtyField, addButton);
        vbox.getChildren().addAll(inputRow, cartTable, totalLabel,
                messageLabel, checkoutButton);
        return vbox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}