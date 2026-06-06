package ru.an.bookstore;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AdressDAO implements Dao<Adress, Long> {

    private final static String FIND_ALL = "SELECT * FROM store.adress ORDER BY country, region, city";
    private final static String FIND_BY_ID = "SELECT * FROM store.adress WHERE id_adress = ?";
    private final static String INSERT = "INSERT INTO store.adress (country, region, city, street, house) VALUES (?, ?, ?, ?, ?) RETURNING id_adress";
    private final static String UPDATE = "UPDATE store.adress SET country = ?, region = ?, city = ?, street = ?, house = ? WHERE id_adress = ?";
    private final static String DELETE = "DELETE FROM store.adress WHERE id_adress = ?";

    @Override
    public Adress findById(Long id) {
        Adress adress = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(FIND_BY_ID);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                adress = mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return adress;
    }

    @Override
    public Collection<Adress> findAll() {
        List<Adress> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(FIND_ALL);
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

    @Override
    public Adress save(Adress entity) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getCountry());
            ps.setString(2, entity.getRegion());
            ps.setString(3, entity.getCity());
            ps.setString(4, entity.getStreet());
            ps.setString(5, entity.getHouse());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                entity.setIdAdress(rs.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return entity;
    }

    @Override
    public Adress update(Adress entity) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(UPDATE);
            ps.setString(1, entity.getCountry());
            ps.setString(2, entity.getRegion());
            ps.setString(3, entity.getCity());
            ps.setString(4, entity.getStreet());
            ps.setString(5, entity.getHouse());
            ps.setLong(6, entity.getIdAdress());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
        return entity;
    }

    @Override
    public void delete(Adress entity) {
        deleteById(entity.getIdAdress());
    }

    @Override
    public void deleteById(Long id) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(DELETE);
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