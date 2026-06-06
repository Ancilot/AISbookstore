package ru.an.bookstore;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AuthorsDAO implements Dao<Authors, Long> {

    private final static String FIND_ALL = "SELECT * FROM store.authors ORDER BY surname, name_author";
    private final static String FIND_BY_ID = "SELECT * FROM store.authors WHERE id_authors = ?";
    private final static String INSERT = "INSERT INTO store.authors (surname, name_author, patronymic) VALUES (?, ?, ?) RETURNING id_authors";
    private final static String UPDATE = "UPDATE store.authors SET surname = ?, name_author = ?, patronymic = ? WHERE id_authors = ?";
    private final static String DELETE = "DELETE FROM store.authors WHERE id_authors = ?";
    private final static String SEARCH = "SELECT * FROM store.search_authors(?)";

    @Override
    public Authors findById(Long id) {
        Authors author = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(FIND_BY_ID);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                author = mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return author;
    }

    @Override
    public Collection<Authors> findAll() {
        List<Authors> list = new ArrayList<>();
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
    public Authors save(Authors entity) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getSurname());
            ps.setString(2, entity.getNameAuthor());
            ps.setString(3, entity.getPatronymic());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                entity.setIdAuthors(rs.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return entity;
    }

    @Override
    public Authors update(Authors entity) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(UPDATE);
            ps.setString(1, entity.getSurname());
            ps.setString(2, entity.getNameAuthor());
            ps.setString(3, entity.getPatronymic());
            ps.setLong(4, entity.getIdAuthors());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
        return entity;
    }

    @Override
    public void delete(Authors entity) {
        deleteById(entity.getIdAuthors());
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

    public List<Authors> search(String searchText) {
        List<Authors> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(SEARCH);
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (ps != null) ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) DBHelper.close(conn);
        } catch (Exception e) {
            e.printStackTrace();
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