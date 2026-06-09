package ru.an.bookstore;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class CreatOrderController {

    @FXML private ResourceBundle resources;
    @FXML private TextField tfQuantity;
    @FXML private TextField tfComment;

    private Stage stage;
    private Clients client;
    private BookCatalog book;
    private final OrdersDAO ordersDAO = new OrdersDAO();

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setClient(Clients client) {
        this.client = client;
    }

    public void setBook(BookCatalog book) {
        this.book = book;
    }

    public void setQuantity(String quantity) {
        tfQuantity.setText(quantity);
        tfQuantity.setEditable(false);
    }

    @FXML
    public void onSave(ActionEvent actionEvent) {
        String quantityText = tfQuantity.getText();
        if (quantityText == null || quantityText.trim().isEmpty()) {
            showAlert(resources.getString("create_order.alert.error.enter_quantity"));
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showAlert(resources.getString("create_order.alert.error.quantity_positive"));
                return;
            }

            Orders order = new Orders();
            order.setClient(client);
            order.setBook(book);
            order.setQuantity(quantity);
            order.setTextOrder(tfComment.getText());
            ordersDAO.save(order);

            showAlert(resources.getString("create_order.alert.success"));
            stage.close();
        } catch (NumberFormatException e) {
            showAlert(resources.getString("create_order.alert.error.invalid_number"));
        }
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
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