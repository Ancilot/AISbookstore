package ru.an.bookstore;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImagesDAO {

    private final static String INSERT = "INSERT INTO store.images (image, id_book) VALUES (?, ?) RETURNING id_image";
    private final static String FIND_BY_BOOK = "SELECT * FROM store.images WHERE id_book = ?";
    private final static String DELETE_BY_BOOK = "DELETE FROM store.images WHERE id_book = ?";
    private final static String DELETE_BY_ID = "DELETE FROM store.images WHERE id_image = ?";

    public Images save(Long bookId, String imagePath) {
        Images image = new Images();
        image.setImagePath(imagePath);
        image.setIdBook(bookId);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, imagePath);
            ps.setLong(2, bookId);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                image.setIdImage(rs.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return image;
    }

    public List<Images> findByBook(Long bookId) {
        List<Images> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(FIND_BY_BOOK);
            ps.setLong(1, bookId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Images image = new Images();
                image.setIdImage(rs.getLong("id_image"));
                image.setImagePath(rs.getString("image"));
                image.setIdBook(rs.getLong("id_book"));
                list.add(image);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return list;
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

    public void deleteById(Long id) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(DELETE_BY_ID);
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
}