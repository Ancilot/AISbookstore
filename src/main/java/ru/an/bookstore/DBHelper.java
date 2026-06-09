package ru.an.bookstore;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBHelper {

    private static String dbUrl;
    private static String login;
    private static String pass;
    private static Connection connection;  // ← одно подключение

    public static Connection getConnection() {
        if (connection == null) {
            try {
                URL url = DBHelper.class.getResource("/ru/an/bookstore/config.properties");
                if (url == null) {
                    throw new RuntimeException("config.properties не найден");
                }

                Properties property = new Properties();
                try (FileInputStream fis = new FileInputStream(url.getFile())) {
                    property.load(fis);
                }

                dbUrl = property.getProperty("db.url");
                login = property.getProperty("db.login");
                pass = property.getProperty("db.pass");

                connection = DriverManager.getConnection(dbUrl, login, pass);

            } catch (SQLException | IOException ex) {
                throw new RuntimeException("Ошибка подключения к БД", ex);
            }
        }
        return connection;
    }

    public static void close() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка при закрытии соединения", e);
            }
        }
    }
}