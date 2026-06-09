package ru.an.bookstore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.ResourceBundle;

public class NotificationsController {

    private static final Logger log = LoggerFactory.getLogger(NotificationsController.class);

    @FXML private ResourceBundle resources;

    private final NotificationsDAO notificationsDAO = new NotificationsDAO();
    private ObservableList<Notifications> notificationsList = FXCollections.observableArrayList();
    private Stage stage;
    private Clients client;

    @FXML private TableView<Notifications> tvNotifications;
    @FXML private TableColumn<Notifications, String> colText;
    @FXML private TableColumn<Notifications, String> colDate;
    @FXML private TableColumn<Notifications, String> colStatus;

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setClient(Clients client) {
        this.client = client;
        log.debug("Загрузка уведомлений для клиента id={}, name={} {}",
                client.getIdClient(), client.getSurname(), client.getNameClient());
        refreshTable();
    }

    @FXML
    void initialize() {
        log.debug("Инициализация NotificationsController");

        colText.setCellValueFactory(new PropertyValueFactory<>("textNotification"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateNotification"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tvNotifications.setItems(notificationsList);
    }

    private void refreshTable() {
        notificationsList.setAll(notificationsDAO.findByClient(client.getIdClient()));
        tvNotifications.setItems(notificationsList);
        log.debug("Загружено {} уведомлений для клиента id={}", notificationsList.size(), client.getIdClient());
    }

    @FXML
    public void onDelete(ActionEvent actionEvent) {
        Notifications selected = tvNotifications.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.warn("Попытка удаления уведомления без выбора для клиента id={}",
                    client != null ? client.getIdClient() : "null");
            showAlert(resources.getString("notifications.alert.warning.select"));
            return;
        }

        log.info("Запрос на удаление уведомления id={} для клиента id={}",
                selected.getIdNotification(), client.getIdClient());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(resources.getString("notifications.alert.confirm.delete_title"));
        confirm.setHeaderText(null);
        confirm.setContentText(resources.getString("notifications.alert.confirm.delete_text"));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            notificationsDAO.deleteById(selected.getIdNotification());
            log.info("Уведомление id={} удалено", selected.getIdNotification());
            refreshTable();
        } else {
            log.debug("Удаление уведомления id={} отменено", selected.getIdNotification());
        }
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
        log.debug("Закрытие окна уведомлений для клиента id={}",
                client != null ? client.getIdClient() : "null");
        stage.close();
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(resources.getString("alert.title.information"));
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}