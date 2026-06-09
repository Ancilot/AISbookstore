package ru.an.bookstore;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class ClientsTest extends ApplicationTest {

    private final ClientsDAO clientsDAO = new ClientsDAO();
    private final List<Clients> clientsToClean = new ArrayList<>();
    private ResourceBundle resources;

    private String testSurname;
    private String testName = "Тест";
    private String testPatronymic = "Тестович";
    private String testPhone;
    private String testEmail;

    private String updatedSurname;
    private String updatedPhone;

    private String generateLetterSuffix(long num) {
        String letters = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
        StringBuilder result = new StringBuilder();
        long n = Math.abs(num);
        while (n > 0) {
            result.insert(0, letters.charAt((int)(n % letters.length())));
            n = n / letters.length();
        }
        while (result.length() < 4) {
            result.append("А");
        }
        return result.toString();
    }

    @BeforeAll
    static void initDatabase() throws SQLException {
        DBHelper.initConnection("salesman123", "salesman123");
    }

    @BeforeEach
    void setUp() {
        long uniqueId = System.nanoTime();
        String letterSuffix = generateLetterSuffix(uniqueId);

        testSurname = "Тестовый" + letterSuffix;
        updatedSurname = "Обновлённый" + letterSuffix;

        testPhone = "9" + (uniqueId % 10000000000L);
        updatedPhone = "8" + ((uniqueId + 10000000000L) % 10000000000L);
        testEmail = "test" + uniqueId + "@example.com";

        System.out.println("Уникальные данные для теста");
        System.out.println("Фамилия: " + testSurname);
        System.out.println("Телефон: " + testPhone);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Locale.setDefault(new Locale("ru", "RU"));
        resources = ResourceBundle.getBundle("ru.an.bookstore.messages", Locale.getDefault());

        FXMLLoader loader = new FXMLLoader(
                MainApplication.class.getResource("clients.fxml"), resources);
        Scene scene = new Scene(loader.load(), 850, 400);
        stage.setScene(scene);
        stage.show();

        waitFor(15, TimeUnit.SECONDS, () -> lookup("#tvClients").tryQuery().isPresent());
    }

    @AfterEach
    void cleanUp() {
        for (Clients client : clientsToClean) {
            try {
                if (client.getIdClient() != null) {
                    clientsDAO.deleteById(client.getIdClient());
                    System.out.println("Удалён клиент id=" + client.getIdClient());
                }
            } catch (Exception e) {
                System.err.println("Ошибка очистки: " + e.getMessage());
            }
        }
        clientsToClean.clear();
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // Вспомогательный метод для закрытия Alert
    private void closeAlertIfPresent() {
        try {
            if (lookup(".dialog-pane").tryQuery().isPresent()) {
                clickOn("OK");
                Thread.sleep(500);
            }
        } catch (Exception e) {
            // Alert не найден - игнорируем
        }
    }

    @Test
    @DisplayName("TC-01: Успешное добавление нового клиента")
    void testAddClient() throws Exception {
        System.out.println("ТЕСТ: добавление клиента");

        clickOn("#btnAddClient");
        waitFor(10, TimeUnit.SECONDS, () -> lookup("#tfSurname").tryQuery().isPresent());

        clickOn("#tfSurname").write(testSurname);
        clickOn("#tfName").write(testName);
        clickOn("#tfPatronomic").write(testPatronymic);
        clickOn("#tfNumber").write(testPhone);
        clickOn("#tfEmail").write(testEmail);
        clickOn("#btnSaveClient");

        // Ждём Alert с подтверждением и закрываем его
        waitFor(10, TimeUnit.SECONDS, () -> lookup(".dialog-pane").tryQuery().isPresent());
        clickOn("OK");
        Thread.sleep(500);
        Thread.sleep(2000);

        // Ждём появления клиента в таблице
        waitFor(15, TimeUnit.SECONDS, () -> {
            TableView<Clients> tv = lookup("#tvClients").query();
            for (Clients c : tv.getItems()) {
                if (testSurname.equals(c.getSurname())) {
                    return true;
                }
            }
            return false;
        });

        TableView<Clients> tableView = lookup("#tvClients").query();
        boolean found = false;
        Clients savedClient = null;

        for (Clients c : tableView.getItems()) {
            if (testSurname.equals(c.getSurname()) &&
                    testName.equals(c.getNameClient()) &&
                    testPhone.equals(c.getNumberClient())) {
                found = true;
                savedClient = c;
                break;
            }
        }

        assertThat(found).isTrue();
        if (savedClient != null) {
            clientsToClean.add(savedClient);
            System.out.println("Клиент добавлен с id=" + savedClient.getIdClient());
        }
        System.out.println("ТЕСТ ЗАВЕРШЕН");
    }

    @Test
    @DisplayName("TC-02: Успешное редактирование существующего клиента")
    void testEditClient() throws Exception {
        System.out.println("ТЕСТ: Редактирование клиента");

        Clients testClient = new Clients();
        testClient.setSurname(testSurname);
        testClient.setNameClient(testName);
        testClient.setPatrontmic(testPatronymic);
        testClient.setNumberClient(testPhone);
        testClient.setEmail(testEmail);
        clientsDAO.save(testClient);
        clientsToClean.add(testClient);
        System.out.println("Создан клиент с ID: " + testClient.getIdClient());

        Thread.sleep(1000);
        clickOn("#btnFindClient");
        Thread.sleep(1000);

        waitFor(10, TimeUnit.SECONDS, () -> {
            TableView<Clients> tv = lookup("#tvClients").query();
            for (Clients c : tv.getItems()) {
                if (testSurname.equals(c.getSurname())) {
                    return true;
                }
            }
            return false;
        });

        TableView<Clients> tableView = lookup("#tvClients").query();
        for (Clients c : tableView.getItems()) {
            if (testSurname.equals(c.getSurname())) {
                clickOn(c.getSurname());
                break;
            }
        }

        clickOn("#btnEditClient");
        waitFor(10, TimeUnit.SECONDS, () -> lookup("#tfSurname").tryQuery().isPresent());

        clickOn("#tfSurname")
                .press(javafx.scene.input.KeyCode.CONTROL)
                .press(javafx.scene.input.KeyCode.A)
                .release(javafx.scene.input.KeyCode.A)
                .release(javafx.scene.input.KeyCode.CONTROL)
                .write(updatedSurname);

        clickOn("#tfNumber")
                .press(javafx.scene.input.KeyCode.CONTROL)
                .press(javafx.scene.input.KeyCode.A)
                .release(javafx.scene.input.KeyCode.A)
                .release(javafx.scene.input.KeyCode.CONTROL)
                .write(updatedPhone);

        clickOn("#btnSaveClient");

        // Ждём Alert с подтверждением и закрываем его
        waitFor(10, TimeUnit.SECONDS, () -> lookup(".dialog-pane").tryQuery().isPresent());
        clickOn("OK");
        Thread.sleep(500);
        Thread.sleep(2000);

        waitFor(15, TimeUnit.SECONDS, () -> {
            TableView<Clients> tv = lookup("#tvClients").query();
            for (Clients c : tv.getItems()) {
                if (updatedSurname.equals(c.getSurname())) {
                    return true;
                }
            }
            return false;
        });

        tableView = lookup("#tvClients").query();
        boolean foundUpdated = false;
        for (Clients c : tableView.getItems()) {
            if (updatedSurname.equals(c.getSurname()) &&
                    updatedPhone.equals(c.getNumberClient())) {
                foundUpdated = true;
                break;
            }
        }

        assertThat(foundUpdated).isTrue();
        System.out.println("ТЕСТ ЗАВЕРШЁН");
    }

    @Test
    @DisplayName("TC-03: Добавление клиента с пустым полем 'Фамилия'")
    void testAddClientEmptySurname() throws Exception {
        System.out.println("ТЕСТ: Пустая фамилия");

        clickOn("#btnAddClient");
        waitFor(10, TimeUnit.SECONDS, () -> lookup("#tfSurname").tryQuery().isPresent());

        clickOn("#tfName").write(testName);
        clickOn("#tfNumber").write(testPhone);
        clickOn("#btnSaveClient");

        Thread.sleep(1500);

        // Закрываем Alert с ошибкой, если он появился
        closeAlertIfPresent();

        boolean formStillOpen = lookup("#tfSurname").tryQuery().isPresent();
        assertThat(formStillOpen).isTrue();

        clickOn("#btnCancelClient");
        Thread.sleep(500);

        System.out.println("ТЕСТ ЗАВЕРШЕН");
    }

    @Test
    @DisplayName("TC-04: Добавление клиента с неверным форматом телефона")
    void testAddClientInvalidPhone() throws Exception {
        System.out.println("ТЕСТ: Неверный телефон");

        String invalidPhone = "abc123def456";

        clickOn("#btnAddClient");
        waitFor(10, TimeUnit.SECONDS, () -> lookup("#tfSurname").tryQuery().isPresent());

        clickOn("#tfSurname").write(testSurname);
        clickOn("#tfName").write(testName);
        clickOn("#tfNumber").write(invalidPhone);
        clickOn("#btnSaveClient");

        Thread.sleep(1500);

        // Закрываем Alert с ошибкой, если он появился
        closeAlertIfPresent();

        boolean formStillOpen = lookup("#tfSurname").tryQuery().isPresent();
        assertThat(formStillOpen).isTrue();

        clickOn("#btnCancelClient");
        Thread.sleep(500);

        System.out.println("ТЕСТ ЗАВЕРШЕН");
    }
}