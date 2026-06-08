package ru.an.bookstore;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    private final static String INSERT =
            "INSERT INTO store.invoice (supplier, warehouse, date_invoice, quanity, price) " +
                    "VALUES (?, ?, ?, ?, ?) RETURNING id_invoice";

    private final static String FIND_ALL =
            "SELECT i.*, s.name_supplier, w.book " +
                    "FROM store.invoice i " +
                    "LEFT JOIN store.suppliers s ON s.id_supplier = i.supplier " +
                    "LEFT JOIN store.warehouse w ON w.id_warehouse = i.warehouse " +
                    "ORDER BY i.date_invoice DESC";

    public Invoice save(Invoice invoice) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, invoice.getSupplier().getIdSupplier());
            ps.setLong(2, invoice.getWarehouse().getIdWarehouse());
            ps.setDate(3, Date.valueOf(invoice.getDateInvoice()));
            ps.setInt(4, invoice.getQuantity());
            ps.setBigDecimal(5, invoice.getPrice().getPrice());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                invoice.setIdInvoice(rs.getLong(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            closeResources(rs, ps, conn);
        }
        return invoice;
    }

    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();
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

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setIdInvoice(rs.getLong("id_invoice"));
        invoice.setDateInvoice(rs.getDate("date_invoice").toLocalDate());
        invoice.setQuantity(rs.getInt("quanity"));

        Suppliers supplier = new Suppliers();
        supplier.setIdSupplier(rs.getLong("supplier"));
        supplier.setNameSupplier(rs.getString("name_supplier"));
        invoice.setSupplier(supplier);

        Warehouse warehouse = new Warehouse();
        warehouse.setIdWarehouse(rs.getLong("warehouse"));
        invoice.setWarehouse(warehouse);

        Price price = new Price();
        price.setPrice(rs.getBigDecimal("price"));
        invoice.setPrice(price);

        return invoice;
    }

    private void closeResources(ResultSet rs, PreparedStatement ps, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (ps != null) ps.close(); } catch (SQLException e) {}
        try { if (conn != null) DBHelper.close(conn); } catch (Exception e) {}
    }
}