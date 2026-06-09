package ru.an.bookstore;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class LoyaltyBaseController {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyBaseController.class);

    @FXML private ResourceBundle resources;
    @FXML private TextField tfCard;
    @FXML private TextField tfDate;
    @FXML private TextField tfNumeric;
    @FXML private TextField tfDiscount;

    private Stage stage;
    private Clients client;
    private LoyaltyBase loyalty;
    private LoyaltyBaseDAO loyaltyBaseDAO;

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setClient(Clients client) {
        this.client = client;
        log.debug("Установлен клиент для карты лояльности: id={}, name={} {}",
                client.getIdClient(), client.getSurname(), client.getNameClient());
    }

    public void setLoyalty(LoyaltyBase loyalty) {
        this.loyalty = loyalty;
        if (loyalty != null) {
            log.debug("Загружена существующая карта лояльности: clientId={}, cardNumber={}",
                    client != null ? client.getIdClient() : "null", loyalty.getCardNumber());
            fillFormFromLoyalty();
        } else {
            log.debug("Создание новой карты лояльности для клиента id={}",
                    client != null ? client.getIdClient() : "null");
        }
    }

    public void setLoyaltyBaseDAO(LoyaltyBaseDAO loyaltyBaseDAO) {
        this.loyaltyBaseDAO = loyaltyBaseDAO;
    }

    private void fillFormFromLoyalty() {
        tfCard.setText(loyalty.getCardNumber());
        tfDate.setText(loyalty.getDateCard().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        tfNumeric.setText(loyalty.getRansomAmount().toString());
        tfDiscount.setText(loyalty.getDiscount());
        log.debug("Форма заполнена данными карты: cardNumber={}, date={}, amount={}, discount={}",
                loyalty.getCardNumber(), loyalty.getDateCard(), loyalty.getRansomAmount(), loyalty.getDiscount());
    }

    private boolean validateFields() {
        String cardNumber = tfCard.getText().trim();
        if (cardNumber.isEmpty()) {
            log.warn("Ошибка валидации: пустой номер карты для клиента id={}",
                    client != null ? client.getIdClient() : "null");
            showAlert(resources.getString("loyalty.alert.error.empty_card"));
            return false;
        }
        if (!cardNumber.matches("^[0-9]{16}$")) {
            log.warn("Ошибка валидации: неверный формат номера карты '{}' для клиента id={}",
                    cardNumber, client != null ? client.getIdClient() : "null");
            showAlert(resources.getString("loyalty.alert.error.invalid_card"));
            return false;
        }
        return true;
    }

    @FXML
    public void onSave(ActionEvent actionEvent) {
        log.debug("Сохранение карты лояльности для клиента id={}",
                client != null ? client.getIdClient() : "null");

        if (!validateFields()) {
            return;
        }

        if (loyalty == null) {
            log.info("Создание новой карты лояльности для клиента id={}, cardNumber={}",
                    client.getIdClient(), tfCard.getText().trim());
            loyalty = new LoyaltyBase();
            loyalty.setClient(client);
            loyalty.setCardNumber(tfCard.getText().trim());
            loyaltyBaseDAO.save(loyalty);
            showAlert(resources.getString("loyalty.alert.success.create"));
        } else {
            log.info("Обновление карты лояльности для клиента id={}, oldCard={}, newCard={}",
                    client.getIdClient(), loyalty.getCardNumber(), tfCard.getText().trim());
            loyalty.setCardNumber(tfCard.getText().trim());
            loyaltyBaseDAO.update(loyalty);
            showAlert(resources.getString("loyalty.alert.success.update"));
        }
        stage.close();
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
        log.debug("Закрытие окна карты лояльности для клиента id={}",
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