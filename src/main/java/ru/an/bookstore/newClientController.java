package ru.an.bookstore;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class newClientController {

    @FXML private TextField tfSurname;
    @FXML private TextField tfName;
    @FXML private TextField tfPatronomic;
    @FXML private TextField tfNumber;
    @FXML private TextField tfEmail;

    private Stage stage;
    private Clients client;
    private ClientsDAO clientsDAO;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setClient(Clients client) {
        this.client = client;
        if (client != null && client.getIdClient() != null) {
            fillFormFromClient();
        }
    }

    public void setClientsDAO(ClientsDAO clientsDAO) {
        this.clientsDAO = clientsDAO;
    }

    private void fillFormFromClient() {
        tfSurname.setText(client.getSurname());
        tfName.setText(client.getNameClient());
        tfPatronomic.setText(client.getPatrontmic());
        tfNumber.setText(client.getNumberClient());
        tfEmail.setText(client.getEmail());
    }

    private void fillClientFromForm() {
        if (client == null) {
            client = new Clients();
        }
        client.setSurname(tfSurname.getText().trim());
        client.setNameClient(tfName.getText().trim());
        client.setPatrontmic(tfPatronomic.getText().trim());
        client.setNumberClient(tfNumber.getText().trim());
        client.setEmail(tfEmail.getText().trim());
    }

    private boolean validateFields() {
        if (tfSurname.getText() == null || tfSurname.getText().trim().isEmpty()) {
            showAlert("Ошибка", "Введите фамилию");
            return false;
        }
        if (tfName.getText() == null || tfName.getText().trim().isEmpty()) {
            showAlert("Ошибка", "Введите имя");
            return false;
        }
        String phone = tfNumber.getText().trim();
        if (phone.isEmpty()) {
            showAlert("Ошибка", "Введите номер телефона");
            return false;
        }
        // Простая проверка телефона (можно расширить)
        if (!phone.matches("^(\\+[0-9]{11}|[0-9]{11})$")) {
            showAlert("Ошибка", "Номер телефона должен быть в формате: 11 цифр или +7XXXXXXXXXX");
            return false;
        }
        return true;
    }

    @FXML
    public void onSave(ActionEvent actionEvent) {
        if (!validateFields()) {
            return;
        }

        fillClientFromForm();

        if (client.getIdClient() == null) {
            clientsDAO.save(client);
            showAlert("Успех", "Клиент добавлен");
        } else {
            clientsDAO.update(client);
            showAlert("Успех", "Клиент обновлен");
        }
        stage.close();
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