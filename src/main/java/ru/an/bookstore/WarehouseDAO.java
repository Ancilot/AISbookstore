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

public class WarehouseDAO {

    private static final Logger logger =
            LoggerFactory.getLogger(WarehouseDAO.class);

    private static Properties property = new Properties();

    public WarehouseDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
            logger.debug("SQL-запросы для WarehouseDAO загружены");
        } catch (IOException e) {
            logger.error("Ошибка загрузки statements.properties", e);
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
        logger.debug("Получение списка книг на складе");
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
            logger.debug("Получено {} записей склада", list.size());
        } catch (SQLException e) {
            logger.error("Ошибка получения списка книг на складе", e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public List<Warehouse> findAllArchive() {
        logger.debug("Получение архивных записей склада");
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
            logger.debug("Получено {} архивных записей склада", list.size());
        } catch (SQLException e) {
            logger.error("Ошибка получения архивных записей склада", e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public List<BookCatalog> findBooksNotInWarehouse() {
        logger.debug("Получение книг, отсутствующих на складе");
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
            logger.debug("Найдено {} книг, отсутствующих на складе", list.size());
        } catch (SQLException e) {
            logger.error("Ошибка получения списка книг вне склада", e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public Warehouse save(Warehouse warehouse) {
        logger.debug(
                "Добавление книги id={} на склад, количество={}",
                warehouse.getBook().getIdBook(),
                warehouse.getQuantity()
        );
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
                logger.info(
                        "Создана запись склада id={} для книги id={}",
                        warehouse.getIdWarehouse(),
                        warehouse.getBook().getIdBook()
                );
            }
        } catch (SQLException e) {
            logger.error(
                    "Ошибка добавления книги id={} на склад",
                    warehouse.getBook().getIdBook(),
                    e
            );
        } finally {
            closeResources(rs, ps);
        }
        return warehouse;
    }

    public int deleteBookToArchive(int bookId) {
        logger.debug("Архивация книги id={}", bookId);
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
            logger.info(
                    "Архивация книги id={} завершена, результат={}",
                    bookId,
                    result
            );
        } catch (SQLException e) {
            logger.error(
                    "Ошибка архивации книги id={}",
                    bookId,
                    e
            );
            result = 0;
        } finally {
            closeResources(null, cs);
        }
        return result;
    }

    public List<Warehouse> searchActive(String searchText) {
        logger.debug(
                "Поиск книг на складе по запросу '{}'",
                searchText
        );
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
            logger.debug(
                    "По запросу '{}' найдено {} книг на складе",
                    searchText,
                    list.size()
            );
        } catch (SQLException e) {
            logger.error(
                    "Ошибка поиска книг на складе по запросу '{}'",
                    searchText,
                    e
            );
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public List<Warehouse> searchArchive(String searchText) {
        logger.debug(
                "Поиск архивных книг по запросу '{}'",
                searchText
        );
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
            logger.debug(
                    "По запросу '{}' найдено {} архивных книг",
                    searchText,
                    list.size()
            );
        } catch (SQLException e) {
            logger.error(
                    "Ошибка поиска архивных книг по запросу '{}'",
                    searchText,
                    e
            );
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
        logger.debug(
                "Поиск книг для продажи, запрос='{}'",
                searchText
        );
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
            logger.debug(
                    "Для продажи найдено {} книг",
                    list.size()
            );
        } catch (SQLException e) {
            logger.error(
                    "Ошибка поиска книг для продажи, запрос='{}'",
                    searchText,
                    e
            );
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }


    private void closeResources(ResultSet rs, PreparedStatement ps) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {logger.error("Ошибка закрытия ResultSet", e);}
        try { if (ps != null) ps.close(); } catch (SQLException e) {logger.error("Ошибка закрытия PreparedStatement", e);
        }
    }
}