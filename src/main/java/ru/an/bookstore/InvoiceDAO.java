package ru.an.bookstore;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceDAO {

    private static final Logger logger =
            LoggerFactory.getLogger(InvoiceDAO.class);

    private static Properties property = new Properties();

    public InvoiceDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private final static String INSERT =
//            "INSERT INTO store.invoice (supplier, warehouse, date_invoice, quanity, price) " +
//                    "VALUES (?, ?, ?, ?, ?) RETURNING id_invoice";
//
//    private final static String FIND_ALL =
//            "SELECT i.*, s.name_supplier, w.book " +
//                    "FROM store.invoice i " +
//                    "LEFT JOIN store.suppliers s ON s.id_supplier = i.supplier " +
//                    "LEFT JOIN store.warehouse w ON w.id_warehouse = i.warehouse " +
//                    "ORDER BY i.date_invoice DESC";

    public Invoice save(Invoice invoice) {
        logger.debug(
                "Создание накладной supplierId={}, warehouseId={}, quantity={}",
                invoice.getSupplier().getIdSupplier(),
                invoice.getWarehouse().getIdWarehouse(),
                invoice.getQuantity()
        );
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("invoice.insert"), Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, invoice.getSupplier().getIdSupplier());
            ps.setLong(2, invoice.getWarehouse().getIdWarehouse());
            ps.setDate(3, Date.valueOf(invoice.getDateInvoice()));
            ps.setInt(4, invoice.getQuantity());
            ps.setBigDecimal(5, invoice.getPrice().getPrice());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                invoice.setIdInvoice(rs.getLong(1));
                logger.info(
                        "Накладная создана id={}",
                        invoice.getIdInvoice()
                );
            }
        } catch (SQLException e) {
            logger.error(
                    "Ошибка создания накладной supplierId={}, warehouseId={}",
                    invoice.getSupplier().getIdSupplier(),
                    invoice.getWarehouse().getIdWarehouse(),
                    e
            );
            throw new RuntimeException(e);
        } finally {
            closeResources(rs, ps);
        }
        return invoice;
    }

    public List<Invoice> findAll() {
        logger.debug("Получение всех накладных");
        List<Invoice> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("invoice.find_all"));
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            logger.debug("Найдено {} накладных", list.size());
        } catch (SQLException e) {
            logger.error("Ошибка получения списка накладных", e);
        } finally {
            closeResources(rs, ps);
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

    private void closeResources(ResultSet rs, PreparedStatement ps) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {logger.error("Ошибка закрытия ресурса InvoiceDAO", e);}
        try { if (ps != null) ps.close(); } catch (SQLException e) {logger.error("Ошибка закрытия ресурса InvoiceDAO", e);}
    }
}