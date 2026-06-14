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
        cartTab.setContent(new Label("Cart coming soon"));

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

        // Bubble sort by location
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

    public static void main(String[] args) {
        launch(args);
    }
}