package ru.an.bookstore;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SuppliersDAO implements Dao<Suppliers, Long> {

    private final static String FIND_ALL = "SELECT * FROM store.suppliers ORDER BY name_supplier";
    private final static String FIND_BY_ID = "SELECT * FROM store.suppliers WHERE id_supplier = ?";

    @Override
    public Suppliers findById(Long id) {
        Suppliers supplier = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(FIND_BY_ID);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                supplier = mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return supplier;
    }

    @Override
    public Collection<Suppliers> findAll() {
        List<Suppliers> list = new ArrayList<>();
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
    public Suppliers save(Suppliers entity) { return entity; }

    @Override
    public Suppliers update(Suppliers entity) { return entity; }

    @Override
    public void delete(Suppliers entity) { }

    @Override
    public void deleteById(Long id) { }

    private Suppliers mapRow(ResultSet rs) throws SQLException {
        Suppliers supplier = new Suppliers();
        supplier.setIdSupplier(rs.getLong("id_supplier"));
        supplier.setNameSupplier(rs.getString("name_supplier"));
        supplier.setNumberSupplier(rs.getString("number_supplier"));
        supplier.setEmail(rs.getString("email"));
        return supplier;
    }

    private void closeResources(ResultSet rs, PreparedStatement ps, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (ps != null) ps.close(); } catch (SQLException e) {}
        try { if (conn != null) DBHelper.close(conn); } catch (Exception e) {}
    }
}