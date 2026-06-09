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

public class GenresBookDAO {
    private static final Logger logger =
            LoggerFactory.getLogger(GenresBookDAO.class);
    private static Properties property = new Properties();

    public GenresBookDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
            logger.debug("SQL для GenresBookDAO загружены");
        } catch (IOException e) {
            logger.error("Ошибка загрузки statements.properties", e);
        }
    }

//    private final static String INSERT = "INSERT INTO store.genres_book (genr, book) VALUES (?, ?)";
//    private final static String DELETE_BY_BOOK = "DELETE FROM store.genres_book WHERE book = ?";
//    private final static String DELETE_BY_BOOK_AND_GENRE = "DELETE FROM store.genres_book WHERE book = ? AND genr = ?";
//    private final static String FIND_BY_BOOK = "SELECT g.* FROM store.genres_book gb " +
//            "JOIN store.genres g ON g.id_genr = gb.genr WHERE gb.book = ?";
//    private final static String FIND_GENRES_NOT_IN_BOOK = "SELECT * FROM store.genres g " +
//            "WHERE g.id_genr NOT IN (SELECT genr FROM store.genres_book WHERE book = ?) " +
//            "ORDER BY g.genr";

    public void save(Long bookId, Long genreId) {
        logger.debug("Добавление жанра к книге: bookId={}, genreId={}", bookId, genreId);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("genres_book.insert"));
            ps.setLong(1, genreId);
            ps.setLong(2, bookId);
            ps.executeUpdate();
            logger.info("Жанр добавлен к книге: bookId={}, genreId={}", bookId, genreId);
        } catch (SQLException e) {
            logger.error(
                    "Ошибка добавления жанра к книге: bookId={}, genreId={}",
                    bookId,
                    genreId,
                    e
            );
        } finally {
            closeResources(null, ps);
        }
    }

    public void deleteByBook(Long bookId) {
        logger.debug("Удаление всех жанров книги id={}", bookId);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("genres_book.delete_by_book"));
            ps.setLong(1, bookId);
            ps.executeUpdate();
            logger.info("Жанры удалены у книги id={}", bookId);
        } catch (SQLException e) {
            logger.error("Ошибка удаления жанров книги id={}", bookId, e);
        } finally {
            closeResources(null, ps);
        }
    }

    public void delete(Long bookId, Long genreId) {
        logger.debug("Удаление жанра из книги: bookId={}, genreId={}", bookId, genreId);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("genres_book.delete_by_book_genre"));
            ps.setLong(1, bookId);
            ps.setLong(2, genreId);
            ps.executeUpdate();
            logger.info("Жанр удалён: bookId={}, genreId={}", bookId, genreId);
        } catch (SQLException e) {
            logger.error(
                    "Ошибка удаления жанра: bookId={}, genreId={}",
                    bookId,
                    genreId,
                    e
            );
        } finally {
            closeResources(null, ps);
        }
    }

    public List<Genres> findByBook(Long bookId) {
        logger.debug("Получение жанров книги id={}", bookId);
        List<Genres> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("genres_book.find_by_book"));
            ps.setLong(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Genres genre = new Genres();
                genre.setIdGenr(rs.getLong("id_genr"));
                genre.setGenr(rs.getString("genr"));
                list.add(genre);
            }
            logger.debug("Найдено {} жанров для книги id={}", list.size(), bookId);
        } catch (SQLException e) {
            logger.error("Ошибка получения жанров книги id={}", bookId, e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public List<Genres> findGenresOnlyByBook(Long bookId) {
        return findByBook(bookId);
    }

    private void closeResources(ResultSet rs, PreparedStatement ps) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        } catch (SQLException e) {
            logger.error("Ошибка закрытия JDBC ресурса", e);
        }
    }
}