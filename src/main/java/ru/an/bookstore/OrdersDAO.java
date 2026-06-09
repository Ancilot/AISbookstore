package ru.an.bookstore;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrdersDAO {

    private static final Logger logger =
            LoggerFactory.getLogger(OrdersDAO.class);

    private static Properties property = new Properties();

    public OrdersDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
            logger.debug("SQL-запросы для OrdersDAO загружены");
        } catch (IOException e) {
            logger.error("Ошибка загрузки statements.properties", e);
        }
    }

//    private final static String FIND_ALL_ACTIVE =
//            "SELECT o.*, c.surname, c.name_client, c.patrontmic, b.name_book " +
//                    "FROM store.orders o " +
//                    "LEFT JOIN store.clients c ON c.id_client = o.client " +
//                    "LEFT JOIN store.book_catalog b ON b.id_book = o.book " +
//                    "WHERE o.archiv IS NOT TRUE " +
//                    "ORDER BY o.date_order DESC";
//
//    private final static String SEARCH =
//            "SELECT o.*, c.surname, c.name_client, c.patrontmic, b.name_book " +
//                    "FROM store.orders o " +
//                    "LEFT JOIN store.clients c ON c.id_client = o.client " +
//                    "LEFT JOIN store.book_catalog b ON b.id_book = o.book " +
//                    "WHERE o.archiv IS NOT TRUE AND (" +
//                    "c.surname ILIKE ? OR c.name_client ILIKE ? OR " +
//                    "b.name_book ILIKE ? OR o.status ILIKE ?) " +
//                    "ORDER BY o.date_order DESC";
//
//    private final static String COMPLETE_ORDER = "{ call store.complete_order(?) }";
//    private final static String DELETE_ORDER = "{ ? = call store.delete_order(?) }";
//
//    private final static String SAVE = "INSERT INTO store.orders (client, book, quanity, text_order) VALUES (?, ?, ?, ?) RETURNING id_order";

    public List<Orders> findAllActive() {
        logger.debug("Получение активных заказов");
        List<Orders> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("orders.find_all_active"));
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            logger.debug("Найдено {} активных заказов", list.size());
        } catch (SQLException e) {
            logger.error("Ошибка получения активных заказов", e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public List<Orders> search(String searchText) {
        logger.debug("Поиск заказов по тексту: {}", searchText);
        List<Orders> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("orders.search"));
            String pattern = "%" + searchText + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            logger.debug("По запросу '{}' найдено {} заказов", searchText, list.size());
        } catch (SQLException e) {
            logger.error("Ошибка поиска заказов: {}", searchText, e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public void completeOrder(Long orderId) {
        logger.debug("Завершение заказа id={}", orderId);

        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = DBHelper.getConnection();
            cs = conn.prepareCall(property.getProperty("orders.complete"));
            cs.setInt(1, orderId.intValue());
            cs.execute();
            logger.info("Заказ id={} успешно завершён", orderId);
        } catch (SQLException e) {
            logger.error("Ошибка завершения заказа id={}", orderId, e);
            throw new RuntimeException("Ошибка при завершении заказа: " + e.getMessage(), e);
        } finally {
            closeResources(null, cs);
        }
    }

    public int deleteOrderToArchive(Long orderId) {
        logger.debug("Удаление заказа в архив id={}", orderId);
        Connection conn = null;
        CallableStatement cs = null;

        try {
            conn = DBHelper.getConnection();

            cs = conn.prepareCall(property.getProperty("orders.delete"));
            cs.registerOutParameter(1, Types.INTEGER);
            cs.setInt(2, orderId.intValue());

            cs.execute();
            logger.info("Заказ id={} перемещён в архив", orderId);
            return cs.getInt(1);

        } catch (SQLException e) {
            logger.error("Ошибка архивирования заказа id={}", orderId, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            closeResources(null, cs);
        }
    }

    private Orders mapRow(ResultSet rs) throws SQLException {
        Orders order = new Orders();
        order.setIdOrder(rs.getLong("id_order"));
        order.setDateOrder(rs.getDate("date_order").toLocalDate());
        order.setStatus(rs.getString("status"));
        order.setTextOrder(rs.getString("text_order"));

        // Устанавливаем книгу
        order.setBook(new BookCatalog());
        order.getBook().setIdBook(rs.getLong("book"));
        order.getBook().setNameBook(rs.getString("name_book"));
        order.setQuantity(rs.getInt("quanity"));

        // Устанавливаем клиента
        Clients client = new Clients();
        client.setIdClient(rs.getLong("client"));
        client.setSurname(rs.getString("surname"));
        client.setNameClient(rs.getString("name_client"));
        client.setPatrontmic(rs.getString("patrontmic"));
        order.setClient(client);

        return order;
    }

    public Orders save(Orders order) {
        logger.debug(
                "Создание заказа: clientId={}, bookId={}, qty={}",
                order.getClient().getIdClient(),
                order.getBook().getIdBook(),
                order.getQuantity()
        );
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("orders.insert"),
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, order.getClient().getIdClient());
            ps.setLong(2, order.getBook().getIdBook());
            ps.setInt(3, order.getQuantity());
            ps.setString(4, order.getTextOrder());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                order.setIdOrder(rs.getLong(1));
                logger.info("Заказ создан id={}", order.getIdOrder());
            }
        } catch (SQLException e) {
            logger.error(
                    "Ошибка создания заказа clientId={}, bookId={}",
                    order.getClient().getIdClient(),
                    order.getBook().getIdBook(),
                    e
            );
        } finally {
            closeResources(rs, ps);
        }
        return order;
    }

    private void closeResources(ResultSet rs, Statement st) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {logger.error("Ошибка закрытия ResultSet", e);}
        try { if (st != null) st.close(); } catch (SQLException e) {logger.error("Ошибка закрытия Statement", e);}
    }
}