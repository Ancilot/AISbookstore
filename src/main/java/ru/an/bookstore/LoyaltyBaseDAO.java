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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoyaltyBaseDAO {

    private static final Logger logger =
            LoggerFactory.getLogger(LoyaltyBaseDAO.class);


    private static Properties property = new Properties();

    public LoyaltyBaseDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
            logger.debug("SQL-запросы для LoyaltyBaseDAO загружены");
        } catch (IOException e) {
            logger.error("Ошибка загрузки statements.properties (LoyaltyBaseDAO)", e);
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
        logger.debug("Поиск loyalty-карты для clientId={}", clientId);
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
                logger.debug("Loyalty-карта найдена для clientId={}", clientId);
            }
        } catch (SQLException e) {
            logger.error("Ошибка поиска loyalty-карты clientId={}", clientId, e);
        } finally {
            closeResources(rs, ps);
        }
        return loyalty;
    }

    public LoyaltyBase save(LoyaltyBase entity) {
        logger.debug(
                "Создание loyalty-карты для clientId={}, cardNumber={}",
                entity.getClient().getIdClient(),
                entity.getCardNumber()
        );
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
                logger.info(
                        "Loyalty-карта создана: cardNumber={}, clientId={}",
                        entity.getCardNumber(),
                        entity.getClient().getIdClient()
                );
            }
        } catch (SQLException e) {
            logger.error(
                    "Ошибка создания loyalty-карты clientId={}",
                    entity.getClient().getIdClient(),
                    e
            );
        } finally {
            closeResources(rs, ps);
        }
        return entity;
    }

    public LoyaltyBase update(LoyaltyBase entity) {
        logger.debug("Обновление loyalty-карты cardNumber={}", entity.getCardNumber());
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("loyalty_base.update"));
            ps.setString(1, entity.getCardNumber());
            ps.setLong(2, entity.getClient().getIdClient());
            ps.executeUpdate();
            logger.info("Loyalty-карта обновлена cardNumber={}", entity.getCardNumber());
        } catch (SQLException e) {
            logger.error("Ошибка обновления loyalty-карты cardNumber={}", entity.getCardNumber(), e);
        } finally {
            closeResources(null, ps);
        }
        return entity;
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

    private void closeResources(ResultSet rs, Statement st) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {logger.error("Ошибка закрытия ресурсов LoyaltyBaseDAO", e);}
        try { if (st != null) st.close(); } catch (SQLException e) {logger.error("Ошибка закрытия ресурсов LoyaltyBaseDAO", e);}
    }
}