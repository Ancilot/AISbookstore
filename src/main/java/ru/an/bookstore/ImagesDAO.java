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

public class ImagesDAO {
    private static final Logger logger =
            LoggerFactory.getLogger(ImagesDAO.class);

    private static Properties property = new Properties();

    public ImagesDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
            logger.debug("SQL-запросы ImagesDAO загружены");
        } catch (IOException e) {
            logger.error("Ошибка загрузки statements.properties для ImagesDAO", e);
        }
    }

//    private final static String INSERT = "INSERT INTO store.images (image, id_book) VALUES (?, ?) RETURNING id_image";
//    private final static String FIND_BY_BOOK = "SELECT * FROM store.images WHERE id_book = ?";
//    private final static String DELETE_BY_BOOK = "DELETE FROM store.images WHERE id_book = ?";
//    private final static String DELETE_BY_ID = "DELETE FROM store.images WHERE id_image = ?";

    public Images save(Long bookId, String imagePath) {
        logger.debug("Добавление изображения: bookId={}, path={}", bookId, imagePath);
        Images image = new Images();
        image.setImagePath(imagePath);
        image.setIdBook(bookId);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("images.insert"), Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, imagePath);
            ps.setLong(2, bookId);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                image.setIdImage(rs.getLong(1));
                logger.info("Изображение добавлено id={}, bookId={}", image.getIdImage(), bookId);
            }
        } catch (SQLException e) {
            logger.error("Ошибка добавления изображения bookId={}, path={}", bookId, imagePath, e);
        } finally {
            closeResources(rs, ps);
        }
        return image;
    }

    public List<Images> findByBook(Long bookId) {
        logger.debug("Поиск изображений для книги id={}", bookId);
        List<Images> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("images.find_by_book"));
            ps.setLong(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Images image = new Images();
                image.setIdImage(rs.getLong("id_image"));
                image.setImagePath(rs.getString("image"));
                image.setIdBook(rs.getLong("id_book"));
                list.add(image);
            }
            logger.debug("Найдено {} изображений для книги id={}", list.size(), bookId);
        } catch (SQLException e) {
            logger.error("Ошибка поиска изображений bookId={}", bookId, e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public void deleteByBook(Long bookId) {
        logger.debug("Удаление изображений книги id={}", bookId);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("images.delete_by_book"));
            ps.setLong(1, bookId);
            ps.executeUpdate();
            logger.info("Изображения удалены для книги id={}", bookId);
        } catch (SQLException e) {
            logger.error("Ошибка удаления изображений книги id={}", bookId, e);
        } finally {
            closeResources(null, ps);
        }
    }

    public void deleteById(Long id) {
        logger.debug("Удаление изображения id={}", id);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("images.delete_by_id"));
            ps.setLong(1, id);
            ps.executeUpdate();
            logger.info("Изображение удалено id={}", id);
        } catch (SQLException e) {
            logger.error("Ошибка удаления изображения id={}", id, e);
        } finally {
            closeResources(null, ps);
        }
    }

    private void closeResources(ResultSet rs, PreparedStatement ps) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        } catch (SQLException e) {
            logger.error("Ошибка закрытия ресурсов в ImagesDAO", e);
        }
    }
}