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

public class ClientController {

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
    }

    @FXML
    public void onAdd(ActionEvent actionEvent) {
        showClientDialog(null);
    }

    @FXML
    public void onEdit(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите клиента для редактирования");
            return;
        }
        showClientDialog(selected);
    }

    @FXML
    public void onDelete(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите клиента для удаления");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы уверены, что хотите удалить клиента \"" +
                selected.getSurname() + " " + selected.getNameClient() + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int deleteResult = clientsDAO.deleteClientToArchive(selected.getIdClient());
            String message;
            Alert.AlertType alertType;

            if (deleteResult == 1) {
                message = "Клиент архивирован (есть связанные данные)";
                alertType = Alert.AlertType.WARNING;
            } else {
                message = "Клиент полностью удален";
                alertType = Alert.AlertType.INFORMATION;
            }

            showAlert("Результат", message, alertType);
            refreshTable();
        }
    }

    @FXML
    public void onFind(ActionEvent actionEvent) {
        String searchText = tfSearch.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            refreshTable();
        } else {
            clientsList.setAll(clientsDAO.search(searchText));
            tvClients.setItems(clientsList);
        }
    }

    @FXML
    public void onLoyaltyBase(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите клиента");
            return;
        }

        LoyaltyBase existingLoyalty = loyaltyBaseDAO.findByClient(selected.getIdClient());

        if (existingLoyalty == null) {
            // Предлагаем создать карту лояльности
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Создание карты лояльности");
            confirm.setHeaderText(null);
            confirm.setContentText("У клиента нет карты лояльности. Создать?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                showLoyaltyDialog(selected, null);
            }
        } else {
            // Показываем существующую карту
            showLoyaltyDialog(selected, existingLoyalty);
        }
    }

    private void showClientDialog(Clients client) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ClientController.class.getResource("add-edit-clients.fxml"));
            Scene scene = new Scene(loader.load(), 300, 285);

            NewClientController controller = loader.getController();
            Stage stage = new Stage();

            controller.setStage(stage);
            controller.setClient(client);
            controller.setClientsDAO(clientsDAO);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(client == null ? "Добавление клиента" : "Редактирование клиента");
            stage.setScene(scene);
            stage.showAndWait();

            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showLoyaltyDialog(Clients client, LoyaltyBase loyalty) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ClientController.class.getResource("loyality-base.fxml"));
            Scene scene = new Scene(loader.load(), 300, 210);

            LoyaltyBaseController controller = loader.getController();
            Stage stage = new Stage();

            controller.setStage(stage);
            controller.setClient(client);
            controller.setLoyalty(loyalty);
            controller.setLoyaltyBaseDAO(loyaltyBaseDAO);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Карта лояльности");
            stage.setScene(scene);
            stage.showAndWait();

            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onMain(ActionEvent actionEvent) {
        navigateTo(actionEvent, "main.fxml", "Главная");
    }

    @FXML
    public void onWarehouse(ActionEvent actionEvent) {
        navigateTo(actionEvent, "warehouse.fxml", "Склад");
    }

    @FXML
    public void onOrder(ActionEvent actionEvent) {
        navigateTo(actionEvent, "orders.fxml", "Заказы");
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
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource(fxml));
            Scene scene = new Scene(loader.load(), 1200, 600);
            Stage stage = (Stage) ((MenuItem) actionEvent.getSource())
                    .getParentPopup().getOwnerWindow();
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

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void onNotifications(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите клиента");
            return;
        }
        showNotificationsDialog(selected);
    }


    @FXML
    public void onChecks(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите клиента");
            return;
        }
        showRefundDialog(selected);
    }

    private void showRefundDialog(Clients client) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ClientController.class.getResource("refund.fxml"));
            Scene scene = new Scene(loader.load(), 935, 400);

            RefundController controller = loader.getController();
            Stage stage = new Stage();

            controller.setStage(stage);
            controller.setClient(client);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Возврат товаров - " + client.getSurname() + " " + client.getNameClient());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onMaking(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите клиента");
            return;
        }
        showMakingDialog(selected);
    }

    private void showNotificationsDialog(Clients client) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ClientController.class.getResource("notifications.fxml"));
            Scene scene = new Scene(loader.load(), 935, 400);

            NotificationsController controller = loader.getController();
            Stage stage = new Stage();

            controller.setStage(stage);
            controller.setClient(client);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Уведомления");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMakingDialog(Clients client) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ClientController.class.getResource("making-a-purchase.fxml"));
            Scene scene = new Scene(loader.load(), 860, 342);

            MakingController controller = loader.getController();
            Stage stage = new Stage();

            controller.setStage(stage);
            controller.setClient(client);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Оформление покупки");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}