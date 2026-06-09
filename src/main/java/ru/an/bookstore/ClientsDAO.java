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

public class ClientsDAO implements Dao<Clients, Long> {
    private static final Logger logger =
            LoggerFactory.getLogger(ClientsDAO.class);

    private static Properties property = new Properties();

    public ClientsDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
            logger.debug("SQL-запросы для ClientsDAO загружены");
        } catch (IOException e) {
            logger.error("Ошибка загрузки statements.properties", e);
        }
    }

//    private final static String FIND_ALL_ACTIVE =
//            "SELECT * FROM store.clients WHERE archiv IS NOT TRUE ORDER BY surname, name_client";
//
//    private final static String FIND_ALL_ARCHIVE =
//            "SELECT * FROM store.clients WHERE archiv = TRUE ORDER BY surname, name_client";
//
//    private final static String FIND_BY_ID =
//            "SELECT * FROM store.clients WHERE id_client = ?";
//
//    private final static String INSERT =
//            "INSERT INTO store.clients (surname, name_client, patrontmic, number_client, email) " +
//                    "VALUES (?, ?, ?, ?, ?) RETURNING id_client";
//
//    private final static String UPDATE =
//            "UPDATE store.clients SET surname = ?, name_client = ?, patrontmic = ?, " +
//                    "number_client = ?, email = ? WHERE id_client = ?";
//
//    private final static String SEARCH =
//            "SELECT * FROM store.clients WHERE archiv IS NOT TRUE AND " +
//                    "(surname ILIKE ? OR name_client ILIKE ? OR patrontmic ILIKE ?) " +
//                    "ORDER BY surname, name_client";
//
//    private final static String DELETE_CLIENT =
//            "{ ? = call store.delete_client(?) }";

    @Override
    public Clients findById(Long id) {
        logger.debug("Поиск клиента id={}", id);
        Clients client = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("clients.find_by_id"));
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                client = mapRow(rs);
                logger.debug("Клиент id={} найден", id);
            }
        } catch (SQLException e) {
            logger.error("Ошибка поиска клиента id={}", id, e);
        } finally {
            closeResources(rs, ps);
        }
        return client;
    }

    @Override
    public Collection<Clients> findAll() {
        logger.debug("Получение активных клиентов");
        List<Clients> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("clients.find_all_active"));
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            logger.debug("Получено {} активных клиентов", list.size());
        } catch (SQLException e) {
            logger.error("Ошибка получения активных клиентов", e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public Collection<Clients> findAllArchive() {
        logger.debug("Получение архивных клиентов");
        List<Clients> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("clients.find_all_archive"));
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            logger.debug("Получено {} архивных клиентов", list.size());
        } catch (SQLException e) {
            logger.error("Ошибка получения архивных клиентов", e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    @Override
    public Clients save(Clients entity) {
        logger.debug("Добавление клиента {} {}", entity.getSurname(), entity.getNameClient());
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("clients.insert"), Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getSurname());
            ps.setString(2, entity.getNameClient());
            ps.setString(3, entity.getPatrontmic());
            ps.setString(4, entity.getNumberClient());
            ps.setString(5, entity.getEmail());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                entity.setIdClient(rs.getLong(1));
                logger.info("Добавлен клиент id={}", entity.getIdClient());
            }
        } catch (SQLException e) {
            logger.error(
                    "Ошибка добавления клиента {} {}",
                    entity.getSurname(),
                    entity.getNameClient(),
                    e
            );
        } finally {
            closeResources(rs, ps);
        }
        return entity;
    }

    @Override
    public Clients update(Clients entity) {
        logger.debug("Обновление клиента id={}", entity.getIdClient());
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("clients.update"));
            ps.setString(1, entity.getSurname());
            ps.setString(2, entity.getNameClient());
            ps.setString(3, entity.getPatrontmic());
            ps.setString(4, entity.getNumberClient());
            ps.setString(5, entity.getEmail());
            ps.setLong(6, entity.getIdClient());
            ps.executeUpdate();
            logger.info("Клиент id={} обновлён", entity.getIdClient());
        } catch (SQLException e) {
            logger.error("Ошибка обновления клиента id={}", entity.getIdClient(), e);
        } finally {
            closeResources(null, ps);
        }
        return entity;
    }

    @Override
    public void delete(Clients entity) {
        deleteById(entity.getIdClient());
    }

    @Override
    public void deleteById(Long id) {
        logger.debug("Удаление клиента id={}", id);
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = DBHelper.getConnection();
            cs = conn.prepareCall(property.getProperty("clients.delete"));
            cs.registerOutParameter(1, Types.INTEGER);
            cs.setInt(2, id.intValue());
            cs.execute();
            logger.info("Клиент id={} удалён", id);
        } catch (SQLException e) {
            logger.error("Ошибка удаления клиента id={}", id, e);
        } finally {
            closeResources(null, cs);
        }
    }

    public int deleteClientToArchive(Long clientId) {
        logger.debug("Перенос клиента id={} в архив", clientId);
        Connection conn = null;
        CallableStatement cs = null;
        int result = 0;
        try {
            conn = DBHelper.getConnection();
            cs = conn.prepareCall(property.getProperty("clients.delete"));
            cs.registerOutParameter(1, Types.INTEGER);
            cs.setInt(2, clientId.intValue());
            cs.execute();
            result = cs.getInt(1);
            logger.info("Клиент id={} отправлен в архив, результат={}", clientId, result);
        } catch (SQLException e) {
            logger.error("Ошибка архивации клиента id={}", clientId, e);
        } finally {
            closeResources(null, cs);
        }
        return result;
    }

    public List<Clients> search(String searchText) {
        logger.debug("Поиск клиентов по тексту '{}'", searchText);
        List<Clients> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("clients.search"));
            String searchPattern = "%" + searchText + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            logger.debug("По запросу '{}' найдено {} клиентов", searchText, list.size());
        } catch (SQLException e) {
            logger.error("Ошибка поиска клиентов по '{}'", searchText, e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    private Clients mapRow(ResultSet rs) throws SQLException {
        Clients client = new Clients();
        client.setIdClient(rs.getLong("id_client"));
        client.setSurname(rs.getString("surname"));
        client.setNameClient(rs.getString("name_client"));
        client.setPatrontmic(rs.getString("patrontmic"));
        client.setNumberClient(rs.getString("number_client"));
        client.setEmail(rs.getString("email"));
        return client;
    }

    private void closeResources(ResultSet rs, Statement st) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {logger.error("Ошибка закрытия ResultSet", e);}
        try { if (st != null) st.close(); } catch (SQLException e) {logger.error("Ошибка закрытия Statement", e);}
    }
}