package ru.an.bookstore;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportDAO {

    private static final Logger logger =
            LoggerFactory.getLogger(ReportDAO.class);

    private static Properties property = new Properties();

    public ReportDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
            logger.debug("SQL-запросы для ReportDAO загружены");
        } catch (IOException e) {
            logger.error("Ошибка загрузки statements.properties", e);
        }
    }

    // Продажи
//    private final static String REPORT_SALES = "SELECT * FROM store.report_sales(?)";
//
//    // Остатки товаров (VIEW)
//    private final static String REPORT_STOCK = "SELECT * FROM store.report_stock";
//
//    // Популярность по авторам
//    private final static String REPORT_AUTHOR_POPULARITY = "SELECT * FROM store.report_author_popularity(?)";
//
//    // Популярность по жанрам
//    private final static String REPORT_GENRE_POPULARITY = "SELECT * FROM store.report_genre_popularity(?)";
//
//    // Популярность по книгам
//    private final static String REPORT_BOOK_POPULARITY = "SELECT * FROM store.report_book_popularity(?)";

    // Класс для хранения данных о продажах
    public static class SalesReport {
        public long checksCount;
        public long booksCount;
        public BigDecimal totalAmount;

        public SalesReport(long checksCount, long booksCount, BigDecimal totalAmount) {
            this.checksCount = checksCount;
            this.booksCount = booksCount;
            this.totalAmount = totalAmount;
        }

        public long getChecksCount() {
            return checksCount;
        }

        public long getBooksCount() {
            return booksCount;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
    }

    // Класс для остатков товаров
    public static class StockReport {
        public long idBook;
        public String nameBook;
        public int quantity;
        public BigDecimal currentPrice;
        public BigDecimal stockAmount;

        public StockReport(long idBook, String nameBook, int quantity,
                           BigDecimal currentPrice, BigDecimal stockAmount) {
            this.idBook = idBook;
            this.nameBook = nameBook;
            this.quantity = quantity;
            this.currentPrice = currentPrice;
            this.stockAmount = stockAmount;
        }

        public long getIdBook() {
            return idBook;
        }

        public String getNameBook() {
            return nameBook;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getCurrentPrice() {
            return currentPrice;
        }

        public BigDecimal getStockAmount() {
            return stockAmount;
        }

    }

    // Класс для популярности
    public static class PopularityReport {
        public String name;
        public long soldQuantity;
        public BigDecimal totalAmount;
        public Long place; // для книг

        public PopularityReport(String name, long soldQuantity, BigDecimal totalAmount) {
            this.name = name;
            this.soldQuantity = soldQuantity;
            this.totalAmount = totalAmount;
        }

        public PopularityReport(Long place, String name, long soldQuantity, BigDecimal totalAmount) {
            this.place = place;
            this.name = name;
            this.soldQuantity = soldQuantity;
            this.totalAmount = totalAmount;
        }

        // Геттеры
        public String getName() { return name; }
        public long getSoldQuantity() { return soldQuantity; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public Long getPlace() { return place; }
    }


    public SalesReport getSalesReport(String period) {
        logger.debug("Формирование отчёта по продажам, период={}", period);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        SalesReport report = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("report.sales"));
            ps.setString(1, period);
            rs = ps.executeQuery();
            if (rs.next()) {
                report = new SalesReport(
                        rs.getLong("checks_count"),
                        rs.getLong("books_count"),
                        rs.getBigDecimal("total_amount")
                );
                logger.debug(
                        "Отчёт по продажам сформирован: чеков={}, книг={}, сумма={}",
                        report.getChecksCount(),
                        report.getBooksCount(),
                        report.getTotalAmount()
                );
            }
        } catch (SQLException e) {
            logger.error(
                    "Ошибка формирования отчёта по продажам, период={}",
                    period,
                    e
            );
        } finally {
            closeResources(rs, ps);
        }
        return report;
    }

    public List<StockReport> getStockReport() {
        logger.debug("Формирование отчёта по остаткам");
        List<StockReport> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("report.stock"));
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new StockReport(
                        rs.getLong("id_book"),
                        rs.getString("name_book"),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("current_price"),
                        rs.getBigDecimal("stock_amount")
                ));
            }
            logger.debug(
                    "Отчёт по остаткам сформирован, записей={}",
                    list.size()
            );
        } catch (SQLException e) {
            logger.error("Ошибка формирования отчёта по остаткам", e);
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public List<PopularityReport> getAuthorPopularity(String period) {
        logger.debug(
                "Формирование отчёта популярности авторов, период={}",
                period
        );
        List<PopularityReport> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("report.author_popularity"));
            ps.setString(1, period);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new PopularityReport(
                        rs.getString("author_name"),
                        rs.getLong("sold_quantity"),
                        rs.getBigDecimal("total_amount")
                ));
            }
            logger.debug(
                    "Получено {} записей популярности авторов",
                    list.size()
            );
        } catch (SQLException e) {
            logger.error(
                    "Ошибка формирования отчёта популярности авторов, период={}",
                    period,
                    e
            );
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public List<PopularityReport> getGenrePopularity(String period) {
        logger.debug(
                "Формирование отчёта популярности жанров, период={}",
                period
        );
        List<PopularityReport> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("report.genre_popularity"));
            ps.setString(1, period);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new PopularityReport(
                        rs.getString("genre"),
                        rs.getLong("sold_quantity"),
                        rs.getBigDecimal("total_amount")
                ));
            }
            logger.debug(
                    "Получено {} записей популярности жанров",
                    list.size()
            );
        } catch (SQLException e) {
            logger.error(
                    "Ошибка формирования отчёта популярности жанров, период={}",
                    period,
                    e
            );
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public List<PopularityReport> getBookPopularity(String period) {
        logger.debug(
                "Формирование отчёта популярности книг, период={}",
                period
        );
        List<PopularityReport> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("report.book_popularity"));
            ps.setString(1, period);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new PopularityReport(
                        rs.getLong("place"),
                        rs.getString("book_name"),
                        rs.getLong("sold_quantity"),
                        rs.getBigDecimal("total_amount")
                ));
            }
            logger.debug(
                    "Получено {} записей популярности книг",
                    list.size()
            );
        } catch (SQLException e) {
            logger.error(
                    "Ошибка формирования отчёта популярности книг, период={}",
                    period,
                    e
            );
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    private void closeResources(ResultSet rs, Statement st) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {logger.error("Ошибка закрытия ResultSet", e);}
        try { if (st != null) st.close(); } catch (SQLException e) {logger.error("Ошибка закрытия Statement", e);}
    }
}