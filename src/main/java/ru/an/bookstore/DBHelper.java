package ru.an.bookstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBHelper {

    private static final Logger logger = LoggerFactory.getLogger(DBHelper.class);

    private static String dbUrlBase;
    private static String dbName;
    private static Connection connection;

    static {
        try {
            URL url = DBHelper.class.getResource("/ru/an/bookstore/config.properties");
            if (url == null) {
                throw new RuntimeException("config.properties не найден");
            }

            Properties prop = new Properties();
            try (FileInputStream fis = new FileInputStream(url.getFile())) {
                prop.load(fis);
            }

            dbUrlBase = prop.getProperty("db.url");
            dbName = prop.getProperty("db.name");

            logger.debug("Загружены настройки подключения: url={}, name={}", dbUrlBase, dbName);
        } catch (IOException ex) {
            logger.error("Ошибка загрузки config.properties", ex);
            throw new RuntimeException("Ошибка загрузки конфигурации", ex);
        }
    }

    public static void initConnection(String user, String password) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            closeConnection();
        }

        String fullUrl = dbUrlBase + dbName;
        logger.info("Попытка подключения к БД пользователем: {}", user);

        try {
            connection = DriverManager.getConnection(fullUrl, user, password);
            logger.info("Соединение с БД успешно установлено для пользователя: {}", user);
        } catch (SQLException e) {
            logger.error("Ошибка подключения для пользователя {}: {}", user, e.getMessage());
            throw e;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            logger.error("Попытка получить соединение, но оно не инициализировано");
            throw new SQLException("Соединение не инициализировано. Вызовите initConnection()");
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Соединение с БД закрыто");
            } catch (SQLException ex) {
                logger.error("Ошибка при закрытии соединения", ex);
            }
        }
    }
}