package ru.an.bookstore;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ChecksDAO {
    private static Properties property = new Properties();

    public ChecksDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private final static String INSERT_CHECK =
//            "INSERT INTO store.checks (client, sum_check) VALUES (?, 0) RETURNING id_check";
//
//    private final static String FIND_BY_ID =
//            "SELECT * FROM store.checks WHERE id_check = ?";
//
//    private final static String COMPLETE_CHECK = "{ call store.complete_check(?) }";
//
//    private final static String DELETE_BY_ID =
//            "DELETE FROM store.checks WHERE id_check = ?";

    public Checks createCheck(Long clientId) {
        Checks check = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("checks.insert"), Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, clientId);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                Long checkId = rs.getLong(1);
                check = findById(checkId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps);
        }
        return check;
    }

    public Checks findById(Long id) {
        Checks check = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("checks.find_by_id"));
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                check = mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps);
        }
        return check;
    }

    // Заменяем payCheck на completeCheck
    public void completeCheck(Long checkId) {
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = DBHelper.getConnection();
            cs = conn.prepareCall(property.getProperty("checks.complete"));
            cs.setInt(1, checkId.intValue());
            cs.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, cs);
        }
    }

    public void deleteCheck(Long checkId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("checks.delete"));
            ps.setLong(1, checkId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps);
        }
    }

    private Checks mapRow(ResultSet rs) throws SQLException {
        Checks check = new Checks();
        check.setIdCheck(rs.getLong("id_check"));
        check.setDateTime(rs.getTimestamp("date_time").toLocalDateTime());
        check.setSumCheck(rs.getBigDecimal("sum_check"));
        check.setStatus(rs.getString("status"));

        Clients client = new Clients();
        client.setIdClient(rs.getLong("client"));
        check.setClient(client);

        return check;
    }

    private void closeResources(ResultSet rs, Statement st) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (st != null) st.close(); } catch (SQLException e) {}
    }
}