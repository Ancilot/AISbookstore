package ru.an.bookstore;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class WarehouseDAO {
    private static Properties property = new Properties();

    public WarehouseDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    // Получить все книги на складе (не в архиве)
//    private final static String FIND_ALL_ACTIVE =
//            "SELECT w.id_warehouse, w.book, w.quantity, w.status, " +
//                    "       b.name_book, b.isbn, b.year_publication, " +
//                    "       p.name_publishing " +
//                    "FROM store.warehouse w " +
//                    "JOIN store.book_catalog b ON b.id_book = w.book " +
//                    "LEFT JOIN store.publishing_houses p ON p.id_pub = b.publishing_houses " +
//                    "WHERE w.archiv IS NOT TRUE " +
//                    "ORDER BY b.name_book";
//
//    // Для поиска книг на складе
//    private final static String SEARCH_WAREHOUSE = "SELECT * FROM store.search_warehouse(?)";
//
//    // Получить все книги в архиве
//    private final static String FIND_ALL_ARCHIVE =
//            "SELECT w.id_warehouse, w.book, w.quantity, w.status, " +
//                    "       b.name_book, b.isbn, b.year_publication, " +
//                    "       p.name_publishing " +
//                    "FROM store.warehouse w " +
//                    "JOIN store.book_catalog b ON b.id_book = w.book " +
//                    "LEFT JOIN store.publishing_houses p ON p.id_pub = b.publishing_houses " +
//                    "WHERE w.archiv = TRUE " +
//                    "ORDER BY b.name_book";
//
//    // Книги, которых нет на складе (для добавления)
//    private final static String FIND_BOOKS_NOT_IN_WAREHOUSE =
//            "SELECT b.id_book, b.name_book, b.isbn, b.year_publication, " +
//                    "       p.name_publishing, " +
//                    "       COALESCE(string_agg(DISTINCT a.surname || ' ' || a.name_author, ', '), '') AS authors " +
//                    "FROM store.book_catalog b " +
//                    "LEFT JOIN store.publishing_houses p ON p.id_pub = b.publishing_houses " +
//                    "LEFT JOIN store.authors_book ab ON ab.book = b.id_book " +
//                    "LEFT JOIN store.authors a ON a.id_authors = ab.author " +
//                    "WHERE NOT EXISTS (SELECT 1 FROM store.warehouse w WHERE w.book = b.id_book) " +
//                    "GROUP BY b.id_book, b.name_book, b.isbn, b.year_publication, p.name_publishing " +
//                    "ORDER BY b.name_book";
//
//    // Добавить книгу на склад
//    private final static String INSERT_WAREHOUSE =
//            "INSERT INTO store.warehouse (book, quantity) VALUES (?, ?) RETURNING id_warehouse";
//
//    // Удалить книгу со склада (архивация)
//    private final static String DELETE_BOOK = "{ ? = call store.delete_book(?) }";
//
//    // Поиск на складе
//    private final static String SEARCH_ACTIVE =
//            "SELECT w.id_warehouse, w.book, w.quantity, w.status, " +
//                    "       b.name_book, b.isbn, b.year_publication, " +
//                    "       p.name_publishing " +
//                    "FROM store.warehouse w " +
//                    "JOIN store.book_catalog b ON b.id_book = w.book " +
//                    "LEFT JOIN store.publishing_houses p ON p.id_pub = b.publishing_houses " +
//                    "WHERE w.archiv IS NOT TRUE " +
//                    "  AND b.name_book ILIKE ? " +
//                    "ORDER BY b.name_book";
//
//    // Поиск в архиве
//    private final static String SEARCH_ARCHIVE =
//            "SELECT w.id_warehouse, w.book, w.quantity, w.status, " +
//                    "       b.name_book, b.isbn, b.year_publication, " +
//                    "       p.name_publishing " +
//                    "FROM store.warehouse w " +
//                    "JOIN store.book_catalog b ON b.id_book = w.book " +
//                    "LEFT JOIN store.publishing_houses p ON p.id_pub = b.publishing_houses " +
//                    "WHERE w.archiv = TRUE " +
//                    "  AND b.name_book ILIKE ? " +
//                    "ORDER BY b.name_book";

    public List<Warehouse> findAllActive() {
        List<Warehouse> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("warehouse.find_all_active"));
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

    public List<Warehouse> findAllArchive() {
        List<Warehouse> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("warehouse.find_all_archive"));
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

    public List<BookCatalog> findBooksNotInWarehouse() {
        List<BookCatalog> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("warehouse.find_books_not_in_warehouse"));
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
            closeResources(rs, ps);
        }
        return list;
    }

    public Warehouse save(Warehouse warehouse) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("warehouse.insert"), Statement.RETURN_GENERATED_KEYS);
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
            closeResources(rs, ps);
        }
        return warehouse;
    }

    public int deleteBookToArchive(int bookId) {
        Connection conn = null;
        CallableStatement cs = null;
        int result = 0;
        try {
            conn = DBHelper.getConnection();
            cs = conn.prepareCall(property.getProperty("warehouse.delete_book"));
            cs.registerOutParameter(1, Types.INTEGER);
            cs.setInt(2, bookId);
            cs.execute();
            result = cs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            result = 0;
        } finally {
            closeResources(null, cs);
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
            ps = conn.prepareStatement(property.getProperty("warehouse.search_active"));
            ps.setString(1, "%" + searchText + "%");
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

    public List<Warehouse> searchArchive(String searchText) {
        List<Warehouse> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("warehouse.search_archive"));
            ps.setString(1, "%" + searchText + "%");
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
    // WarehouseDAO.java
    public List<BookCatalog> findBooksForSale(String searchText) {
        List<BookCatalog> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("warehouse.search_for_sale"));
            if (searchText == null || searchText.trim().isEmpty()) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, searchText);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                BookCatalog book = new BookCatalog();
                book.setIdBook(rs.getLong("book"));  // теперь поле "book" существует
                book.setNameBook(rs.getString("book_name"));
                book.setAuthors(rs.getString("authors"));
                book.setGenres(rs.getString("genres"));
                book.setQuantity(rs.getInt("quantity"));
                book.setStatus(rs.getString("status"));
                book.setPrice(rs.getBigDecimal("sale_price"));
                list.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }


    private void closeResources(ResultSet rs, PreparedStatement ps) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (ps != null) ps.close(); } catch (SQLException e) {}
    }
}