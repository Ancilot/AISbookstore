package ru.an.bookstore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class BookCatalogDAO implements Dao<BookCatalog, Long> {
    private Properties property = new Properties();
    private final static String FIND_ALL = "SELECT\n" +
            "    b.id_book,\n" +
            "    b.name_book,\n" +
            "    b.isbn,\n" +
            "    b.year_publication,\n" +
            "    p.name_publishing,\n" +
            "    string_agg(DISTINCT g.genr, ', ') AS genres,\n" +
            "    string_agg(DISTINCT a.surname || ' ' || a.name_author, ', ') AS authors,\n" +
            "    pr.price,\n" +
            "    w.quantity\n" +
            "FROM store.book_catalog b\n" +
            "LEFT JOIN store.publishing_houses p ON p.id_pub = b.publishing_houses\n" +
            "LEFT JOIN store.genres_book gb ON gb.book = b.id_book\n" +
            "LEFT JOIN store.genres g ON g.id_genr = gb.genr\n" +
            "LEFT JOIN store.authors_book ab ON ab.book = b.id_book\n" +
            "LEFT JOIN store.authors a ON a.id_authors = ab.author\n" +
            "LEFT JOIN store.price pr ON pr.id_books = b.id_book\n" +
            "LEFT JOIN store.warehouse w ON w.book = b.id_book\n" +
            "GROUP BY\n" +
            "    b.id_book,\n" +
            "    b.name_book,\n" +
            "    b.isbn,\n" +
            "    b.year_publication,\n" +
            "    p.name_publishing,\n" +
            "    pr.price,\n" +
            "    w.quantity;";

    protected List<BookCatalog> mapper(ResultSet rs){
        List<BookCatalog> books = new ArrayList<>();
        try {
            while (rs.next()) {

                BookCatalog book = new BookCatalog();

                book.setIdBook(rs.getLong("id_book"));
                book.setNameBook(rs.getString("name_book"));
                book.setGenres(rs.getString("genres"));
                book.setAuthors(rs.getString("authors"));
                book.setPublishingName(rs.getString("name_publishing"));
                book.setYearPublication(rs.getDate("year_publication").toLocalDate());
                book.setIsbn(rs.getString("isbn"));
                book.setPrice(rs.getBigDecimal("price"));
                book.setQuantity(rs.getInt("quantity"));
                books.add(book);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return books;
    }
    @Override
    public BookCatalog findById(Long id) {
        return null;
    }

    @Override
    public Collection<BookCatalog> findAll() {

        List<BookCatalog> books = new ArrayList<>();
        ResultSet rs = null;
        try(PreparedStatement statement =
                    DBHelper
                            .getConnection()
                            .prepareStatement(FIND_ALL)) {


            rs = statement.executeQuery();
            books = mapper(rs);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    @Override
    public BookCatalog save(BookCatalog entity) {
        return null;
    }

    @Override
    public BookCatalog update(BookCatalog entity) {
        return null;
    }

    @Override
    public void delete(BookCatalog entity) {

    }

    @Override
    public void deleteById(Long id) {

    }
}