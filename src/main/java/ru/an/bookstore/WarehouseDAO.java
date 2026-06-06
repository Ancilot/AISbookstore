package ru.an.bookstore;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WarehouseDAO {

    // Получить все книги на складе (не в архиве)
    private final static String FIND_ALL_ACTIVE =
            "SELECT w.id_warehouse, w.book, w.quantity, w.status, " +
                    "       b.name_book, b.isbn, b.year_publication, " +
                    "       p.name_publishing " +
                    "FROM store.warehouse w " +
                    "JOIN store.book_catalog b ON b.id_book = w.book " +
                    "LEFT JOIN store.publishing_houses p ON p.id_pub = b.publishing_houses " +
                    "WHERE w.archiv IS NOT TRUE " +
                    "ORDER BY b.name_book";

    // Получить все книги в архиве
    private final static String FIND_ALL_ARCHIVE =
            "SELECT w.id_warehouse, w.book, w.quantity, w.status, " +
                    "       b.name_book, b.isbn, b.year_publication, " +
                    "       p.name_publishing " +
                    "FROM store.warehouse w " +
                    "JOIN store.book_catalog b ON b.id_book = w.book " +
                    "LEFT JOIN store.publishing_houses p ON p.id_pub = b.publishing_houses " +
                    "WHERE w.archiv = TRUE " +
                    "ORDER BY b.name_book";

    // Книги, которых нет на складе (для добавления)
    private final static String FIND_BOOKS_NOT_IN_WAREHOUSE =
            "SELECT b.id_book, b.name_book, b.isbn, b.year_publication, " +
                    "       p.name_publishing, " +
                    "       COALESCE(string_agg(DISTINCT a.surname || ' ' || a.name_author, ', '), '') AS authors " +
                    "FROM store.book_catalog b " +
                    "LEFT JOIN store.publishing_houses p ON p.id_pub = b.publishing_houses " +
                    "LEFT JOIN store.authors_book ab ON ab.book = b.id_book " +
                    "LEFT JOIN store.authors a ON a.id_authors = ab.author " +
                    "WHERE NOT EXISTS (SELECT 1 FROM store.warehouse w WHERE w.book = b.id_book) " +
                    "GROUP BY b.id_book, b.name_book, b.isbn, b.year_publication, p.name_publishing " +
                    "ORDER BY b.name_book";

    // Добавить книгу на склад
    private final static String INSERT_WAREHOUSE =
            "INSERT INTO store.warehouse (book, quantity) VALUES (?, ?) RETURNING id_warehouse";

    // Удалить книгу со склада (архивация)
    private final static String DELETE_BOOK = "{ ? = call store.delete_book(?) }";

    // Поиск на складе
    private final static String SEARCH_ACTIVE =
            "SELECT w.id_warehouse, w.book, w.quantity, w.status, " +
                    "       b.name_book, b.isbn, b.year_publication, " +
                    "       p.name_publishing " +
                    "FROM store.warehouse w " +
                    "JOIN store.book_catalog b ON b.id_book = w.book " +
                    "LEFT JOIN store.publishing_houses p ON p.id_pub = b.publishing_houses " +
                    "WHERE w.archiv IS NOT TRUE " +
                    "  AND b.name_book ILIKE ? " +
                    "ORDER BY b.name_book";

    // Поиск в архиве
    private final static String SEARCH_ARCHIVE =
            "SELECT w.id_warehouse, w.book, w.quantity, w.status, " +
                    "       b.name_book, b.isbn, b.year_publication, " +
                    "       p.name_publishing " +
                    "FROM store.warehouse w " +
                    "JOIN store.book_catalog b ON b.id_book = w.book " +
                    "LEFT JOIN store.publishing_houses p ON p.id_pub = b.publishing_houses " +
                    "WHERE w.archiv = TRUE " +
                    "  AND b.name_book ILIKE ? " +
                    "ORDER BY b.name_book";

    public List<Warehouse> findAllActive() {
        List<Warehouse> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(FIND_ALL_ACTIVE);
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

    public List<Warehouse> findAllArchive() {
        List<Warehouse> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(FIND_ALL_ARCHIVE);
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

    public List<BookCatalog> findBooksNotInWarehouse() {
        List<BookCatalog> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(FIND_BOOKS_NOT_IN_WAREHOUSE);
            rs = ps.executeQuery();
            while (rs.next()) {
                BookCatalog book = new BookCatalog();
                book.setIdBook(rs.getLong("id_book"));
                book.setNameBook(rs.getString("name_book"));
                book.setIsbn(rs.getString("isbn"));
                int year = rs.getInt("year_publication");
                if (!rs.wasNull() && year > 0) {
                    book.setYearPublication(java.time.LocalDate.of(year, 1, 1));
                }
                book.setPublishingName(rs.getString("name_publishing"));
                book.setAuthors(rs.getString("authors"));
                list.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return list;
    }

    public Warehouse save(Warehouse warehouse) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(INSERT_WAREHOUSE, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, warehouse.getBook().getIdBook());
            ps.setInt(2, warehouse.getQuantity());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                warehouse.setIdWarehouse(rs.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return warehouse;
    }

    public int deleteBookToArchive(int bookId) {
        Connection conn = null;
        CallableStatement cs = null;
        int result = 0;
        try {
            conn = DBHelper.getConnection();
            cs = conn.prepareCall("{ ? = call store.delete_book(?) }");
            cs.registerOutParameter(1, Types.INTEGER);
            cs.setInt(2, bookId);
            cs.execute();
            result = cs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            result = 0;
        } finally {
            closeResources(null, cs, conn);
        }
        return result;
    }

    public List<Warehouse> searchActive(String searchText) {
        List<Warehouse> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(SEARCH_ACTIVE);
            ps.setString(1, "%" + searchText + "%");
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

    public List<Warehouse> searchArchive(String searchText) {
        List<Warehouse> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(SEARCH_ARCHIVE);
            ps.setString(1, "%" + searchText + "%");
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

    private Warehouse mapRow(ResultSet rs) throws SQLException {
        Warehouse warehouse = new Warehouse();
        warehouse.setIdWarehouse(rs.getLong("id_warehouse"));
        warehouse.setQuantity(rs.getInt("quantity"));
        warehouse.setStatus(rs.getString("status"));

        BookCatalog book = new BookCatalog();
        book.setIdBook(rs.getLong("book"));
        book.setNameBook(rs.getString("name_book"));
        book.setIsbn(rs.getString("isbn"));
        int year = rs.getInt("year_publication");
        if (!rs.wasNull() && year > 0) {
            book.setYearPublication(java.time.LocalDate.of(year, 1, 1));
        }
        book.setPublishingName(rs.getString("name_publishing"));
        warehouse.setBook(book);

        return warehouse;
    }

    private void closeResources(ResultSet rs, PreparedStatement ps, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (ps != null) ps.close(); } catch (SQLException e) {}
        try { if (conn != null) DBHelper.close(conn); } catch (Exception e) {}
    }
}