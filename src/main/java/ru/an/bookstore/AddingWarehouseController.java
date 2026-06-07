package ru.an.bookstore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class AddingWarehouseController {

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

    @FXML
    void initialize() {
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
    }

    public void onFind(ActionEvent actionEvent) {
        String searchText = tfName.getText();
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
            showAlert("Ошибка", "Выберите книгу для добавления");
            return;
        }

        String countText = tfCount.getText();
        if (countText == null || countText.trim().isEmpty()) {
            showAlert("Ошибка", "Введите количество");
            return;
        }

        try {
            int quantity = Integer.parseInt(countText);
            if (quantity <= 0) {
                showAlert("Ошибка", "Количество должно быть больше 0");
                return;
            }

            Warehouse warehouse = new Warehouse();
            warehouse.setBook(selected);
            warehouse.setQuantity(quantity);
            warehouseDAO.save(warehouse);

            showAlert("Успех", "Книга успешно добавлена на склад");
            stage.close();
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректное количество");
        }
    }

    public void onExit(ActionEvent actionEvent) {
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}