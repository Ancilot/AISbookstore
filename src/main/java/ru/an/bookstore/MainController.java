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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class MainController {

    private final BookCatalogDAO dao = new BookCatalogDAO();
    public TextField tfFind;

    private ObservableList<BookCatalog> books =
            FXCollections.observableArrayList();

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
    }

    public void onExit(ActionEvent actionEvent) {
            Platform.exit();

    }

    public void onAdd(ActionEvent actionEvent) {
        shomDialog(null);
    }

    public void onEdit(ActionEvent actionEvent) {
        BookCatalog selected = tvBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите книгу для редактирования", Alert.AlertType.WARNING);
            return;
        }
        shomDialog(selected);
    }

    private void shomDialog(BookCatalog book) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource("adding-and-editing.fxml"));

            Scene scene = new Scene(loader.load(), 1200, 600);

            // Получаем контроллер
            NewBookController controller = loader.getController();

            // Создаём окно
            Stage stage = new Stage();

            // Передаём данные контроллеру
            controller.setBook(book);
            controller.setStage(stage);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Книга");
            stage.setScene(scene);

            stage.showAndWait();

            refreshTable();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onClient(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource("clients.fxml"));

            Scene scene = new Scene(loader.load(), 1200, 600);

            Stage stage = (Stage) ((MenuItem) actionEvent.getSource())
                    .getParentPopup()
                    .getOwnerWindow();

            stage.setTitle("Клиенты");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onWarehouse(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource("warehouse.fxml"));

            Scene scene = new Scene(loader.load(), 1200, 600);

            Stage stage = (Stage) ((MenuItem) actionEvent.getSource())
                    .getParentPopup()
                    .getOwnerWindow();

            stage.setTitle("Склад");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onOrder(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource("orders.fxml"));

            Scene scene = new Scene(loader.load(), 1200, 600);

            Stage stage = (Stage) ((MenuItem) actionEvent.getSource())
                    .getParentPopup()
                    .getOwnerWindow();

            stage.setTitle("Заказы");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void OnReport(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource("report.fxml"));

            Scene scene = new Scene(loader.load(), 1200, 600);

            // Получаем контроллер
            ReportController controller = loader.getController();

            Stage stage = (Stage) ((MenuItem) actionEvent.getSource())
                    .getParentPopup()
                    .getOwnerWindow();

            stage.setTitle("Отчеты");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDelete(ActionEvent actionEvent) {
        BookCatalog selected = tvBooks.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Предупреждение", "Пожалуйста, выберите книгу для удаления", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы уверены, что хотите удалить книгу \"" + selected.getNameBook() + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dao.delete(selected);
                refreshTable();
                showAlert("Успех", "Книга успешно удалена", Alert.AlertType.INFORMATION);
            } catch (RuntimeException e) {
                // Показываем пользователю сообщение от триггера
                String errorMessage = e.getMessage();
                if (errorMessage != null) {
                    showAlert("Невозможно удалить книгу", errorMessage, Alert.AlertType.WARNING);
                } else {
                    showAlert("Ошибка", "Не удалось удалить книгу", Alert.AlertType.ERROR);
                }
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void onFind(ActionEvent actionEvent) {
        String text = tfFind.getText();

        if (text == null || text.trim().isEmpty()) {
            refreshTable();
            return;
        }

        tvBooks.getItems().setAll(dao.search(text));
    }
}