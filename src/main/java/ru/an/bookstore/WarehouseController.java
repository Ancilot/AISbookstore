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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;

public class WarehouseController {

    private static final Logger log = LoggerFactory.getLogger(WarehouseController.class);

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
        log.debug("Инициализация WarehouseController, showArchive={}", showArchive);

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
            log.debug("Таблица склада обновлена (архив), загружено {} записей", warehouseList.size());
        } else {
            warehouseList.setAll(warehouseDAO.findAllActive());
            log.debug("Таблица склада обновлена (активные), загружено {} записей", warehouseList.size());
        }
        tvWarehouse.setItems(warehouseList);
    }

    public void onFind(ActionEvent actionEvent) {
        String searchText = tfFind.getText();
        log.debug("Поиск на складе: searchText='{}', showArchive={}", searchText, showArchive);

        if (searchText == null || searchText.trim().isEmpty()) {
            refreshTable();
        } else {
            if (showArchive) {
                warehouseList.setAll(warehouseDAO.searchArchive(searchText));
            } else {
                warehouseList.setAll(warehouseDAO.searchActive(searchText));
            }
            tvWarehouse.setItems(warehouseList);
            log.debug("Найдено {} записей по запросу '{}'", warehouseList.size(), searchText);
        }
    }

    public void onAdd(ActionEvent actionEvent) {
        log.debug("Открытие диалога добавления книги на склад");
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

            log.debug("Диалог добавления закрыт, обновление таблицы");
            refreshTable();
        } catch (IOException e) {
            log.error("Ошибка загрузки FXML adding-to-the-warehouse.fxml", e);
            e.printStackTrace();
        }
    }

    public void onDelete(ActionEvent actionEvent) {
        Warehouse selected = tvWarehouse.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.warn("Попытка удаления/архивации книги без выбора");
            showAlert(resources.getString("warehouse.alert.warning.select_book"), Alert.AlertType.WARNING);
            return;
        }

        String bookName = selected.getBook().getNameBook();
        Long bookIdLong = selected.getBook().getIdBook();
        int bookId = bookIdLong != null ? bookIdLong.intValue() : 0;

        log.info("Запрос на удаление/архивацию книги: id={}, name='{}'", bookId, bookName);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(resources.getString("warehouse.alert.confirm.delete_title"));
        confirm.setHeaderText(null);
        confirm.setContentText(java.text.MessageFormat.format(
                resources.getString("warehouse.alert.confirm.delete_text"),
                bookName));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int deleteResult = warehouseDAO.deleteBookToArchive(bookId);

            String message;
            Alert.AlertType alertType;

            switch (deleteResult) {
                case 1:
                    log.info("Книга '{}' отправлена в архив", bookName);
                    message = java.text.MessageFormat.format(
                            resources.getString("warehouse.alert.result.archived"),
                            bookName);
                    alertType = Alert.AlertType.WARNING;
                    break;
                case 2:
                    log.info("Книга '{}' полностью удалена", bookName);
                    message = java.text.MessageFormat.format(
                            resources.getString("warehouse.alert.result.deleted"),
                            bookName);
                    alertType = Alert.AlertType.INFORMATION;
                    break;
                default:
                    log.error("Ошибка при удалении/архивации книги '{}'", bookName);
                    message = java.text.MessageFormat.format(
                            resources.getString("warehouse.alert.result.error"),
                            bookName);
                    alertType = Alert.AlertType.ERROR;
                    break;
            }

            showAlert(message, alertType);
            refreshTable();
        } else {
            log.debug("Удаление/архивация книги '{}' отменена", bookName);
        }
    }

    public void onInvoice(ActionEvent actionEvent) {
        Warehouse selected = tvWarehouse.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.warn("Попытка создания накладной без выбора книги");
            showAlert(resources.getString("warehouse.alert.warning.select_for_invoice"), Alert.AlertType.WARNING);
            return;
        }

        log.debug("Открытие диалога создания накладной для книги id={}, name='{}'",
                selected.getBook().getIdBook(), selected.getBook().getNameBook());

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

            log.debug("Диалог накладной закрыт, обновление таблицы");
            refreshTable();
        } catch (IOException e) {
            log.error("Ошибка загрузки FXML invoice.fxml", e);
            e.printStackTrace();
        }
    }

    public void onMain(ActionEvent actionEvent) {
        log.debug("Навигация: главное меню");
        navigateTo("main.fxml", resources.getString("app.title"), actionEvent);
    }

    public void onClirnt(ActionEvent actionEvent) {
        log.debug("Навигация: клиенты");
        navigateTo("clients.fxml", resources.getString("app.title.clients"), actionEvent);
    }

    public void onOreder(ActionEvent actionEvent) {
        log.debug("Навигация: заказы");
        navigateTo("orders.fxml", resources.getString("app.title.orders"), actionEvent);
    }

    public void onReprt(ActionEvent actionEvent) {
        log.debug("Навигация: отчёты");
        navigateTo("report.fxml", resources.getString("app.title.reports"), actionEvent);
    }

    public void onExit(ActionEvent actionEvent) {
        log.info("Завершение работы приложения");
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