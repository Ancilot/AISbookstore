package ru.an.bookstore;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthorsBookDAO {

    private final static String INSERT = "INSERT INTO store.authors_book (author, book) VALUES (?, ?)";
    private final static String DELETE_BY_BOOK = "DELETE FROM store.authors_book WHERE book = ?";
    private final static String DELETE_BY_BOOK_AND_AUTHOR = "DELETE FROM store.authors_book WHERE book = ? AND author = ?";
    private final static String FIND_BY_BOOK = "SELECT a.* FROM store.authors_book ab " +
            "JOIN store.authors a ON a.id_authors = ab.author WHERE ab.book = ?";
    private final static String FIND_AUTHORS_NOT_IN_BOOK = "SELECT * FROM store.authors a " +
            "WHERE a.id_authors NOT IN (SELECT author FROM store.authors_book WHERE book = ?) " +
            "ORDER BY a.surname, a.name_author";

    public void save(Long bookId, Long authorId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(INSERT);
            ps.setLong(1, authorId);
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

    public void delete(Long bookId, Long authorId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(DELETE_BY_BOOK_AND_AUTHOR);
            ps.setLong(1, bookId);
            ps.setLong(2, authorId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public List<Authors> findByBook(Long bookId) {
        List<Authors> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(FIND_BY_BOOK);
            ps.setLong(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Authors author = new Authors();
                author.setIdAuthors(rs.getLong("id_authors"));
                author.setSurname(rs.getString("surname"));
                author.setNameAuthor(rs.getString("name_author"));
                author.setPatronymic(rs.getString("patronymic"));
                list.add(author);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return list;
    }

    public List<Authors> findAuthorsOnlyByBook(Long bookId) {
        return findByBook(bookId);
    }

    public List<Authors> findAuthorsNotInBook(Long bookId) {
        List<Authors> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(FIND_AUTHORS_NOT_IN_BOOK);
            ps.setLong(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Authors author = new Authors();
                author.setIdAuthors(rs.getLong("id_authors"));
                author.setSurname(rs.getString("surname"));
                author.setNameAuthor(rs.getString("name_author"));
                author.setPatronymic(rs.getString("patronymic"));
                list.add(author);
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