package ru.an.bookstore;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewClientController {

    @FXML private TextField tfSurname;
    @FXML private TextField tfName;
    @FXML private TextField tfPatronomic;
    @FXML private TextField tfNumber;
    @FXML private TextField tfEmail;

    private static final String NAME_REGEX = "^[\\p{L} -]+$";

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

        String patronymic = tfPatronomic.getText();
        client.setPatrontmic(
                patronymic == null || patronymic.trim().isEmpty()
                        ? null
                        : patronymic.trim()
        );

        client.setNumberClient(tfNumber.getText().trim());

        String email = tfEmail.getText();
        client.setEmail(
                email == null || email.trim().isEmpty()
                        ? null
                        : email.trim()
        );
    }

    private boolean validateFields() {

        // Фамилия
        String surname = tfSurname.getText();
        if (surname == null || surname.trim().isEmpty()) {
            showAlert("Ошибка", "Введите фамилию");
            return false;
        }
        surname = surname.trim();

        if (!surname.matches(NAME_REGEX)) {
            showAlert("Ошибка", "Фамилия содержит недопустимые символы");
            return false;
        }

        // Имя
        String name = tfName.getText();
        if (name == null || name.trim().isEmpty()) {
            showAlert("Ошибка", "Введите имя");
            return false;
        }
        name = name.trim();

        if (!name.matches(NAME_REGEX)) {
            showAlert("Ошибка", "Имя содержит недопустимые символы");
            return false;
        }

        // Отчество (необязательное)
        String patronymic = tfPatronomic.getText();
        if (patronymic != null && !patronymic.trim().isEmpty()) {
            patronymic = patronymic.trim();

            if (!patronymic.matches(NAME_REGEX)) {
                showAlert("Ошибка", "Отчество содержит недопустимые символы");
                return false;
            }
        }

        // Телефон
        String phone = tfNumber.getText();
        if (phone == null || phone.trim().isEmpty()) {
            showAlert("Ошибка", "Введите номер телефона");
            return false;
        }
        phone = phone.trim();

        if (!phone.matches("^(\\+[0-9]{11}|[0-9]{11})$")) {
            showAlert("Ошибка", "Номер телефона должен содержать 11 цифр и может начинаться с '+'");
            return false;
        }

        // Email (необязательный)
        String email = tfEmail.getText();
        if (email != null && !email.trim().isEmpty()) {
            email = email.trim();

            if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                showAlert("Ошибка", "Некорректный email");
                return false;
            }
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