package ru.an.bookstore;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class PublishingHousesDAO implements Dao<PublishingHouses, Long> {

    private static Properties property = new Properties();

    public PublishingHousesDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final AdressDAO adressDAO = new AdressDAO();

//    private final static String FIND_ALL = "SELECT * FROM store.publishing_houses ORDER BY name_publishing";
//    private final static String FIND_BY_ID =
//            "SELECT ph.*, a.country, a.region, a.city, a.street, a.house " +
//                    "FROM store.publishing_houses ph " +
//                    "LEFT JOIN store.adress a ON a.id_adress = ph.adress " +
//                    "WHERE ph.id_pub = ?";
//    private final static String INSERT = "INSERT INTO store.publishing_houses (name_publishing, adress, number_publishing, email) VALUES (?, ?, ?, ?) RETURNING id_pub";
//    private final static String UPDATE = "UPDATE store.publishing_houses SET name_publishing = ?, adress = ?, number_publishing = ?, email = ? WHERE id_pub = ?";
//    private final static String DELETE = "DELETE FROM store.publishing_houses WHERE id_pub = ?";

    @Override
    public PublishingHouses findById(Long id) {
        PublishingHouses publisher = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("publishing_houses.find_by_id"));
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                publisher = mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return publisher;
    }

    @Override
    public Collection<PublishingHouses> findAll() {
        List<PublishingHouses> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("publishing_houses.find_all"));
            rs = ps.executeQuery();
            while (rs.next()) {
                PublishingHouses publisher = new PublishingHouses();
                publisher.setIdPub(rs.getLong("id_pub"));
                publisher.setNamePublishing(rs.getString("name_publishing"));
                publisher.setNumberPublishing(rs.getString("number_publishing"));
                publisher.setEmail(rs.getString("email"));

                Long adressId = rs.getLong("adress");
                if (adressId != null && adressId > 0 && !rs.wasNull()) {
                    publisher.setAdress(adressDAO.findById(adressId));
                }
                list.add(publisher);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return list;
    }

    @Override
    public PublishingHouses save(PublishingHouses entity) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("publishing_houses.insert"), Statement.RETURN_GENERATED_KEYS);

            if (entity.getAdress() != null && entity.getAdress().getIdAdress() == null) {
                adressDAO.save(entity.getAdress());
            }

            Long adressId = entity.getAdress() != null ? entity.getAdress().getIdAdress() : null;

            ps.setString(1, entity.getNamePublishing());
            if (adressId != null) {
                ps.setLong(2, adressId);
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, entity.getNumberPublishing());
            ps.setString(4, entity.getEmail());
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                entity.setIdPub(rs.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return entity;
    }

    @Override
    public PublishingHouses update(PublishingHouses entity) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("publishing_houses.update"));

            if (entity.getAdress() != null) {
                if (entity.getAdress().getIdAdress() == null) {
                    adressDAO.save(entity.getAdress());
                } else {
                    adressDAO.update(entity.getAdress());
                }
            }

            Long adressId = entity.getAdress() != null ? entity.getAdress().getIdAdress() : null;

            ps.setString(1, entity.getNamePublishing());
            if (adressId != null) {
                ps.setLong(2, adressId);
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, entity.getNumberPublishing());
            ps.setString(4, entity.getEmail());
            ps.setLong(5, entity.getIdPub());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
        return entity;
    }

    @Override
    public void delete(PublishingHouses entity) {
        deleteById(entity.getIdPub());
    }

    @Override
    public void deleteById(Long id) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("publishing_houses.delete"));
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
    }

    private void closeResources(ResultSet rs, PreparedStatement ps, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) DBHelper.close(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private PublishingHouses mapRow(ResultSet rs) throws SQLException {
        PublishingHouses publisher = new PublishingHouses();
        publisher.setIdPub(rs.getLong("id_pub"));
        publisher.setNamePublishing(rs.getString("name_publishing"));
        publisher.setNumberPublishing(rs.getString("number_publishing"));
        publisher.setEmail(rs.getString("email"));

        if (rs.getString("country") != null) {
            Adress adress = new Adress();
            adress.setIdAdress(rs.getLong("adress"));
            adress.setCountry(rs.getString("country"));
            adress.setRegion(rs.getString("region"));
            adress.setCity(rs.getString("city"));
            adress.setStreet(rs.getString("street"));
            adress.setHouse(rs.getString("house"));
            publisher.setAdress(adress);
        }

        return publisher;
    }
}