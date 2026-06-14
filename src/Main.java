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

        Tab dealersTab = new Tab("Dealers");
        dealersTab.setClosable(false);
        dealersTab.setContent(buildDealersTab());

        Tab cartTab = new Tab("Point of Sale");
        cartTab.setClosable(false);
        cartTab.setContent(buildCartTab());

        tabPane.getTabs().addAll(inventoryTab, dealersTab, cartTab);

        Scene scene = new Scene(tabPane, 900, 600);
        primaryStage.setTitle("Malabe Tuk-Tuk Spares Depot");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox buildInventoryTab() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10;");

        Label lowStockLabel = new Label();
        lowStockLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        List<Part> lowStock = inventoryManager.getLowStockParts(10);
        if (!lowStock.isEmpty()) {
            String warn = "LOW STOCK: ";
            for (Part p : lowStock) {
                warn += p.getCode() + " (" + p.getQuantity() + ") ";
            }
            lowStockLabel.setText(warn);
        }

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
                new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));

        table.getColumns().addAll(codeCol, nameCol, catCol, priceCol, qtyCol);
        table.getItems().addAll(inventoryManager.getParts());

        Label summaryLabel = new Label("Total Parts: " +
                inventoryManager.getParts().size() +
                " | Total Value: Rs." +
                inventoryManager.getTotalValue());

        vbox.getChildren().addAll(lowStockLabel, table, summaryLabel);
        return vbox;
    }

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

        for (int i = 0; i < selected.size() - 1; i++) {
            for (int j = 0; j < selected.size() - 1 - i; j++) {
                if (selected.get(j).getLocation()
                        .compareToIgnoreCase(selected.get(j + 1).getLocation()) > 0) {
                    Dealer temp2 = selected.get(j);
                    selected.set(j, selected.get(j + 1));
                    selected.set(j + 1, temp2);
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

        Button addButton = new Button("Add to Cart");

        TableView<CartItem> cartTable = new TableView<>();

        TableColumn<CartItem, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPart().getCode()));

        TableColumn<CartItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPart().getName()));

        TableColumn<CartItem, String> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));

        TableColumn<CartItem, String> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(d ->
                new SimpleStringProperty("Rs." + d.getValue().getSubtotal()));

        cartTable.getColumns().addAll(codeCol, nameCol, qtyCol, subtotalCol);

        Label totalLabel = new Label("Total: Rs.0.0");
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");
        Button checkoutButton = new Button("Checkout");

        addButton.setOnAction(e -> {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("");
            String selected = partCombo.getValue();
            if (selected == null) {
                messageLabel.setText("Please select a part!");
                return;
            }
            String code = selected.split(" - ")[0];
            Part part = inventoryManager.findPart(code);

            int qty = 0;
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
            if (qty > part.getQuantity()) {
                messageLabel.setText("Not enough stock! Available: " + part.getQuantity());
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
            if (!found) {
                cart.add(new CartItem(part, qty));
            }

            cartTable.getItems().clear();
            cartTable.getItems().addAll(cart);

            double total = 0;
            for (CartItem item : cart) {
                total += item.getSubtotal();
            }

            boolean hasEngine = false;
            boolean hasElectrical = false;
            for (CartItem item : cart) {
                if (item.getPart().getCategory().equalsIgnoreCase("Engine")) hasEngine = true;
                if (item.getPart().getCategory().equalsIgnoreCase("Electrical")) hasElectrical = true;
            }
            if (hasEngine && hasElectrical) {
                total = total * 0.90;
                totalLabel.setText("Total: Rs." + total + " (10% synergy discount!)");
            } else {
                totalLabel.setText("Total: Rs." + total);
            }
            qtyField.clear();
        });

        checkoutButton.setOnAction(e -> {
            if (cart.isEmpty()) {
                messageLabel.setText("Cart is empty!");
                return;
            }
            for (CartItem item : cart) {
                item.getPart().setQuantity(
                        item.getPart().getQuantity() - item.getQuantity());
                AuditLogger.log("CHECKOUT", item.getPart().getCode(), item.getQuantity());
            }
            cart.clear();
            cartTable.getItems().clear();
            totalLabel.setText("Total: Rs.0.0");
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Checkout successful!");
        });

        HBox inputRow = new HBox(10, partCombo, qtyField, addButton);
        vbox.getChildren().addAll(inputRow, cartTable, totalLabel, messageLabel, checkoutButton);
        return vbox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}