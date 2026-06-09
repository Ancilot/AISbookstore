package ru.an.bookstore;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

public class NewClientController {

    private static final Logger log = LoggerFactory.getLogger(NewClientController.class);

    @FXML private ResourceBundle resources;
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
            log.debug("Загрузка клиента для редактирования: id={}, name={} {}",
                    client.getIdClient(), client.getSurname(), client.getNameClient());
            fillFormFromClient();
        } else {
            log.debug("Создание нового клиента");
        }
    }

    public void setClientsDAO(ClientsDAO clientsDAO) {
        this.clientsDAO = clientsDAO;
    }

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
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
        String surname = tfSurname.getText();
        if (surname == null || surname.trim().isEmpty()) {
            log.warn("Ошибка валидации: пустая фамилия");
            showAlert(resources.getString("client.alert.error.empty_surname"));
            return false;
        }
        surname = surname.trim();

        if (!surname.matches(NAME_REGEX)) {
            log.warn("Ошибка валидации: неверный формат фамилии '{}'", surname);
            showAlert(resources.getString("client.alert.error.invalid_surname"));
            return false;
        }

        String name = tfName.getText();
        if (name == null || name.trim().isEmpty()) {
            log.warn("Ошибка валидации: пустое имя");
            showAlert(resources.getString("client.alert.error.empty_firstname"));
            return false;
        }
        name = name.trim();

        if (!name.matches(NAME_REGEX)) {
            log.warn("Ошибка валидации: неверный формат имени '{}'", name);
            showAlert(resources.getString("client.alert.error.invalid_firstname"));
            return false;
        }

        String patronymic = tfPatronomic.getText();
        if (patronymic != null && !patronymic.trim().isEmpty()) {
            patronymic = patronymic.trim();
            if (!patronymic.matches(NAME_REGEX)) {
                log.warn("Ошибка валидации: неверный формат отчества '{}'", patronymic);
                showAlert(resources.getString("client.alert.error.invalid_patronymic"));
                return false;
            }
        }

        String phone = tfNumber.getText();
        if (phone == null || phone.trim().isEmpty()) {
            log.warn("Ошибка валидации: пустой телефон");
            showAlert(resources.getString("client.alert.error.empty_phone"));
            return false;
        }
        phone = phone.trim();

        if (!phone.matches("^(\\+[0-9]{11}|[0-9]{11})$")) {
            log.warn("Ошибка валидации: неверный формат телефона '{}'", phone);
            showAlert(resources.getString("client.alert.error.invalid_phone"));
            return false;
        }

        String email = tfEmail.getText();
        if (email != null && !email.trim().isEmpty()) {
            email = email.trim();
            if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                log.warn("Ошибка валидации: неверный формат email '{}'", email);
                showAlert(resources.getString("client.alert.error.invalid_email"));
                return false;
            }
        }

        return true;
    }

    @FXML
    public void onSave(ActionEvent actionEvent) {
        log.debug("Сохранение клиента");

        if (!validateFields()) {
            return;
        }

        fillClientFromForm();

        if (client.getIdClient() == null) {
            log.info("Создание нового клиента: surname='{}', name='{}', phone='{}'",
                    client.getSurname(), client.getNameClient(), client.getNumberClient());
            clientsDAO.save(client);
            log.info("Клиент успешно создан с id={}", client.getIdClient());
            showAlert(resources.getString("client.alert.success.add"));
        } else {
            log.info("Обновление клиента id={}: surname='{}', name='{}', phone='{}'",
                    client.getIdClient(), client.getSurname(), client.getNameClient(), client.getNumberClient());
            clientsDAO.update(client);
            log.info("Клиент id={} успешно обновлён", client.getIdClient());
            showAlert(resources.getString("client.alert.success.update"));
        }
        stage.close();
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
        log.debug("Закрытие окна редактирования клиента");
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