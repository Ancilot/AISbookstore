package ru.an.bookstore;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenresDAO implements Dao<Genres, Long> {
    private static final Logger logger =
            LoggerFactory.getLogger(GenresDAO.class);

    private static Properties property = new Properties();

    public GenresDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
            logger.debug("SQL для GenresDAO загружены");
        } catch (IOException e) {
            logger.error("Ошибка загрузки statements.properties для GenresDAO", e);
        }
    }

//    private final static String FIND_ALL = "SELECT * FROM store.genres ORDER BY genr";
//    private final static String FIND_BY_ID = "SELECT * FROM store.genres WHERE id_genr = ?";
//    private final static String INSERT = "INSERT INTO store.genres (genr) VALUES (?) RETURNING id_genr";
//    private final static String UPDATE = "UPDATE store.genres SET genr = ? WHERE id_genr = ?";
//    private final static String DELETE = "DELETE FROM store.genres WHERE id_genr = ?";
//    private final static String SEARCH = "SELECT * FROM store.search_genres(?)";

    @Override
    public Genres findById(Long id) {
        logger.debug("Поиск жанра id={}", id);
        Genres genre = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("genres.find_by_id"));
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                genre = mapRow(rs);
                logger.debug("Жанр id={} найден", id);
            }
        } catch (SQLException e) {
            logger.error("Ошибка поиска жанра id={}", id, e);
        } finally {
            closeResources(rs, ps);
        }
        return genre;
    }

    @Override
    public Collection<Genres> findAll() {
        logger.debug("Получение всех жанров");
        List<Genres> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("genres.find_all"));
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            logger.debug("Найдено {} жанров", list.size());
        } catch (SQLException e) {
            logger.error("Ошибка получения жанров", e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    @Override
    public Genres save(Genres entity) {
        logger.debug("Добавление жанра '{}'", entity.getGenr());
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("genres.insert"), Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getGenr());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                entity.setIdGenr(rs.getLong(1));
                logger.info("Жанр добавлен id={}, name={}", entity.getIdGenr(), entity.getGenr());
            }
        } catch (SQLException e) {
            logger.error("Ошибка добавления жанра '{}'", entity.getGenr(), e);
        } finally {
            closeResources(rs, ps);
        }
        return entity;
    }

    @Override
    public Genres update(Genres entity) {
        logger.debug("Обновление жанра id={}", entity.getIdGenr());
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("genres.update"));
            ps.setString(1, entity.getGenr());
            ps.setLong(2, entity.getIdGenr());
            ps.executeUpdate();
            logger.info("Жанр обновлён id={}", entity.getIdGenr());
        } catch (SQLException e) {
            logger.error("Ошибка обновления жанра id={}", entity.getIdGenr(), e);
        } finally {
            closeResources(null, ps);
        }
        return entity;
    }

    @Override
    public void delete(Genres entity) {
        deleteById(entity.getIdGenr());
    }

    @Override
    public void deleteById(Long id) {
        logger.debug("Удаление жанра id={}", id);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("genres.delete"));
            ps.setLong(1, id);
            ps.executeUpdate();
            logger.info("Жанр удалён id={}", id);
        } catch (SQLException e) {
            logger.error("Ошибка удаления жанра id={}", id, e);
        } finally {
            closeResources(null, ps);
        }
    }

    public List<Genres> search(String searchText) {
        logger.debug("Поиск жанров по тексту '{}'", searchText);
        List<Genres> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("genres.search"));
            ps.setString(1, searchText);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            logger.debug("Найдено {} жанров по запросу '{}'", list.size(), searchText);
        } catch (SQLException e) {
            logger.error("Ошибка поиска жанров по '{}'", searchText, e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    private void closeResources(ResultSet rs, PreparedStatement ps) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        } catch (SQLException e) {
            logger.error("Ошибка закрытия ресурсов (GenresDAO)", e);
        }
    }

    private Genres mapRow(ResultSet rs) throws SQLException {
        Genres genre = new Genres();
        genre.setIdGenr(rs.getLong("id_genr"));
        genre.setGenr(rs.getString("genr"));
        return genre;
    }
}