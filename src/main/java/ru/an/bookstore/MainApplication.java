package ru.an.bookstore;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);
    private static Stage primaryStage;
    private static ResourceBundle bundle;

    public static Stage getStage() {
        return primaryStage;
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        logger.info("Приложение запущено");

        // Настройка локали
        // Locale locale = new Locale("ru", "RU");  // русский
        Locale locale = Locale.ENGLISH;      // английский
        // Locale locale = Locale.GERMAN;        // немецкий

        Locale.setDefault(locale);
        bundle = ResourceBundle.getBundle("ru.an.bookstore.messages", Locale.getDefault());
        logger.info("Загружены ресурсы для локали: " + Locale.getDefault());

        // Цикл аутентификации
        LoginDialog loginDialog = new LoginDialog();

        while (true) {
            Optional<LoginDialog.LoginResult> result = loginDialog.showAndWait();

            if (result.isEmpty()) {
                logger.info("Пользователь отменил вход, выход из приложения");
                Platform.exit();
                return;
            }

            String username = result.get().getUsername();
            String password = result.get().getPassword();

            try {
                DBHelper.initConnection(username, password);
                logger.info("Успешная аутентификация для пользователя: {}", username);
                break;
            } catch (SQLException ex) {
                logger.error("Ошибка аутентификации для пользователя {}: {}", username, ex.getMessage());

                String userMessage;
                    userMessage =
                            "Проверьте:\n" +
                            "- Логин\n" +
                            "- Пароль\n" +
                            "- Доступность сервера";

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка подключения");
                alert.setHeaderText("Не удалось подключиться к базе данных");
                alert.setContentText(userMessage);
                alert.showAndWait();
            }
        }

        // Загрузка главного окна
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    MainApplication.class.getResource("main.fxml"),
                    bundle
            );

            Scene scene = new Scene(fxmlLoader.load(), 620, 340);
            primaryStage.setTitle(bundle.getString("app.title"));
            primaryStage.setScene(scene);
            primaryStage.show();

            logger.debug("Главное окно приложения отображено");
        } catch (IOException e) {
            logger.error("Ошибка загрузки FXML", e);
            Platform.exit();
        }
    }

    @Override
    public void stop() throws Exception {
        logger.info("Приложение завершает работу");
        DBHelper.closeConnection();
        super.stop();
    }
}