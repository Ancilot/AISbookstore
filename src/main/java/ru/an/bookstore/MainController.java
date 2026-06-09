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
import java.util.ResourceBundle;

public class MainController {
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
        showDialog(null);
    }

    public void onEdit(ActionEvent actionEvent) {
        BookCatalog selected = tvBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(resources.getString("main.alert.warning.select_book"),
                    Alert.AlertType.WARNING);
            return;
        }
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

            refreshTable();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClient(ActionEvent actionEvent) {
        navigateTo("clients.fxml", resources.getString("app.title.clients"), actionEvent);
    }

    public void onWarehouse(ActionEvent actionEvent) {
        navigateTo("warehouse.fxml", resources.getString("app.title.warehouse"), actionEvent);
    }

    public void onOrder(ActionEvent actionEvent) {
        navigateTo("orders.fxml", resources.getString("app.title.orders"), actionEvent);
    }

    public void OnReport(ActionEvent actionEvent) {
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDelete(ActionEvent actionEvent) {
        BookCatalog selected = tvBooks.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(resources.getString("main.alert.warning.select_book_delete"),
                    Alert.AlertType.WARNING);
            return;
        }

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
                refreshTable();
                showAlert(resources.getString("main.alert.success.delete"),
                        Alert.AlertType.INFORMATION);
            } catch (RuntimeException e) {
                showAlert(resources.getString("main.alert.error.delete"),
                        Alert.AlertType.ERROR);
            }
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

        if (text == null || text.trim().isEmpty()) {
            refreshTable();
            return;
        }

        tvBooks.getItems().setAll(dao.search(text));
    }
}