package ru.an.bookstore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MakingController {

    @FXML private ResourceBundle resources;
    @FXML private TableView<BookCatalog> tvAvailableBooks;
    @FXML private TableColumn<BookCatalog, String> colAvailableName;
    @FXML private TableColumn<BookCatalog, String> colAvailableAuthor;
    @FXML private TableColumn<BookCatalog, String> colAvailableGenre;
    @FXML private TableColumn<BookCatalog, String> colAvailablePrice;
    @FXML private TableColumn<BookCatalog, Integer> colAvailableQuantity;
    @FXML private TableColumn<BookCatalog, String> colAvailableStatus;
    @FXML private TextField tfAvailableSearch;
    @FXML private TextField tfAvailableCount;

    @FXML private TableView<CompositionCheck> tvCheckItems;
    @FXML private TableColumn<CompositionCheck, String> colCheckName;
    @FXML private TableColumn<CompositionCheck, String> colCheckAuthor;
    @FXML private TableColumn<CompositionCheck, String> colCheckGenre;
    @FXML private TableColumn<CompositionCheck, String> colCheckPrice;
    @FXML private TableColumn<CompositionCheck, Integer> colCheckQuantity;
    @FXML private TextField tfCheckSearch;

    private Stage stage;
    private Clients client;
    private Checks currentCheck;
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final ChecksDAO checksDAO = new ChecksDAO();
    private final CompositionCheckDAO compositionCheckDAO = new CompositionCheckDAO();
    private final OrdersDAO ordersDAO = new OrdersDAO();

    private ObservableList<BookCatalog> availableBooks = FXCollections.observableArrayList();
    private ObservableList<CompositionCheck> checkItems = FXCollections.observableArrayList();
    private List<BookCatalog> allAvailableBooks;

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    @FXML
    void initialize() {
        colAvailableName.setCellValueFactory(new PropertyValueFactory<>("nameBook"));
        colAvailableAuthor.setCellValueFactory(new PropertyValueFactory<>("authors"));
        colAvailableGenre.setCellValueFactory(new PropertyValueFactory<>("genres"));
        colAvailablePrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAvailableQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colAvailableStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tvAvailableBooks.setItems(availableBooks);

        colCheckName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBook().getNameBook()));
        colCheckAuthor.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBook().getAuthors()));
        colCheckGenre.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBook().getGenres()));
        colCheckPrice.setCellValueFactory(new PropertyValueFactory<>("priceTime"));
        colCheckQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tvCheckItems.setItems(checkItems);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setClient(Clients client) {
        this.client = client;
        loadAvailableBooks();
        createNewCheck();
    }

    private void loadAvailableBooks() {
        allAvailableBooks = warehouseDAO.findBooksForSale(null);
        availableBooks.setAll(allAvailableBooks);
    }

    private void createNewCheck() {
        currentCheck = checksDAO.createCheck(client.getIdClient());
        if (currentCheck != null) {
            refreshCheckItems();
        } else {
            showAlert(resources.getString("making.alert.error.check_not_created"));
        }
    }

    @FXML
    public void onFindAvailable(ActionEvent actionEvent) {
        String searchText = tfAvailableSearch.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            refreshAvailableBooks();
        } else {
            List<BookCatalog> results = warehouseDAO.findBooksForSale(searchText);
            availableBooks.setAll(results);
        }
    }

    @FXML
    public void onAddToCheck(ActionEvent actionEvent) {
        if (currentCheck == null) {
            showAlert(resources.getString("making.alert.error.check_not_created"));
            return;
        }

        BookCatalog selected = tvAvailableBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(resources.getString("making.alert.error.select_book"));
            return;
        }

        String countText = tfAvailableCount.getText();
        if (countText == null || countText.trim().isEmpty()) {
            showAlert(resources.getString("making.alert.error.enter_quantity"));
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(countText);
        } catch (NumberFormatException e) {
            showAlert(resources.getString("making.alert.error.invalid_quantity"));
            return;
        }

        if (quantity <= 0) {
            showAlert(resources.getString("making.alert.error.quantity_positive"));
            return;
        }
        if (quantity > selected.getQuantity()) {
            showAlert(java.text.MessageFormat.format(
                    resources.getString("making.alert.error.insufficient_stock"),
                    selected.getQuantity()));
            return;
        }

        CompositionCheck composition = new CompositionCheck();
        composition.setChecks(currentCheck);
        composition.setBook(selected);
        composition.setQuantity(quantity);
        compositionCheckDAO.save(composition);

        refreshAvailableBooks();
        refreshCheckItems();
        tfAvailableCount.clear();
    }

    @FXML
    public void onFindCheck(ActionEvent actionEvent) {
        if (currentCheck == null) {
            refreshCheckItems();
            return;
        }
        String searchText = tfCheckSearch.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            refreshCheckItems();
        } else {
            List<CompositionCheck> items = compositionCheckDAO.searchCheckDetails(currentCheck.getIdCheck(), searchText);
            checkItems.setAll(items);
        }
    }

    @FXML
    public void onRemoveFromCheck(ActionEvent actionEvent) {
        if (currentCheck == null) {
            showAlert(resources.getString("making.alert.error.check_not_found"));
            return;
        }

        CompositionCheck selected = tvCheckItems.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(resources.getString("making.alert.error.select_for_remove"));
            return;
        }

        if (selected.getIdComposition() != null) {
            compositionCheckDAO.deleteById(selected.getIdComposition());
        } else {
            compositionCheckDAO.deleteByCheckAndBook(currentCheck.getIdCheck(), selected.getBook().getIdBook());
        }

        refreshAvailableBooks();
        refreshCheckItems();
    }

    @FXML
    public void onPurchase(ActionEvent actionEvent) {
        if (currentCheck == null) {
            showAlert(resources.getString("making.alert.error.check_not_found"));
            return;
        }
        if (checkItems.isEmpty()) {
            showAlert(resources.getString("making.alert.error.empty_check"));
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(resources.getString("making.alert.confirm.purchase_title"));
        confirm.setHeaderText(null);
        confirm.setContentText(resources.getString("making.alert.confirm.purchase_text"));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            checksDAO.completeCheck(currentCheck.getIdCheck());
            showAlert(resources.getString("making.alert.success.purchase"));
            stage.close();
        }
    }

    @FXML
    public void onMakeOrderDirect(ActionEvent actionEvent) {
        BookCatalog selected = tvAvailableBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(resources.getString("making.alert.error.select_book"));
            return;
        }

        showCreateOrderDialog(selected);
    }

    private void showCreateOrderDialog(BookCatalog selectedBook) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MakingController.class.getResource("creating-an-order.fxml"), resources);
            Scene scene = new Scene(loader.load(), 267, 285);

            CreatOrderController controller = loader.getController();
            Stage orderStage = new Stage();

            controller.setStage(orderStage);
            controller.setClient(client);
            controller.setBook(selectedBook);
            controller.setResources(resources);

            orderStage.initModality(Modality.WINDOW_MODAL);
            orderStage.setTitle(resources.getString("app.title.create_order"));
            orderStage.setScene(scene);
            orderStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(resources.getString("making.alert.error.order_form"));
        }
    }

    @FXML
    public void onBack(ActionEvent actionEvent) {
        if (checkItems.isEmpty() && currentCheck != null) {
            compositionCheckDAO.deleteByCheck(currentCheck.getIdCheck());
            checksDAO.deleteCheck(currentCheck.getIdCheck());
        }
        stage.close();
    }

    private void refreshAvailableBooks() {
        String searchText = tfAvailableSearch.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            allAvailableBooks = warehouseDAO.findBooksForSale(null);
            availableBooks.setAll(allAvailableBooks);
        } else {
            List<BookCatalog> results = warehouseDAO.findBooksForSale(searchText);
            availableBooks.setAll(results);
        }
    }

    private void refreshCheckItems() {
        if (currentCheck != null) {
            List<CompositionCheck> items = compositionCheckDAO.findByCheck(currentCheck.getIdCheck());
            checkItems.setAll(items);
        } else {
            checkItems.clear();
        }
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(resources.getString("alert.title.information"));
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}