package ru.an.bookstore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHelper {
    private static final String URL = "jdbc:postgresql://localhost:5432/Bookstore";
    private static final String LOGIN = "salesman";
    private static final String PASS = "1235";

    public static Connection getConnection() {
        try {
            // Всегда создаем НОВОЕ соединение
            return DriverManager.getConnection(URL, LOGIN, PASS);
        } catch (SQLException ex) {
            throw new RuntimeException("Ошибка подключения к базе данных", ex);
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