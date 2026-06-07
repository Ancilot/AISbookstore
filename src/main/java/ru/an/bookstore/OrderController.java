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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class OrderController {

    private final OrdersDAO ordersDAO = new OrdersDAO();
    private ObservableList<Orders> ordersList = FXCollections.observableArrayList();
    private Stage stage;

    @FXML private TableView<Orders> tvOrders;
    @FXML private TableColumn<Orders, String> colClient;
    @FXML private TableColumn<Orders, String> colDate;
    @FXML private TableColumn<Orders, String> colStatus;
    @FXML private TableColumn<Orders, String> colComment;
    @FXML private TableColumn<Orders, String> colBook;
    @FXML private TableColumn<Orders, Integer> colQuantity;
    @FXML private TextField tfSearch;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    void initialize() {
        colClient.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getClient().getSurname() + " " +
                                cellData.getValue().getClient().getNameClient()));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateOrder"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colComment.setCellValueFactory(new PropertyValueFactory<>("textOrder"));
        colBook.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getBook().getNameBook()));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        refreshTable();
    }

    private void refreshTable() {
        ordersList.setAll(ordersDAO.findAllActive());
        tvOrders.setItems(ordersList);
    }

    @FXML
    public void onComplete(ActionEvent actionEvent) {
        Orders selected = tvOrders.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите заказ для выполнения");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText(null);
        confirm.setContentText("Отметить заказ №" + selected.getIdOrder() + " как выполненный?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ordersDAO.completeOrder(selected.getIdOrder());
                showAlert("Успех", "Заказ выполнен, клиент уведомлен");
                refreshTable();
            } catch (RuntimeException e) {
                showAlert("Ошибка", e.getMessage());
            }
        }
    }

    @FXML
    public void onDelete(ActionEvent actionEvent) {
        Orders selected = tvOrders.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите заказ для удаления");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить заказ №" + selected.getIdOrder() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int deleteResult = ordersDAO.deleteOrderToArchive(selected.getIdOrder());
                String message = deleteResult == 1 ? "Заказ архивирован" : "Заказ удален";
                showAlert("Результат", message);
                refreshTable();
            } catch (RuntimeException e) {
                showAlert("Ошибка", e.getMessage());
            }
        }
    }

    @FXML
    public void onFind(ActionEvent actionEvent) {
        String searchText = tfSearch.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            refreshTable();
        } else {
            ordersList.setAll(ordersDAO.search(searchText));
            tvOrders.setItems(ordersList);
        }
    }

    @FXML
    public void onMain(ActionEvent actionEvent) {
        navigateTo(actionEvent, "main.fxml", "Главная");
    }

    @FXML
    public void onClient(ActionEvent actionEvent) {
        navigateTo(actionEvent, "clients.fxml", "Клиенты");
    }

    @FXML
    public void onWarehouse(ActionEvent actionEvent) {
        navigateTo(actionEvent, "warehouse.fxml", "Склад");
    }

    @FXML
    public void onReport(ActionEvent actionEvent) {
        navigateTo(actionEvent, "report.fxml", "Отчеты");
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }

    private void navigateTo(ActionEvent actionEvent, String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(MainController.class.getResource(fxml));
            Scene scene = new Scene(loader.load(), 1200, 600);
            Stage stage = (Stage) ((MenuItem) actionEvent.getSource()).getParentPopup().getOwnerWindow();
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}