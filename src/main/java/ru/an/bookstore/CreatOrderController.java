package ru.an.bookstore;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreatOrderController {

    @FXML private TextField tfQuantity;
    @FXML private TextField tfComment;

    private Stage stage;
    private Clients client;
    private BookCatalog book;
    private final OrdersDAO ordersDAO = new OrdersDAO();

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
            showAlert("Ошибка", "Введите количество");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showAlert("Ошибка", "Количество должно быть больше 0");
                return;
            }

            Orders order = new Orders();
            order.setClient(client);
            order.setBook(book);
            order.setQuantity(quantity);
            order.setTextOrder(tfComment.getText());
            ordersDAO.save(order);

            showAlert("Успех", "Заказ создан! Уведомление отправлено менеджеру.");
            stage.close();
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректное количество");
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