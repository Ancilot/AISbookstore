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

public class ClientController {

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    @FXML
    private ResourceBundle resources;

    private final ClientsDAO clientsDAO = new ClientsDAO();
    private final LoyaltyBaseDAO loyaltyBaseDAO = new LoyaltyBaseDAO();
    private ObservableList<Clients> clientsList = FXCollections.observableArrayList();

    @FXML private TableView<Clients> tvClients;
    @FXML private TableColumn<Clients, String> colSurname;
    @FXML private TableColumn<Clients, String> colName;
    @FXML private TableColumn<Clients, String> colPatronymic;
    @FXML private TableColumn<Clients, String> colNumber;
    @FXML private TableColumn<Clients, String> colEmail;
    @FXML private TextField tfSearch;

    @FXML
    void initialize() {
        log.debug("Инициализация ClientController");

        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nameClient"));
        colPatronymic.setCellValueFactory(new PropertyValueFactory<>("patrontmic"));
        colNumber.setCellValueFactory(new PropertyValueFactory<>("numberClient"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        refreshTable();
    }

    private void refreshTable() {
        clientsList.setAll(clientsDAO.findAll());
        tvClients.setItems(clientsList);
        log.debug("Таблица клиентов обновлена, загружено {} записей", clientsList.size());
    }

    @FXML
    public void onAdd(ActionEvent actionEvent) {
        log.debug("Открытие диалога добавления клиента");
        showClientDialog(null);
    }

    @FXML
    public void onEdit(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.warn("Попытка редактирования без выбора клиента");
            showAlert(resources.getString("clients.alert.warning.select_for_edit"), Alert.AlertType.WARNING);
            return;
        }
        log.debug("Открытие диалога редактирования клиента id={}", selected.getIdClient());
        showClientDialog(selected);
    }

    @FXML
    public void onDelete(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.warn("Попытка удаления без выбора клиента");
            showAlert(resources.getString("clients.alert.warning.select_for_delete"), Alert.AlertType.WARNING);
            return;
        }

        log.info("Запрос на удаление/архивацию клиента id={}, name={} {}",
                selected.getIdClient(), selected.getSurname(), selected.getNameClient());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(resources.getString("clients.alert.confirm.delete_title"));
        confirm.setHeaderText(null);
        confirm.setContentText(java.text.MessageFormat.format(
                resources.getString("clients.alert.confirm.delete_text"),
                selected.getSurname() + " " + selected.getNameClient()));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int deleteResult = clientsDAO.deleteClientToArchive(selected.getIdClient());
            String message;
            Alert.AlertType alertType;

            if (deleteResult == 1) {
                log.info("Клиент id={} отправлен в архив", selected.getIdClient());
                message = resources.getString("clients.alert.result.archived");
                alertType = Alert.AlertType.WARNING;
            } else {
                log.info("Клиент id={} полностью удалён", selected.getIdClient());
                message = resources.getString("clients.alert.result.deleted");
                alertType = Alert.AlertType.INFORMATION;
            }

            showAlert(message, alertType);
            refreshTable();
        } else {
            log.debug("Удаление клиента id={} отменено", selected.getIdClient());
        }
    }

    @FXML
    public void onFind(ActionEvent actionEvent) {
        String searchText = tfSearch.getText();
        log.debug("Поиск клиентов: searchText='{}'", searchText);

        if (searchText == null || searchText.trim().isEmpty()) {
            refreshTable();
        } else {
            clientsList.setAll(clientsDAO.search(searchText));
            tvClients.setItems(clientsList);
            log.debug("Найдено {} клиентов по запросу '{}'", clientsList.size(), searchText);
        }
    }

    @FXML
    public void onLoyaltyBase(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.warn("Попытка открытия карты лояльности без выбора клиента");
            showAlert(resources.getString("clients.alert.warning.select_client"), Alert.AlertType.WARNING);
            return;
        }

        log.debug("Проверка наличия карты лояльности для клиента id={}", selected.getIdClient());
        LoyaltyBase existingLoyalty = loyaltyBaseDAO.findByClient(selected.getIdClient());

        if (existingLoyalty == null) {
            log.debug("Карта лояльности не найдена, запрос на создание для клиента id={}", selected.getIdClient());
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle(resources.getString("loyalty.alert.confirm.create_title"));
            confirm.setHeaderText(null);
            confirm.setContentText(resources.getString("loyalty.alert.confirm.create_text"));

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                showLoyaltyDialog(selected, null);
            }
        } else {
            log.debug("Карта лояльности найдена для клиента id={}", selected.getIdClient());
            showLoyaltyDialog(selected, existingLoyalty);
        }
    }

    private void showClientDialog(Clients client) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ClientController.class.getResource("add-edit-clients.fxml"), resources);
            Scene scene = new Scene(loader.load(), 300, 285);

            NewClientController controller = loader.getController();
            Stage stage = new Stage();

            controller.setStage(stage);
            controller.setClient(client);
            controller.setClientsDAO(clientsDAO);
            controller.setResources(resources);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(client == null ?
                    resources.getString("app.title.add_client") :
                    resources.getString("app.title.edit_client"));
            stage.setScene(scene);
            stage.showAndWait();

            log.debug("Диалог клиента закрыт, обновление таблицы");
            refreshTable();
        } catch (IOException e) {
            log.error("Ошибка загрузки FXML add-edit-clients.fxml", e);
            e.printStackTrace();
        }
    }

    private void showLoyaltyDialog(Clients client, LoyaltyBase loyalty) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ClientController.class.getResource("loyality-base.fxml"), resources);
            Scene scene = new Scene(loader.load(), 300, 210);

            LoyaltyBaseController controller = loader.getController();
            Stage stage = new Stage();

            controller.setStage(stage);
            controller.setClient(client);
            controller.setLoyalty(loyalty);
            controller.setLoyaltyBaseDAO(loyaltyBaseDAO);
            controller.setResources(resources);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(resources.getString("app.title.loyalty"));
            stage.setScene(scene);
            stage.showAndWait();

            log.debug("Диалог карты лояльности закрыт для клиента id={}", client.getIdClient());
            refreshTable();
        } catch (IOException e) {
            log.error("Ошибка загрузки FXML loyality-base.fxml", e);
            e.printStackTrace();
        }
    }

    @FXML
    public void onMain(ActionEvent actionEvent) {
        log.debug("Навигация: главное меню");
        navigateTo("main.fxml", resources.getString("app.title"), actionEvent);
    }

    @FXML
    public void onWarehouse(ActionEvent actionEvent) {
        log.debug("Навигация: склад");
        navigateTo("warehouse.fxml", resources.getString("app.title.warehouse"), actionEvent);
    }

    @FXML
    public void onOrder(ActionEvent actionEvent) {
        log.debug("Навигация: заказы");
        navigateTo("orders.fxml", resources.getString("app.title.orders"), actionEvent);
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

    @FXML
    public void onNotifications(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.warn("Попытка просмотра уведомлений без выбора клиента");
            showAlert(resources.getString("clients.alert.warning.select_client"), Alert.AlertType.WARNING);
            return;
        }
        log.debug("Открытие уведомлений для клиента id={}", selected.getIdClient());
        showNotificationsDialog(selected);
    }

    @FXML
    public void onChecks(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.warn("Попытка просмотра чеков без выбора клиента");
            showAlert(resources.getString("clients.alert.warning.select_client"), Alert.AlertType.WARNING);
            return;
        }
        log.debug("Открытие возвратов для клиента id={}", selected.getIdClient());
        showRefundDialog(selected);
    }

    private void showRefundDialog(Clients client) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ClientController.class.getResource("refund.fxml"), resources);
            Scene scene = new Scene(loader.load(), 935, 400);

            RefundController controller = loader.getController();
            Stage stage = new Stage();

            controller.setStage(stage);
            controller.setClient(client);
            controller.setResources(resources);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(resources.getString("app.title.refund") + " - " + client.getSurname() + " " + client.getNameClient());
            stage.setScene(scene);
            stage.showAndWait();

            log.debug("Диалог возвратов закрыт для клиента id={}", client.getIdClient());
        } catch (IOException e) {
            log.error("Ошибка загрузки FXML refund.fxml", e);
            e.printStackTrace();
        }
    }

    @FXML
    public void onMaking(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.warn("Попытка оформления покупки без выбора клиента");
            showAlert(resources.getString("clients.alert.warning.select_client"), Alert.AlertType.WARNING);
            return;
        }
        log.info("Оформление покупки для клиента id={}, name={} {}",
                selected.getIdClient(), selected.getSurname(), selected.getNameClient());
        showMakingDialog(selected);
    }

    private void showNotificationsDialog(Clients client) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ClientController.class.getResource("notifications.fxml"), resources);
            Scene scene = new Scene(loader.load(), 935, 400);

            NotificationsController controller = loader.getController();
            Stage stage = new Stage();

            controller.setStage(stage);
            controller.setClient(client);
            controller.setResources(resources);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(resources.getString("app.title.notifications"));
            stage.setScene(scene);
            stage.showAndWait();

            log.debug("Диалог уведомлений закрыт для клиента id={}", client.getIdClient());
        } catch (IOException e) {
            log.error("Ошибка загрузки FXML notifications.fxml", e);
            e.printStackTrace();
        }
    }

    private void showMakingDialog(Clients client) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ClientController.class.getResource("making-a-purchase.fxml"), resources);
            Scene scene = new Scene(loader.load(), 860, 342);

            MakingController controller = loader.getController();
            Stage stage = new Stage();

            controller.setStage(stage);
            controller.setClient(client);
            controller.setResources(resources);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(resources.getString("app.title.making_purchase"));
            stage.setScene(scene);
            stage.showAndWait();

            log.debug("Диалог оформления покупки закрыт для клиента id={}", client.getIdClient());
        } catch (IOException e) {
            log.error("Ошибка загрузки FXML making-a-purchase.fxml", e);
            e.printStackTrace();
        }
    }
}