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
import java.util.ResourceBundle;

public class ClientController {

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
            showAlert(resources.getString("clients.alert.warning.select_for_edit"), Alert.AlertType.WARNING);
            return;
        }
        showClientDialog(selected);
    }

    @FXML
    public void onDelete(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(resources.getString("clients.alert.warning.select_for_delete"), Alert.AlertType.WARNING);
            return;
        }

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
                message = resources.getString("clients.alert.result.archived");
                alertType = Alert.AlertType.WARNING;
            } else {
                message = resources.getString("clients.alert.result.deleted");
                alertType = Alert.AlertType.INFORMATION;
            }

            showAlert(message, alertType);
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
            showAlert(resources.getString("clients.alert.warning.select_client"), Alert.AlertType.WARNING);
            return;
        }

        LoyaltyBase existingLoyalty = loyaltyBaseDAO.findByClient(selected.getIdClient());

        if (existingLoyalty == null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle(resources.getString("loyalty.alert.confirm.create_title"));
            confirm.setHeaderText(null);
            confirm.setContentText(resources.getString("loyalty.alert.confirm.create_text"));

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                showLoyaltyDialog(selected, null);
            }
        } else {
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

            refreshTable();
        } catch (IOException e) {
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

            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onMain(ActionEvent actionEvent) {
        navigateTo("main.fxml", resources.getString("app.title"), actionEvent);
    }

    @FXML
    public void onWarehouse(ActionEvent actionEvent) {
        navigateTo("warehouse.fxml", resources.getString("app.title.warehouse"), actionEvent);
    }

    @FXML
    public void onOrder(ActionEvent actionEvent) {
        navigateTo("orders.fxml", resources.getString("app.title.orders"), actionEvent);
    }

    @FXML
    public void onReport(ActionEvent actionEvent) {
        navigateTo("report.fxml", resources.getString("app.title.reports"), actionEvent);
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
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
        } catch (IOException e) {
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
            showAlert(resources.getString("clients.alert.warning.select_client"), Alert.AlertType.WARNING);
            return;
        }
        showNotificationsDialog(selected);
    }

    @FXML
    public void onChecks(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(resources.getString("clients.alert.warning.select_client"), Alert.AlertType.WARNING);
            return;
        }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onMaking(ActionEvent actionEvent) {
        Clients selected = tvClients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(resources.getString("clients.alert.warning.select_client"), Alert.AlertType.WARNING);
            return;
        }
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
        } catch (IOException e) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}