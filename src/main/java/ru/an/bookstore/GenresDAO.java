package ru.an.bookstore;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class GenresDAO implements Dao<Genres, Long> {

    private static Properties property = new Properties();

    public GenresDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return genre;
    }

    @Override
    public Collection<Genres> findAll() {
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
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return list;
    }

    @Override
    public Genres save(Genres entity) {
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return entity;
    }

    @Override
    public Genres update(Genres entity) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("genres.update"));
            ps.setString(1, entity.getGenr());
            ps.setLong(2, entity.getIdGenr());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
        return entity;
    }

    @Override
    public void delete(Genres entity) {
        deleteById(entity.getIdGenr());
    }

    @Override
    public void deleteById(Long id) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("genres.delete"));
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public List<Genres> search(String searchText) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return list;
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

    private Genres mapRow(ResultSet rs) throws SQLException {
        Genres genre = new Genres();
        genre.setIdGenr(rs.getLong("id_genr"));
        genre.setGenr(rs.getString("genr"));
        return genre;
    }
}