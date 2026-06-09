package ru.an.bookstore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class RefundController {

    @FXML private ResourceBundle resources;
    @FXML private TableView<CompositionCheck> tvRefundItems;
    @FXML private TableColumn<CompositionCheck, String> colBookName;
    @FXML private TableColumn<CompositionCheck, String> colDateTime;
    @FXML private TableColumn<CompositionCheck, Integer> colQuantity;
    @FXML private TableColumn<CompositionCheck, String> colPrice;
    @FXML private TableColumn<CompositionCheck, String> colStatus;
    @FXML private TextField tfSearch;
    @FXML private TextField tfReturnQuantity;

    private Stage stage;
    private Clients client;
    private List<CompositionCheck> allCheckItems;
    private final CompositionCheckDAO compositionCheckDAO = new CompositionCheckDAO();

    private ObservableList<CompositionCheck> refundItems = FXCollections.observableArrayList();

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    @FXML
    void initialize() {
        colBookName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getBook().getNameBook()));
        colDateTime.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getChecks().getDateTime().toString()));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getPriceTime().toString()));
        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getChecks().getStatus()));

        tvRefundItems.setItems(refundItems);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setClient(Clients client) {
        this.client = client;
        loadClientChecks();
    }

    private void loadClientChecks() {
        allCheckItems = compositionCheckDAO.findByClient(client.getIdClient());
        refundItems.setAll(allCheckItems);
    }

    @FXML
    public void onFind(ActionEvent actionEvent) {
        String searchText = tfSearch.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            refundItems.setAll(allCheckItems);
        } else {
            ObservableList<CompositionCheck> filtered = refundItems.filtered(item ->
                    item.getBook().getNameBook().toLowerCase().contains(searchText.toLowerCase()));
            refundItems.setAll(filtered);
        }
    }

    @FXML
    public void onRefund(ActionEvent actionEvent) {
        CompositionCheck selected = tvRefundItems.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(resources.getString("refund.alert.error.select_book"));
            return;
        }

        String returnQtyText = tfReturnQuantity.getText();
        if (returnQtyText == null || returnQtyText.trim().isEmpty()) {
            showAlert(resources.getString("refund.alert.error.enter_quantity"));
            return;
        }

        int returnQuantity;
        try {
            returnQuantity = Integer.parseInt(returnQtyText);
        } catch (NumberFormatException e) {
            showAlert(resources.getString("refund.alert.error.invalid_quantity"));
            return;
        }

        if (returnQuantity <= 0) {
            showAlert(resources.getString("refund.alert.error.quantity_positive"));
            return;
        }

        if (returnQuantity > selected.getQuantity()) {
            showAlert(java.text.MessageFormat.format(
                    resources.getString("refund.alert.error.too_many"),
                    selected.getQuantity()));
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(resources.getString("refund.alert.confirm.title"));
        confirm.setHeaderText(null);
        confirm.setContentText(java.text.MessageFormat.format(
                resources.getString("refund.alert.confirm.text"),
                returnQuantity,
                selected.getBook().getNameBook(),
                selected.getPriceTime()));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                compositionCheckDAO.returnFromCheck(selected.getIdComposition(), returnQuantity);
                showAlert(resources.getString("refund.alert.success"));

                loadClientChecks();
                tfReturnQuantity.clear();
            } catch (RuntimeException e) {
                showAlert(resources.getString("refund.alert.error.general"));
            }
        }
    }

    @FXML
    public void onBack(ActionEvent actionEvent) {
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