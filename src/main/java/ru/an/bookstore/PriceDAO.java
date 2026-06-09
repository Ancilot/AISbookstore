package ru.an.bookstore;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriceDAO {

    private static final Logger logger =
            LoggerFactory.getLogger(PriceDAO.class);

    private static Properties property = new Properties();

    public PriceDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
            logger.debug("SQL-запросы для PriceDAO загружены");
        } catch (IOException e) {
            logger.error("Ошибка загрузки statements.properties", e);
        }
    }

//    private final static String INSERT = "INSERT INTO store.price (price, id_books) VALUES (?, ?) RETURNING id_price";
//    private final static String FIND_CURRENT_BY_BOOK = "SELECT * FROM store.price WHERE id_books = ? ORDER BY date_time DESC LIMIT 1";
//    private final static String FIND_HISTORY_BY_BOOK = "SELECT * FROM store.price WHERE id_books = ? ORDER BY date_time DESC";
//    private final static String DELETE_BY_BOOK = "DELETE FROM store.price WHERE id_books = ?";

    public Price save(Long bookId, BigDecimal priceValue) {
        logger.debug(
                "Добавление цены для книги id={}, price={}",
                bookId,
                priceValue
        );
        Price price = new Price();
        price.setPrice(priceValue);
        price.setIdBooks(bookId);
        price.setDateTime(LocalDateTime.now());
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("price.insert"), Statement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, priceValue);
            ps.setLong(2, bookId);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                price.setIdPrice(rs.getLong(1));
                logger.info(
                        "Цена сохранена id={}, bookId={}, price={}",
                        price.getIdPrice(),
                        bookId,
                        priceValue
                );
            }
        } catch (SQLException e) {
            logger.error(
                    "Ошибка сохранения цены для книги id={}",
                    bookId,
                    e
            );
            throw new RuntimeException(e);
        } finally {
            closeResources(rs, ps);
        }
        return price;
    }

    public Price findCurrentByBook(Long bookId) {
        logger.debug(
                "Получение текущей цены книги id={}",
                bookId
        );
        Price price = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("price.find_current_by_book"));
            ps.setLong(1, bookId);
            rs = ps.executeQuery();
            if (rs.next()) {
                price = mapRow(rs);
                logger.debug(
                        "Текущая цена книги id={} найдена",
                        bookId
                );
            }
        } catch (SQLException e) {
            logger.error(
                    "Ошибка получения текущей цены книги id={}",
                    bookId,
                    e
            );
        } finally {
            closeResources(rs, ps);
        }
        return price;
    }

    public List<Price> findHistoryByBook(Long bookId) {
        logger.debug(
                "Получение истории цен книги id={}",
                bookId
        );
        List<Price> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("price.find_history_by_book"));
            ps.setLong(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            logger.debug(
                    "Найдено {} записей истории цен для книги id={}",
                    list.size(),
                    bookId
            );
        } catch (SQLException e) {
            logger.error(
                    "Ошибка получения истории цен книги id={}",
                    bookId,
                    e
            );
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public void deleteByBook(Long bookId) {
        logger.debug(
                "Удаление истории цен книги id={}",
                bookId
        );
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("price.delete_by_book"));
            ps.setLong(1, bookId);
            ps.executeUpdate();
            logger.info(
                    "История цен книги id={} удалена",
                    bookId
            );
        } catch (SQLException e) {
            logger.error(
                    "Ошибка удаления истории цен книги id={}",
                    bookId,
                    e
            );
        } finally {
            closeResources(null, ps);
        }
    }

    private void closeResources(ResultSet rs, PreparedStatement ps) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        } catch (SQLException e) {
            logger.error("Ошибка закрытия ресурсов в PriceDAO", e);
        }
    }

    private Price mapRow(ResultSet rs) throws SQLException {
        Price price = new Price();
        price.setIdPrice(rs.getLong("id_price"));
        price.setPrice(rs.getBigDecimal("price"));
        price.setDateTime(rs.getTimestamp("date_time").toLocalDateTime());
        price.setIdBooks(rs.getLong("id_books"));
        return price;
    }
}