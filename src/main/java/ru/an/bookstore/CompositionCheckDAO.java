package ru.an.bookstore;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CompositionCheckDAO {

    private static Properties property = new Properties();

    public CompositionCheckDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Функции для работы со складом
//    private final static String RESERVE_BOOK_STOCK = "{ call store.reserve_book_stock(?, ?) }";
//    private final static String RETURN_BOOK_STOCK = "{ call store.return_book_stock(?, ?) }";
//    private final static String UPDATE_RESERVED_STOCK = "{ call store.update_reserved_stock(?, ?, ?) }";
//    private final static String RETURN_FROM_CHECK = "{ call store.return_from_check(?, ?) }";
//
//    private final static String INSERT =
//            "INSERT INTO store.composition_check (checks, book, quantity) VALUES (?, ?, ?) RETURNING id_composition";
//
//    private final static String DELETE_BY_ID =
//            "DELETE FROM store.composition_check WHERE id_composition = ?";
//
//    private final static String DELETE_BY_CHECK_AND_BOOK =
//            "DELETE FROM store.composition_check WHERE checks = ? AND book = ?";
//
//    private final static String DELETE_BY_CHECK =
//            "DELETE FROM store.composition_check WHERE checks = ?";
//
//    private final static String FIND_BY_CHECK =
//            "SELECT cc.*, b.name_book, b.isbn, c.date_time, c.status, " +
//                    "COALESCE((SELECT string_agg(DISTINCT a.surname || ' ' || a.name_author, ', ') " +
//                    "FROM store.authors_book ab JOIN store.authors a ON a.id_authors = ab.author " +
//                    "WHERE ab.book = b.id_book), '') AS authors, " +
//                    "COALESCE((SELECT string_agg(DISTINCT g.genr, ', ') " +
//                    "FROM store.genres_book gb JOIN store.genres g ON g.id_genr = gb.genr " +
//                    "WHERE gb.book = b.id_book), '') AS genres " +
//                    "FROM store.composition_check cc " +
//                    "JOIN store.book_catalog b ON b.id_book = cc.book " +
//                    "JOIN store.checks c ON c.id_check = cc.checks " +
//                    "WHERE cc.checks = ?";
//
//    private final static String SEARCH_CHECK_DETAILS =
//            "SELECT * FROM store.search_check_details(?, ?)";
//
//    private final static String FIND_BY_CLIENT =
//            "SELECT cc.*, b.name_book, b.isbn, c.date_time, c.status, " +
//                    "COALESCE((SELECT string_agg(DISTINCT a.surname || ' ' || a.name_author, ', ') " +
//                    "FROM store.authors_book ab JOIN store.authors a ON a.id_authors = ab.author " +
//                    "WHERE ab.book = b.id_book), '') AS authors, " +
//                    "COALESCE((SELECT string_agg(DISTINCT g.genr, ', ') " +
//                    "FROM store.genres_book gb JOIN store.genres g ON g.id_genr = gb.genr " +
//                    "WHERE gb.book = b.id_book), '') AS genres " +
//                    "FROM store.composition_check cc " +
//                    "JOIN store.book_catalog b ON b.id_book = cc.book " +
//                    "JOIN store.checks c ON c.id_check = cc.checks " +
//                    "WHERE c.client = ? " +
//                    "ORDER BY c.date_time DESC";

    public void reserveBookStock(Long bookId, int quantity) {
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = DBHelper.getConnection();
            cs = conn.prepareCall(property.getProperty("sp.reserve_book_stock"));
            cs.setInt(1, bookId.intValue());
            cs.setInt(2, quantity);
            cs.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при резервировании книги: " + e.getMessage(), e);
        } finally {
            closeResources(null, cs, conn);
        }
    }

    public void returnBookStock(Long bookId, int quantity) {
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = DBHelper.getConnection();
            cs = conn.prepareCall(property.getProperty("sp.return_book_stock"));
            cs.setInt(1, bookId.intValue());
            cs.setInt(2, quantity);
            cs.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при возврате книги на склад: " + e.getMessage(), e);
        } finally {
            closeResources(null, cs, conn);
        }
    }

    public void updateReservedStock(Long bookId, int oldQuantity, int newQuantity) {
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = DBHelper.getConnection();
            cs = conn.prepareCall(property.getProperty("sp.update_reserved_stock"));
            cs.setInt(1, bookId.intValue());
            cs.setInt(2, oldQuantity);
            cs.setInt(3, newQuantity);
            cs.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при обновлении резерва: " + e.getMessage(), e);
        } finally {
            closeResources(null, cs, conn);
        }
    }

    public CompositionCheck save(CompositionCheck composition) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("composition_check.insert"), Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, composition.getChecks().getIdCheck());
            ps.setLong(2, composition.getBook().getIdBook());
            ps.setInt(3, composition.getQuantity());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                composition.setIdComposition(rs.getLong(1));
            }

            // Резервируем книги на складе
            reserveBookStock(composition.getBook().getIdBook(), composition.getQuantity());

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return composition;
    }

    public void deleteById(Long id) {
        // Сначала получаем информацию о книге и количестве
        CompositionCheck cc = findById(id);
        if (cc != null) {
            // Возвращаем книги на склад
            returnBookStock(cc.getBook().getIdBook(), cc.getQuantity());

            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = DBHelper.getConnection();
                ps = conn.prepareStatement(property.getProperty("composition_check.delete_by_id"));
                ps.setLong(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                closeResources(null, ps, conn);
            }
        }
    }

    public void deleteByCheckAndBook(Long checkId, Long bookId) {
        // Сначала получаем информацию о количестве
        CompositionCheck cc = findByCheckAndBook(checkId, bookId);
        if (cc != null) {
            // Возвращаем книги на склад
            returnBookStock(bookId, cc.getQuantity());

            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = DBHelper.getConnection();
                ps = conn.prepareStatement(property.getProperty("composition_check.delete_by_check_book"));
                ps.setLong(1, checkId);
                ps.setLong(2, bookId);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                closeResources(null, ps, conn);
            }
        }
    }

    public void deleteByCheck(Long checkId) {
        // Получаем все позиции чека
        List<CompositionCheck> items = findByCheck(checkId);
        // Возвращаем все книги на склад
        for (CompositionCheck item : items) {
            returnBookStock(item.getBook().getIdBook(), item.getQuantity());
        }

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("composition_check.delete_by_check"));
            ps.setLong(1, checkId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public void updateQuantity(Long compositionId, int newQuantity) {
        // Получаем старую информацию
        CompositionCheck cc = findById(compositionId);
        if (cc != null && !cc.getQuantity().equals(newQuantity)) {
            int oldQuantity = cc.getQuantity();

            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = DBHelper.getConnection();
                ps = conn.prepareStatement(property.getProperty("composition_check.update_quantity"));
                ps.setInt(1, newQuantity);
                ps.setLong(2, compositionId);
                ps.executeUpdate();

                // Обновляем резерв на складе
                updateReservedStock(cc.getBook().getIdBook(), oldQuantity, newQuantity);

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                closeResources(null, ps, conn);
            }
        }
    }

    public List<CompositionCheck> findByClient(Long clientId) {
        List<CompositionCheck> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("composition_check.find_by_client"));
            ps.setLong(1, clientId);
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

    public List<CompositionCheck> findByCheck(Long checkId) {
        List<CompositionCheck> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("composition_check.find_by_check"));
            ps.setLong(1, checkId);
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

    public List<CompositionCheck> searchCheckDetails(Long checkId, String searchText) {
        List<CompositionCheck> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("composition_check.search_details"));
            ps.setInt(1, checkId.intValue());
            if (searchText == null || searchText.trim().isEmpty()) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, "%" + searchText + "%");
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                CompositionCheck cc = new CompositionCheck();
                BookCatalog book = new BookCatalog();
                book.setNameBook(rs.getString("book_name"));
                book.setAuthors(rs.getString("authors"));
                book.setGenres(rs.getString("genres"));
                cc.setBook(book);
                cc.setPriceTime(rs.getBigDecimal("price"));
                cc.setQuantity(rs.getInt("quantity"));
                list.add(cc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return list;
    }

    public CompositionCheck findById(Long id) {
        CompositionCheck cc = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("composition_check.find_by_id"));
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                cc = new CompositionCheck();
                cc.setIdComposition(rs.getLong("id_composition"));
                cc.setQuantity(rs.getInt("quantity"));
                cc.setPriceTime(rs.getBigDecimal("price_time"));

                Checks check = new Checks();
                check.setIdCheck(rs.getLong("checks"));
                cc.setChecks(check);

                BookCatalog book = new BookCatalog();
                book.setIdBook(rs.getLong("book"));
                cc.setBook(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return cc;
    }

    private CompositionCheck findByCheckAndBook(Long checkId, Long bookId) {
        CompositionCheck cc = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("composition_check.find_by_check_book"));
            ps.setLong(1, checkId);
            ps.setLong(2, bookId);
            rs = ps.executeQuery();
            if (rs.next()) {
                cc = new CompositionCheck();
                cc.setIdComposition(rs.getLong("id_composition"));
                cc.setQuantity(rs.getInt("quantity"));
                cc.setPriceTime(rs.getBigDecimal("price_time"));

                BookCatalog book = new BookCatalog();
                book.setIdBook(rs.getLong("book"));
                cc.setBook(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return cc;
    }

    public int getCountByCheck(Long checkId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("composition_check.count_by_check"));
            ps.setLong(1, checkId);
            rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return count;
    }

    public void returnFromCheck(Long compositionId, int returnQuantity) {
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = DBHelper.getConnection();
            cs = conn.prepareCall(property.getProperty("sp.return_from_check"));
            cs.setInt(1, compositionId.intValue());
            cs.setInt(2, returnQuantity);
            cs.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при возврате товара: " + e.getMessage(), e);
        } finally {
            closeResources(null, cs, conn);
        }
    }

    private CompositionCheck mapRow(ResultSet rs) throws SQLException {
        CompositionCheck cc = new CompositionCheck();
        cc.setIdComposition(rs.getLong("id_composition"));
        cc.setQuantity(rs.getInt("quantity"));
        cc.setPriceTime(rs.getBigDecimal("price_time"));

        Checks check = new Checks();
        check.setIdCheck(rs.getLong("checks"));
        check.setDateTime(rs.getTimestamp("date_time").toLocalDateTime());
        check.setStatus(rs.getString("status"));
        cc.setChecks(check);

        BookCatalog book = new BookCatalog();
        book.setIdBook(rs.getLong("book"));
        book.setNameBook(rs.getString("name_book"));
        book.setIsbn(rs.getString("isbn"));
        book.setAuthors(rs.getString("authors"));
        book.setGenres(rs.getString("genres"));
        cc.setBook(book);

        return cc;
    }

    private void closeResources(ResultSet rs, Statement st, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (st != null) st.close(); } catch (SQLException e) {}
        try { if (conn != null) DBHelper.close(conn); } catch (Exception e) {}
    }
}