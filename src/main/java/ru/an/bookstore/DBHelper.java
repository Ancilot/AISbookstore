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
    // TODO: После локализации переделать на single connection
    // Вместо создания подключения каждый раз, создать одно при старте
    public static Connection getConnection() {
        try {
            URL url = DBHelper.class.getResource("/ru/an/bookstore/config.properties");
            if (url == null) {
                throw new RuntimeException("config.properties не найден");
            }

            Properties property = new Properties();
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
            fis.close();

            dbUrl = property.getProperty("db.url");
            login = property.getProperty("db.login");
            pass = property.getProperty("db.pass");

            return DriverManager.getConnection(dbUrl, login, pass);

        } catch (SQLException ex) {
            throw new RuntimeException("Ошибка подключения к базе данных", ex);
        } catch (IOException ex) {
            throw new RuntimeException("Ошибка чтения config.properties", ex);
        }
    }

    public static void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
            }
        }
    }
}