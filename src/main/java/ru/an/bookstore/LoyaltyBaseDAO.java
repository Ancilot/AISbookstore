package ru.an.bookstore;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class LoyaltyBaseDAO {

    private static Properties property = new Properties();

    public LoyaltyBaseDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private final static String FIND_BY_CLIENT =
//            "SELECT * FROM store.loyalty_base WHERE client = ?";
//
//    private final static String INSERT =
//            "INSERT INTO store.loyalty_base (card_number, client, date_card, ransom_amount, discount) " +
//                    "VALUES (?, ?, DEFAULT, 0, '0%') RETURNING card_number, date_card, ransom_amount, discount";
//
//    private final static String UPDATE =
//            "UPDATE store.loyalty_base SET card_number = ? WHERE client = ?";
//
//    private final static String DELETE_BY_CLIENT =
//            "DELETE FROM store.loyalty_base WHERE client = ?";

    public LoyaltyBase findByClient(Long clientId) {
        LoyaltyBase loyalty = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("loyalty_base.find_by_client"));
            ps.setLong(1, clientId);
            rs = ps.executeQuery();
            if (rs.next()) {
                loyalty = mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return loyalty;
    }

    public LoyaltyBase save(LoyaltyBase entity) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("loyalty_base.insert"), Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getCardNumber());
            ps.setLong(2, entity.getClient().getIdClient());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                entity.setCardNumber(rs.getString("card_number"));
                entity.setDateCard(rs.getDate("date_card").toLocalDate());
                entity.setRansomAmount(rs.getBigDecimal("ransom_amount"));
                entity.setDiscount(rs.getString("discount"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return entity;
    }

    public LoyaltyBase update(LoyaltyBase entity) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("loyalty_base.update"));
            ps.setString(1, entity.getCardNumber());
            ps.setLong(2, entity.getClient().getIdClient());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
        return entity;
    }

    public void deleteByClient(Long clientId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("loyalty_base.delete_by_client"));
            ps.setLong(1, clientId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
    }

    private LoyaltyBase mapRow(ResultSet rs) throws SQLException {
        LoyaltyBase loyalty = new LoyaltyBase();
        loyalty.setCardNumber(rs.getString("card_number"));

        Clients client = new Clients();
        client.setIdClient(rs.getLong("client"));
        loyalty.setClient(client);

        loyalty.setDateCard(rs.getDate("date_card").toLocalDate());
        loyalty.setRansomAmount(rs.getBigDecimal("ransom_amount"));
        loyalty.setDiscount(rs.getString("discount"));
        return loyalty;
    }

    private void closeResources(ResultSet rs, Statement st, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (st != null) st.close(); } catch (SQLException e) {}
        try { if (conn != null) DBHelper.close(conn); } catch (Exception e) {}
    }
}