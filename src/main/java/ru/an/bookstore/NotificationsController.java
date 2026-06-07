package ru.an.bookstore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.Optional;

public class NotificationsController {

    private final NotificationsDAO notificationsDAO = new NotificationsDAO();
    private ObservableList<Notifications> notificationsList = FXCollections.observableArrayList();
    private Stage stage;
    private Clients client;

    @FXML private TableView<Notifications> tvNotifications;
    @FXML private TableColumn<Notifications, String> colText;
    @FXML private TableColumn<Notifications, String> colDate;
    @FXML private TableColumn<Notifications, String> colStatus;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setClient(Clients client) {
        this.client = client;
        refreshTable();
    }

    @FXML
    void initialize() {
        colText.setCellValueFactory(new PropertyValueFactory<>("textNotification"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateNotification"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tvNotifications.setItems(notificationsList);
    }

    private void refreshTable() {
        notificationsList.setAll(notificationsDAO.findByClient(client.getIdClient()));
        tvNotifications.setItems(notificationsList);
    }

    @FXML
    public void onDelete(ActionEvent actionEvent) {
        Notifications selected = tvNotifications.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите уведомление для удаления");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить уведомление?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            notificationsDAO.deleteById(selected.getIdNotification());
            refreshTable();
        }
    }

    @FXML
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