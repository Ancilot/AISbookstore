package ru.an.bookstore;

import javafx.application.Platform;
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
import java.util.Optional;

public class WarehouseController {

    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private ObservableList<Warehouse> warehouseList = FXCollections.observableArrayList();
    private boolean showArchive = false;

    @FXML private TextField tfFind;
    @FXML private TableView<Warehouse> tvWarehouse;
    @FXML private TableColumn<Warehouse, String> tcBook;
    @FXML private TableColumn<Warehouse, String> tcISBN;
    @FXML private TableColumn<Warehouse, Integer> tcGuanity;
    @FXML private TableColumn<Warehouse, String> tcStatus;

    @FXML
    void initialize() {
        tcBook.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getBook().getNameBook()));
        tcISBN.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getBook().getIsbn()));
        tcGuanity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tcStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        refreshTable();
    }

    private void refreshTable() {
        if (showArchive) {
            warehouseList.setAll(warehouseDAO.findAllArchive());
        } else {
            warehouseList.setAll(warehouseDAO.findAllActive());
        }
        tvWarehouse.setItems(warehouseList);
    }

    public void onFind(ActionEvent actionEvent) {
        String searchText = tfFind.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            refreshTable();
        } else {
            if (showArchive) {
                warehouseList.setAll(warehouseDAO.searchArchive(searchText));
            } else {
                warehouseList.setAll(warehouseDAO.searchActive(searchText));
            }
            tvWarehouse.setItems(warehouseList);
        }
    }

    public void onAdd(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    WarehouseController.class.getResource("adding-to-the-warehouse.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 600);

            AddingWarehouseController controller = loader.getController();
            Stage stage = new Stage();

            controller.setStage(stage);
            controller.setWarehouseDAO(warehouseDAO);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Добавление книги на склад");
            stage.setScene(scene);
            stage.showAndWait();

            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDelete(ActionEvent actionEvent) {
        Warehouse selected = tvWarehouse.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите книгу для удаления", Alert.AlertType.WARNING);
            return;
        }

        String bookName = selected.getBook().getNameBook();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы уверены, что хотите удалить книгу \"" + bookName + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Long bookIdLong = selected.getBook().getIdBook();
            int bookId = bookIdLong != null ? bookIdLong.intValue() : 0;
            int deleteResult = warehouseDAO.deleteBookToArchive(bookId);

            // Формируем сообщение в зависимости от результата
            String message;
            Alert.AlertType alertType;

            switch (deleteResult) {
                case 1:
                    message = "Книга \"" + bookName + "\" перенесена в архив (были продажи или остатки на складе)";
                    alertType = Alert.AlertType.WARNING;
                    break;
                case 2:
                    message = "Книга \"" + bookName + "\" полностью удалена из каталога";
                    alertType = Alert.AlertType.INFORMATION;
                    break;
                default:
                    message = "Ошибка при удалении книги \"" + bookName + "\"";
                    alertType = Alert.AlertType.ERROR;
                    break;
            }

            showAlert("Результат", message, alertType);
            refreshTable();
        }
    }



    public void onInvoice(ActionEvent actionEvent) {
        Warehouse selected = tvWarehouse.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите книгу для создания накладной", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    WarehouseController.class.getResource("invoice.fxml"));
            Scene scene = new Scene(loader.load(), 300, 250);

            InvoiceController controller = loader.getController();
            Stage stage = new Stage();

            controller.setStage(stage);
            controller.setWarehouse(selected);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Накладная");
            stage.setScene(scene);
            stage.showAndWait();

            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onMain(ActionEvent actionEvent) {
        navigateTo(actionEvent, "main.fxml", "Главная");
    }

    public void onClirnt(ActionEvent actionEvent) {
        navigateTo(actionEvent, "clients.fxml", "Клиенты");
    }

    public void onOreder(ActionEvent actionEvent) {
        navigateTo(actionEvent, "orders.fxml", "Заказы");
    }

    public void onReprt(ActionEvent actionEvent) {
        navigateTo(actionEvent, "report.fxml", "Отчеты");
    }

    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }

    private void navigateTo(ActionEvent actionEvent, String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource(fxml));
            Scene scene = new Scene(loader.load(), 1200, 600);
            Stage stage = (Stage) ((MenuItem) actionEvent.getSource())
                    .getParentPopup().getOwnerWindow();
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}