package ru.an.bookstore;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

public class CreatOrderController {

    private static final Logger log = LoggerFactory.getLogger(CreatOrderController.class);

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
        log.debug("Установлен клиент для заказа: id={}, name={} {}",
                client.getIdClient(), client.getSurname(), client.getNameClient());
    }

    public void setBook(BookCatalog book) {
        this.book = book;
        log.debug("Установлена книга для заказа: id={}, name='{}'",
                book.getIdBook(), book.getNameBook());
    }

    public void setQuantity(String quantity) {
        tfQuantity.setText(quantity);
        tfQuantity.setEditable(false);
        log.debug("Установлено количество для заказа: {}", quantity);
    }

    @FXML
    public void onSave(ActionEvent actionEvent) {
        String quantityText = tfQuantity.getText();
        if (quantityText == null || quantityText.trim().isEmpty()) {
            log.warn("Попытка создания заказа без указания количества");
            showAlert(resources.getString("create_order.alert.error.enter_quantity"));
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                log.warn("Попытка создания заказа с неположительным количеством: {}", quantity);
                showAlert(resources.getString("create_order.alert.error.quantity_positive"));
                return;
            }

            log.info("Создание заказа: clientId={}, bookId={}, quantity={}, comment='{}'",
                    client.getIdClient(), book.getIdBook(), quantity, tfComment.getText());

            Orders order = new Orders();
            order.setClient(client);
            order.setBook(book);
            order.setQuantity(quantity);
            order.setTextOrder(tfComment.getText());
            ordersDAO.save(order);

            log.info("Заказ успешно создан для клиента id={}, книга id={}",
                    client.getIdClient(), book.getIdBook());
            showAlert(resources.getString("create_order.alert.success"));
            stage.close();
        } catch (NumberFormatException e) {
            log.warn("Ошибка парсинга количества: value='{}'", quantityText);
            showAlert(resources.getString("create_order.alert.error.invalid_number"));
        }
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
        log.debug("Закрытие окна создания заказа");
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