import javafx.application.Application;
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
        // Load data
        List<Part> parts = FileParser.loadParts("data/inventory_legacy.txt");
        dealers = FileParser.loadDealers("data/dealers_legacy.txt");
        inventoryManager = new InventoryManager(parts);

        // Create tab pane
        TabPane tabPane = new TabPane();

        // Inventory tab
        Tab inventoryTab = new Tab("Inventory");
        inventoryTab.setClosable(false);
        inventoryTab.setContent(new Label("Inventory coming soon"));

        // Dealers tab
        Tab dealersTab = new Tab("Dealers");
        dealersTab.setClosable(false);
        dealersTab.setContent(new Label("Dealers coming soon"));

        // Cart tab
        Tab cartTab = new Tab("Point of Sale");
        cartTab.setClosable(false);
        cartTab.setContent(new Label("Cart coming soon"));

        tabPane.getTabs().addAll(inventoryTab, dealersTab, cartTab);

        Scene scene = new Scene(tabPane, 900, 600);
        primaryStage.setTitle("Malabe Tuk-Tuk Spares Depot");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}