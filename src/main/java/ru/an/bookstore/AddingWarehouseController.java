package ru.an.bookstore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ResourceBundle;

public class AddingWarehouseController {

    private static final Logger log = LoggerFactory.getLogger(AddingWarehouseController.class);

    @FXML private ResourceBundle resources;
    @FXML private TextField tfName;
    @FXML private TextField tfCount;
    @FXML private TableView<BookCatalog> tvAddWarehouse;
    @FXML private TableColumn<BookCatalog, String> tcName;
    @FXML private TableColumn<BookCatalog, String> tcAuther;
    @FXML private TableColumn<BookCatalog, String> tcPublishing;
    @FXML private TableColumn<BookCatalog, String> tcYear;
    @FXML private TableColumn<BookCatalog, String> tcISBN;

    private Stage stage;
    private WarehouseDAO warehouseDAO;
    private ObservableList<BookCatalog> availableBooks = FXCollections.observableArrayList();
    private List<BookCatalog> allAvailableBooks;

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    @FXML
    void initialize() {
        log.debug("Инициализация AddingWarehouseController");

        tcName.setCellValueFactory(new PropertyValueFactory<>("nameBook"));
        tcAuther.setCellValueFactory(new PropertyValueFactory<>("authors"));
        tcPublishing.setCellValueFactory(new PropertyValueFactory<>("publishingName"));
        tcYear.setCellValueFactory(cellData -> {
            if (cellData.getValue().getYearPublication() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(cellData.getValue().getYearPublication().getYear()));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        tcISBN.setCellValueFactory(new PropertyValueFactory<>("isbn"));

        tvAddWarehouse.setItems(availableBooks);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setWarehouseDAO(WarehouseDAO warehouseDAO) {
        this.warehouseDAO = warehouseDAO;
        loadAvailableBooks();
    }

    private void loadAvailableBooks() {
        allAvailableBooks = warehouseDAO.findBooksNotInWarehouse();
        availableBooks.setAll(allAvailableBooks);
        log.debug("Загружено {} книг для добавления на склад", allAvailableBooks.size());
    }

    public void onFind(ActionEvent actionEvent) {
        String searchText = tfName.getText();
        log.debug("Поиск книг для добавления: searchText='{}'", searchText);

        if (searchText == null || searchText.trim().isEmpty()) {
            availableBooks.setAll(allAvailableBooks);
        } else {
            availableBooks.setAll(allAvailableBooks.stream()
                    .filter(book -> book.getNameBook().toLowerCase()
                            .contains(searchText.toLowerCase()))
                    .toList());
        }
    }

    public void onAdd(ActionEvent actionEvent) {
        BookCatalog selected = tvAddWarehouse.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(resources.getString("add_warehouse.alert.error.select_book"));
            return;
        }

        String countText = tfCount.getText();
        if (countText == null || countText.trim().isEmpty()) {
            showAlert(resources.getString("add_warehouse.alert.error.enter_quantity"));
            return;
        }

        try {
            int quantity = Integer.parseInt(countText);
            if (quantity <= 0) {
                showAlert(resources.getString("add_warehouse.alert.error.quantity_positive"));
                return;
            }

            log.info("Добавление книги на склад: bookId={}, bookName='{}', quantity={}",
                    selected.getIdBook(), selected.getNameBook(), quantity);

            Warehouse warehouse = new Warehouse();
            warehouse.setBook(selected);
            warehouse.setQuantity(quantity);
            warehouseDAO.save(warehouse);

            log.info("Книга успешно добавлена на склад: bookId={}", selected.getIdBook());
            showAlert(resources.getString("add_warehouse.alert.success"));
            stage.close();
        } catch (NumberFormatException e) {
            log.warn("Ошибка ввода количества: value='{}'", countText);
            showAlert(resources.getString("add_warehouse.alert.error.invalid_number"));
        }
    }

    public void onExit(ActionEvent actionEvent) {
        log.debug("Закрытие окна добавления на склад");
        stage.close();
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(resources.getString("alert.title.information"));
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}