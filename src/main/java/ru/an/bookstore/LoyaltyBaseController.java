package ru.an.bookstore;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class LoyaltyBaseController {

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
    }

    public void setLoyalty(LoyaltyBase loyalty) {
        this.loyalty = loyalty;
        if (loyalty != null) {
            fillFormFromLoyalty();
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
    }

    private boolean validateFields() {
        String cardNumber = tfCard.getText().trim();
        if (cardNumber.isEmpty()) {
            showAlert(resources.getString("loyalty.alert.error.empty_card"));
            return false;
        }
        if (!cardNumber.matches("^[0-9]{16}$")) {
            showAlert(resources.getString("loyalty.alert.error.invalid_card"));
            return false;
        }
        return true;
    }

    @FXML
    public void onSave(ActionEvent actionEvent) {
        if (!validateFields()) {
            return;
        }

        if (loyalty == null) {
            loyalty = new LoyaltyBase();
            loyalty.setClient(client);
            loyalty.setCardNumber(tfCard.getText().trim());
            loyaltyBaseDAO.save(loyalty);
            showAlert(resources.getString("loyalty.alert.success.create"));
        } else {
            loyalty.setCardNumber(tfCard.getText().trim());
            loyaltyBaseDAO.update(loyalty);
            showAlert(resources.getString("loyalty.alert.success.update"));
        }
        stage.close();
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