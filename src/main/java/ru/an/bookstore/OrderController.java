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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;

public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @FXML private ResourceBundle resources;

    private final OrdersDAO ordersDAO = new OrdersDAO();
    private ObservableList<Orders> ordersList = FXCollections.observableArrayList();

    @FXML private TableView<Orders> tvOrders;
    @FXML private TableColumn<Orders, String> colClient;
    @FXML private TableColumn<Orders, String> colDate;
    @FXML private TableColumn<Orders, String> colStatus;
    @FXML private TableColumn<Orders, String> colComment;
    @FXML private TableColumn<Orders, String> colBook;
    @FXML private TableColumn<Orders, Integer> colQuantity;
    @FXML private TextField tfSearch;

    @FXML
    void initialize() {
        log.debug("Инициализация OrderController");

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
        log.debug("Таблица заказов обновлена, загружено {} записей", ordersList.size());
    }

    @FXML
    public void onComplete(ActionEvent actionEvent) {
        Orders selected = tvOrders.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(resources.getString("orders.alert.warning.select_for_complete"), Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(resources.getString("orders.alert.confirm.complete_title"));
        confirm.setHeaderText(null);
        confirm.setContentText(java.text.MessageFormat.format(
                resources.getString("orders.alert.confirm.complete_text"),
                selected.getIdOrder()));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                log.info("Завершение заказа id={}", selected.getIdOrder());
                ordersDAO.completeOrder(selected.getIdOrder());
                showAlert(resources.getString("orders.alert.success.complete"), Alert.AlertType.INFORMATION);
                refreshTable();
            } catch (RuntimeException e) {
                log.error("Ошибка завершения заказа id={}", selected.getIdOrder(), e);
                showAlert(resources.getString("orders.alert.error.insufficient_stock"), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void onDelete(ActionEvent actionEvent) {
        Orders selected = tvOrders.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(resources.getString("orders.alert.warning.select_for_delete"), Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(resources.getString("orders.alert.confirm.complete_title"));
        confirm.setHeaderText(null);
        confirm.setContentText(java.text.MessageFormat.format(
                resources.getString("orders.alert.confirm.archive_text"),
                selected.getIdOrder()));

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                log.info("Архивация заказа id={}", selected.getIdOrder());
                ordersDAO.deleteOrderToArchive(selected.getIdOrder());
                showAlert(resources.getString("orders.alert.success.archive"), Alert.AlertType.INFORMATION);
                refreshTable();
            } catch (RuntimeException e) {
                log.error("Ошибка архивации заказа id={}", selected.getIdOrder(), e);
                showAlert(resources.getString("orders.alert.error.cannot_archive"), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void onFind(ActionEvent actionEvent) {
        String searchText = tfSearch.getText();
        log.debug("Поиск заказов: text='{}'", searchText);

        if (searchText == null || searchText.trim().isEmpty()) {
            refreshTable();
        } else {
            ordersList.setAll(ordersDAO.search(searchText));
            tvOrders.setItems(ordersList);
        }
    }

    @FXML
    public void onMain(ActionEvent actionEvent) {
        log.debug("Навигация: главное меню");
        navigateTo("main.fxml", resources.getString("app.title"), actionEvent);
    }

    @FXML
    public void onClient(ActionEvent actionEvent) {
        log.debug("Навигация: клиенты");
        navigateTo("clients.fxml", resources.getString("app.title.clients"), actionEvent);
    }

    @FXML
    public void onWarehouse(ActionEvent actionEvent) {
        log.debug("Навигация: склад");
        navigateTo("warehouse.fxml", resources.getString("app.title.warehouse"), actionEvent);
    }

    @FXML
    public void onReport(ActionEvent actionEvent) {
        log.debug("Навигация: отчёты");
        navigateTo("report.fxml", resources.getString("app.title.reports"), actionEvent);
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
        log.info("Завершение работы приложения");
        Platform.exit();
    }

    private void navigateTo(String fxml, String title, ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(MainController.class.getResource(fxml), resources);
            Scene scene = new Scene(loader.load(), 1200, 600);
            Stage stage = (Stage) ((MenuItem) actionEvent.getSource()).getParentPopup().getOwnerWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            log.debug("Успешная навигация на {}", fxml);
        } catch (IOException e) {
            log.error("Ошибка навигации на {}", fxml, e);
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