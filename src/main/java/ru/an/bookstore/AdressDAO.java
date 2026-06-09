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

public class AdressDAO implements Dao<Adress, Long> {
    private static final Logger logger =
            LoggerFactory.getLogger(AdressDAO.class);

    private static Properties property = new Properties();

    public AdressDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
            logger.debug("SQL-запросы для AdressDAO загружены");
        } catch (IOException e) {
            logger.error("Ошибка загрузки statements.properties", e);
        }
    }

//    private final static String FIND_ALL = "SELECT * FROM store.adress ORDER BY country, region, city";
//    private final static String FIND_BY_ID = "SELECT * FROM store.adress WHERE id_adress = ?";
//    private final static String INSERT = "INSERT INTO store.adress (country, region, city, street, house) VALUES (?, ?, ?, ?, ?) RETURNING id_adress";
//    private final static String UPDATE = "UPDATE store.adress SET country = ?, region = ?, city = ?, street = ?, house = ? WHERE id_adress = ?";
//    private final static String DELETE = "DELETE FROM store.adress WHERE id_adress = ?";

    @Override
    public Adress findById(Long id) {
        logger.debug("Поиск адреса по id={}", id);
        Adress adress = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("adress.find_by_id"));
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                adress = mapRow(rs);
                logger.debug("Адрес найден: id={}", id);
            }
        } catch (SQLException e) {
            logger.error("Ошибка поиска адреса по id={}", id, e);
        } finally {
            closeResources(rs, ps);
        }
        return adress;
    }

    @Override
    public Collection<Adress> findAll() {
        logger.debug("Получение списка всех адресов");
        List<Adress> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("adress.find_all"));
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            logger.debug("Получено {} адресов", list.size());
        } catch (SQLException e) {
            logger.error("Ошибка получения списка адресов", e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    @Override
    public Adress save(Adress entity) {
        logger.debug("Добавление адреса: {}, {}, {}",
                entity.getCountry(),
                entity.getCity(),
                entity.getStreet());
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("adress.insert"), Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getCountry());
            ps.setString(2, entity.getRegion());
            ps.setString(3, entity.getCity());
            ps.setString(4, entity.getStreet());
            ps.setString(5, entity.getHouse());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                entity.setIdAdress(rs.getLong(1));
                logger.info("Добавлен адрес с id={}", entity.getIdAdress());
            }
        } catch (SQLException e) {
            logger.error("Ошибка добавления адреса", e);
        } finally {
            closeResources(rs, ps);
        }
        return entity;
    }

    @Override
    public Adress update(Adress entity) {
        logger.debug("Обновление адреса id={}", entity.getIdAdress());
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("adress.update"));
            ps.setString(1, entity.getCountry());
            ps.setString(2, entity.getRegion());
            ps.setString(3, entity.getCity());
            ps.setString(4, entity.getStreet());
            ps.setString(5, entity.getHouse());
            ps.setLong(6, entity.getIdAdress());
            ps.executeUpdate();
            logger.info("Адрес id={} успешно обновлён",
                    entity.getIdAdress());
        } catch (SQLException e) {
            logger.error("Ошибка обновления адреса id={}",
                    entity.getIdAdress(), e);
        } finally {
            closeResources(null, ps);
        }
        return entity;
    }

    @Override
    public void delete(Adress entity) {
        deleteById(entity.getIdAdress());
    }

    @Override
    public void deleteById(Long id) {
        logger.debug("Удаление адреса id={}", id);
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("adress.delete"));
            ps.setLong(1, id);
            ps.executeUpdate();
            logger.info("Адрес id={} удалён", id);
        } catch (SQLException e) {
            logger.error("Ошибка удаления адреса id={}", id, e);
        } finally {
            closeResources(null, ps);
        }
    }

    private void closeResources(ResultSet rs, PreparedStatement ps) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        } catch (SQLException e) {
            logger.error("Ошибка закрытия JDBC ресурсов", e);
        }
    }

    private Adress mapRow(ResultSet rs) throws SQLException {
        Adress adress = new Adress();
        adress.setIdAdress(rs.getLong("id_adress"));
        adress.setCountry(rs.getString("country"));
        adress.setRegion(rs.getString("region"));
        adress.setCity(rs.getString("city"));
        adress.setStreet(rs.getString("street"));
        adress.setHouse(rs.getString("house"));
        return adress;
    }
}