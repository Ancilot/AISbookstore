package ru.an.bookstore;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class MainApplication extends Application {

    public static ResourceBundle getBundle() {
        return bundle;
    }


    private static Stage stage;
    private static ResourceBundle bundle;
    private static final Logger logger = Logger.getLogger(MainApplication.class.getName());

    public static Stage getStage() {
        return stage;
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Выберите одну из локалей (раскомментируйте нужную)
        // Locale locale = new Locale("ru", "RU");  // русский
        Locale locale = Locale.ENGLISH;      // английский
        // Locale locale = Locale.GERMAN;        // немецкий

        Locale.setDefault(locale);
        bundle = ResourceBundle.getBundle("ru.an.bookstore.messages", Locale.getDefault());
        logger.info("Загружены ресурсы для локали: " + Locale.getDefault());

        FXMLLoader fxmlLoader = new FXMLLoader(
                MainApplication.class.getResource("main.fxml"),
                bundle
        );

        MainApplication.stage = stage;
        Scene scene = new Scene(fxmlLoader.load(), 620, 340);
        stage.setTitle(bundle.getString("app.title"));
        stage.setScene(scene);
        stage.show();


    }

    @Override
    public void stop() throws Exception {
        DBHelper.close();  // закрываем подключение к БД при выходе из приложения
        super.stop();
    }
}

