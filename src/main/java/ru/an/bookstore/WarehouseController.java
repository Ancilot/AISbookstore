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
import java.util.ResourceBundle;

public class WarehouseController {

    @FXML private ResourceBundle resources;

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
                    WarehouseController.class.getResource("adding-to-the-warehouse.fxml"), resources);
            Scene scene = new Scene(loader.load(), 1000, 600);

            AddingWarehouseController controller = loader.getController();
            Stage stage = new Stage();

            controller.setStage(stage);
            controller.setWarehouseDAO(warehouseDAO);
            controller.setResources(resources);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(resources.getString("app.title.add_warehouse"));
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
            showAlert(resources.getString("warehouse.alert.warning.select_book"), Alert.AlertType.WARNING);
            return;
        }

        String bookName = selected.getBook().getNameBook();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(resources.getString("warehouse.alert.confirm.delete_title"));
        confirm.setHeaderText(null);
        confirm.setContentText(java.text.MessageFormat.format(
                resources.getString("warehouse.alert.confirm.delete_text"),
                bookName));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Long bookIdLong = selected.getBook().getIdBook();
            int bookId = bookIdLong != null ? bookIdLong.intValue() : 0;
            int deleteResult = warehouseDAO.deleteBookToArchive(bookId);

            String message;
            Alert.AlertType alertType;

            switch (deleteResult) {
                case 1:
                    message = java.text.MessageFormat.format(
                            resources.getString("warehouse.alert.result.archived"),
                            bookName);
                    alertType = Alert.AlertType.WARNING;
                    break;
                case 2:
                    message = java.text.MessageFormat.format(
                            resources.getString("warehouse.alert.result.deleted"),
                            bookName);
                    alertType = Alert.AlertType.INFORMATION;
                    break;
                default:
                    message = java.text.MessageFormat.format(
                            resources.getString("warehouse.alert.result.error"),
                            bookName);
                    alertType = Alert.AlertType.ERROR;
                    break;
            }

            showAlert(message, alertType);
            refreshTable();
        }
    }

    public void onInvoice(ActionEvent actionEvent) {
        Warehouse selected = tvWarehouse.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(resources.getString("warehouse.alert.warning.select_for_invoice"), Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    WarehouseController.class.getResource("invoice.fxml"), resources);
            Scene scene = new Scene(loader.load(), 300, 250);

            InvoiceController controller = loader.getController();
            Stage stage = new Stage();

            controller.setStage(stage);
            controller.setWarehouse(selected);
            controller.setResources(resources);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(resources.getString("app.title.invoice"));
            stage.setScene(scene);
            stage.showAndWait();

            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onMain(ActionEvent actionEvent) {
        navigateTo("main.fxml", resources.getString("app.title"), actionEvent);
    }

    public void onClirnt(ActionEvent actionEvent) {
        navigateTo("clients.fxml", resources.getString("app.title.clients"), actionEvent);
    }

    public void onOreder(ActionEvent actionEvent) {
        navigateTo("orders.fxml", resources.getString("app.title.orders"), actionEvent);
    }

    public void onReprt(ActionEvent actionEvent) {
        navigateTo("report.fxml", resources.getString("app.title.reports"), actionEvent);
    }

    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }

    private void navigateTo(String fxml, String title, ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource(fxml), resources);
            Scene scene = new Scene(loader.load(), 1200, 600);
            Stage stage = (Stage) ((MenuItem) actionEvent.getSource())
                    .getParentPopup().getOwnerWindow();
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        String title;
        if (type == Alert.AlertType.ERROR) {
            title = resources.getString("alert.title.error");
        } else if (type == Alert.AlertType.WARNING) {
            title = resources.getString("alert.title.warning");
        } else {
            title = resources.getString("alert.title.information");
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}