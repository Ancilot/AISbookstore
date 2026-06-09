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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML
    private ResourceBundle resources;

    private final BookCatalogDAO dao = new BookCatalogDAO();

    @FXML
    public TextField tfFind;

    private ObservableList<BookCatalog> books = FXCollections.observableArrayList();

    @FXML
    private TableView<BookCatalog> tvBooks;
    @FXML
    private TableColumn<BookCatalog, LocalDate> yesrColumn;
    @FXML
    private TableColumn<BookCatalog, BigDecimal> priceColumn;
    @FXML
    private TableColumn<BookCatalog, Integer> quantityColumn;
    @FXML
    private TableColumn<BookCatalog, String> nameColumn;
    @FXML
    private TableColumn<BookCatalog, String> genresColumn;
    @FXML
    private TableColumn<BookCatalog, String> athorColumn;
    @FXML
    private TableColumn<BookCatalog, String> publisherColumn;
    @FXML
    private TableColumn<BookCatalog, String> isbnColumn;

    @FXML
    void initialize() {
        log.debug("Инициализация MainController");

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nameBook"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        genresColumn.setCellValueFactory(new PropertyValueFactory<>("genres"));
        publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publishingName"));
        athorColumn.setCellValueFactory(new PropertyValueFactory<>("authors"));
        yesrColumn.setCellValueFactory(new PropertyValueFactory<>("yearPublication"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        refreshTable();
    }

    private void refreshTable() {
        tvBooks.getItems().setAll(dao.findAll());
        log.debug("Таблица книг обновлена, загружено {} записей", tvBooks.getItems().size());
    }

    public void onExit(ActionEvent actionEvent) {
        log.info("Завершение работы приложения");
        Platform.exit();
    }

    public void onAdd(ActionEvent actionEvent) {
        log.debug("Открытие диалога добавления книги");
        showDialog(null);
    }

    public void onEdit(ActionEvent actionEvent) {
        BookCatalog selected = tvBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.warn("Попытка редактирования без выбора книги");
            showAlert(resources.getString("main.alert.warning.select_book"),
                    Alert.AlertType.WARNING);
            return;
        }
        log.debug("Открытие диалога редактирования книги id={}, name='{}'",
                selected.getIdBook(), selected.getNameBook());
        showDialog(selected);
    }

    private void showDialog(BookCatalog book) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource("adding-and-editing.fxml"),
                    resources);
            Scene scene = new Scene(loader.load(), 1200, 600);

            NewBookController controller = loader.getController();
            Stage stage = new Stage();

            controller.setBook(book);
            controller.setStage(stage);
            controller.setResources(resources);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(book == null ?
                    resources.getString("app.title.add_book") :
                    resources.getString("app.title.edit_book"));
            stage.setScene(scene);
            stage.showAndWait();

            log.debug("Диалог книги закрыт, обновление таблицы");
            refreshTable();

        } catch (IOException e) {
            log.error("Ошибка загрузки FXML adding-and-editing.fxml", e);
            e.printStackTrace();
        }
    }

    public void onClient(ActionEvent actionEvent) {
        log.debug("Навигация: клиенты");
        navigateTo("clients.fxml", resources.getString("app.title.clients"), actionEvent);
    }

    public void onWarehouse(ActionEvent actionEvent) {
        log.debug("Навигация: склад");
        navigateTo("warehouse.fxml", resources.getString("app.title.warehouse"), actionEvent);
    }

    public void onOrder(ActionEvent actionEvent) {
        log.debug("Навигация: заказы");
        navigateTo("orders.fxml", resources.getString("app.title.orders"), actionEvent);
    }

    public void OnReport(ActionEvent actionEvent) {
        log.debug("Навигация: отчёты");
        navigateTo("report.fxml", resources.getString("app.title.reports"), actionEvent);
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

    public void onDelete(ActionEvent actionEvent) {
        BookCatalog selected = tvBooks.getSelectionModel().getSelectedItem();

        if (selected == null) {
            log.warn("Попытка удаления без выбора книги");
            showAlert(resources.getString("main.alert.warning.select_book_delete"),
                    Alert.AlertType.WARNING);
            return;
        }

        log.info("Запрос на удаление книги id={}, name='{}'", selected.getIdBook(), selected.getNameBook());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(resources.getString("main.alert.confirm.delete_title"));
        confirm.setHeaderText(null);
        confirm.setContentText(java.text.MessageFormat.format(
                resources.getString("main.alert.confirm.delete_text"),
                selected.getNameBook()));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dao.delete(selected);
                log.info("Книга id={} успешно удалена", selected.getIdBook());
                refreshTable();
                showAlert(resources.getString("main.alert.success.delete"),
                        Alert.AlertType.INFORMATION);
            } catch (RuntimeException e) {
                log.error("Ошибка удаления книги id={}: {}", selected.getIdBook(), e.getMessage());
                showAlert(resources.getString("main.alert.error.delete"),
                        Alert.AlertType.ERROR);
            }
        } else {
            log.debug("Удаление книги id={} отменено", selected.getIdBook());
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

    public void onFind(ActionEvent actionEvent) {
        String text = tfFind.getText();
        log.debug("Поиск книг: searchText='{}'", text);

        if (text == null || text.trim().isEmpty()) {
            refreshTable();
            return;
        }

        tvBooks.getItems().setAll(dao.search(text));
        log.debug("Найдено {} книг по запросу '{}'", tvBooks.getItems().size(), text);
    }
}