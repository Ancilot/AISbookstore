package ru.an.bookstore;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PriceDAO {

    private final static String INSERT = "INSERT INTO store.price (price, id_books) VALUES (?, ?) RETURNING id_price";
    private final static String FIND_CURRENT_BY_BOOK = "SELECT * FROM store.price WHERE id_books = ? ORDER BY date_time DESC LIMIT 1";
    private final static String FIND_HISTORY_BY_BOOK = "SELECT * FROM store.price WHERE id_books = ? ORDER BY date_time DESC";
    private final static String DELETE_BY_BOOK = "DELETE FROM store.price WHERE id_books = ?";

    public Price save(Long bookId, BigDecimal priceValue) {
        Price price = new Price();
        price.setPrice(priceValue);
        price.setIdBooks(bookId);
        price.setDateTime(LocalDateTime.now());
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, priceValue);
            ps.setLong(2, bookId);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                price.setIdPrice(rs.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return price;
    }

    public Price findCurrentByBook(Long bookId) {
        Price price = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(FIND_CURRENT_BY_BOOK);
            ps.setLong(1, bookId);
            rs = ps.executeQuery();
            if (rs.next()) {
                price = mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return price;
    }

    public List<Price> findHistoryByBook(Long bookId) {
        List<Price> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(FIND_HISTORY_BY_BOOK);
            ps.setLong(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return list;
    }

    public void deleteByBook(Long bookId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(DELETE_BY_BOOK);
            ps.setLong(1, bookId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
    }

    private void closeResources(ResultSet rs, PreparedStatement ps, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) DBHelper.close(conn);
        } catch (SQLException e) {
            e.printStackTrace();
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