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

public class AuthorsDAO implements Dao<Authors, Long> {
    private static final Logger logger =
            LoggerFactory.getLogger(AuthorsDAO.class);

    private static Properties property = new Properties();

    public AuthorsDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
            logger.debug("SQL-запросы для AuthorsDAO загружены");
        } catch (IOException e) {
            logger.error("Ошибка загрузки statements.properties", e);
        }
    }

//    private final static String FIND_ALL = "SELECT * FROM store.authors ORDER BY surname, name_author";
//    private final static String FIND_BY_ID = "SELECT * FROM store.authors WHERE id_authors = ?";
//    private final static String INSERT = "INSERT INTO store.authors (surname, name_author, patronymic) VALUES (?, ?, ?) RETURNING id_authors";
//    private final static String UPDATE = "UPDATE store.authors SET surname = ?, name_author = ?, patronymic = ? WHERE id_authors = ?";
//    private final static String DELETE = "DELETE FROM store.authors WHERE id_authors = ?";
//    private final static String SEARCH = "SELECT * FROM store.search_authors(?)";

    @Override
    public Authors findById(Long id) {
        logger.debug("Поиск автора по id={}", id);
        Authors author = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("authors.find_by_id"));
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                author = mapRow(rs);
                logger.debug("Автор найден: id={}", id);
            }
        } catch (SQLException e) {
            logger.error("Ошибка поиска автора по id={}", id, e);
        } finally {
            closeResources(rs, ps);
        }
        return author;
    }

    @Override
    public Collection<Authors> findAll() {
        logger.debug("Получение списка авторов");
        List<Authors> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("authors.find_all"));
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            logger.debug("Получено {} авторов", list.size());
        } catch (SQLException e) {
            logger.error("Ошибка получения списка авторов", e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    @Override
    public Authors save(Authors entity) {
        logger.debug(
                "Добавление автора: {} {}",
                entity.getSurname(),
                entity.getNameAuthor()
        );
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("authors.insert"), Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getSurname());
            ps.setString(2, entity.getNameAuthor());
            ps.setString(3, entity.getPatronymic());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                entity.setIdAuthors(rs.getLong(1));
            }
            logger.info(
                    "Добавлен автор id={}",
                    entity.getIdAuthors()
            );
        } catch (SQLException e) {
            logger.error(
                    "Ошибка добавления автора {} {}",
                    entity.getSurname(),
                    entity.getNameAuthor(),
                    e
            );
        } finally {
            closeResources(rs, ps);
        }
        return entity;
    }

    @Override
    public Authors update(Authors entity) {
        logger.debug(
                "Обновление автора id={}",
                entity.getIdAuthors()
        );
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("authors.update"));
            ps.setString(1, entity.getSurname());
            ps.setString(2, entity.getNameAuthor());
            ps.setString(3, entity.getPatronymic());
            ps.setLong(4, entity.getIdAuthors());
            ps.executeUpdate();
            logger.info(
                    "Автор id={} обновлён",
                    entity.getIdAuthors()
            );
        } catch (SQLException e) {
            logger.error(
                    "Ошибка обновления автора id={}",
                    entity.getIdAuthors(),
                    e
            );
        } finally {
            closeResources(null, ps);
        }
        return entity;
    }

    @Override
    public void delete(Authors entity) {
        deleteById(entity.getIdAuthors());
    }

    @Override
    public void deleteById(Long id) {
        logger.debug(
                "Удаление автора id={}",
                id
        );
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("authors.delete"));
            ps.setLong(1, id);
            ps.executeUpdate();
            logger.info(
                    "Автор id={} удалён",
                    id
            );
        } catch (SQLException e) {
            logger.error(
                    "Ошибка удаления автора id={}",
                    id,
                    e
            );
        } finally {
            closeResources(null, ps);
        }
    }

    public List<Authors> search(String searchText) {
        logger.debug(
                "Поиск авторов по строке '{}'",
                searchText
        );
        List<Authors> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("authors.search"));
            ps.setString(1, searchText);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            logger.debug(
                    "По запросу '{}' найдено {} авторов",
                    searchText,
                    list.size()
            );
        } catch (SQLException e) {
            logger.error(
                    "Ошибка поиска авторов по строке '{}'",
                    searchText,
                    e
            );
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    private void closeResources(ResultSet rs, PreparedStatement ps) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            logger.error("Ошибка закрытия ResultSet", e);
        }
        try {
            if (ps != null) ps.close();
        } catch (SQLException e) {
            logger.error("Ошибка закрытия PreparedStatement", e);
        }
    }

    private Authors mapRow(ResultSet rs) throws SQLException {
        Authors author = new Authors();
        author.setIdAuthors(rs.getLong("id_authors"));
        author.setSurname(rs.getString("surname"));
        author.setNameAuthor(rs.getString("name_author"));
        author.setPatronymic(rs.getString("patronymic"));
        return author;
    }
}