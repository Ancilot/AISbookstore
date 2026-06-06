package ru.an.bookstore;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GenresBookDAO {

    private final static String INSERT = "INSERT INTO store.genres_book (genr, book) VALUES (?, ?)";
    private final static String DELETE_BY_BOOK = "DELETE FROM store.genres_book WHERE book = ?";
    private final static String DELETE_BY_BOOK_AND_GENRE = "DELETE FROM store.genres_book WHERE book = ? AND genr = ?";
    private final static String FIND_BY_BOOK = "SELECT g.* FROM store.genres_book gb " +
            "JOIN store.genres g ON g.id_genr = gb.genr WHERE gb.book = ?";
    private final static String FIND_GENRES_NOT_IN_BOOK = "SELECT * FROM store.genres g " +
            "WHERE g.id_genr NOT IN (SELECT genr FROM store.genres_book WHERE book = ?) " +
            "ORDER BY g.genr";

    public void save(Long bookId, Long genreId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(INSERT);
            ps.setLong(1, genreId);
            ps.setLong(2, bookId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public void deleteByBook(Long bookId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(DELETE_BY_BOOK);
            ps.setLong(1, bookId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public void delete(Long bookId, Long genreId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(DELETE_BY_BOOK_AND_GENRE);
            ps.setLong(1, bookId);
            ps.setLong(2, genreId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public List<Genres> findByBook(Long bookId) {
        List<Genres> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(FIND_BY_BOOK);
            ps.setLong(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Genres genre = new Genres();
                genre.setIdGenr(rs.getLong("id_genr"));
                genre.setGenr(rs.getString("genr"));
                list.add(genre);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return list;
    }

    public List<Genres> findGenresOnlyByBook(Long bookId) {
        return findByBook(bookId);
    }

    public List<Genres> findGenresNotInBook(Long bookId) {
        List<Genres> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(FIND_GENRES_NOT_IN_BOOK);
            ps.setLong(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Genres genre = new Genres();
                genre.setIdGenr(rs.getLong("id_genr"));
                genre.setGenr(rs.getString("genr"));
                list.add(genre);
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
}