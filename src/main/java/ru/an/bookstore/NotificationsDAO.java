package ru.an.bookstore;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class NotificationsDAO {

    private static Properties property = new Properties();

    public NotificationsDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private final static String FIND_ALL =
//            "SELECT n.*, c.surname, c.name_client " +
//                    "FROM store.notifications n " +
//                    "JOIN store.clients c ON c.id_client = n.client " +
//                    "ORDER BY n.date_notification DESC";
//
//    private final static String DELETE_BY_ID =
//            "DELETE FROM store.notifications WHERE id_notification = ?";

    public List<Notifications> findAll() {
        List<Notifications> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("notifications.find_all"));
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public void deleteById(Long id) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("notifications.delete_by_id"));
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps);
        }
    }

    private Notifications mapRow(ResultSet rs) throws SQLException {
        Notifications notification = new Notifications();
        notification.setIdNotification(rs.getLong("id_notification"));
        notification.setTextNotification(rs.getString("text_notification"));
        notification.setDateNotification(rs.getDate("date_notification").toLocalDate());
        notification.setStatus(rs.getString("status"));

        Clients client = new Clients();
        client.setIdClient(rs.getLong("client"));
        client.setSurname(rs.getString("surname"));
        client.setNameClient(rs.getString("name_client"));
        notification.setClient(client);

        return notification;
    }

    public List<Notifications> findByClient(Long clientId) {
        List<Notifications> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("notifications.find_by_client"));
            ps.setLong(1, clientId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }
    private void closeResources(ResultSet rs, Statement st) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (st != null) st.close(); } catch (SQLException e) {}
    }
}