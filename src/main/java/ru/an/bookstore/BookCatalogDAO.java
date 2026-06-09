package ru.an.bookstore;

import org.postgresql.util.PSQLException;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class BookCatalogDAO implements Dao<BookCatalog, Long> {

    private static Properties property = new Properties();

    public BookCatalogDAO() {
        try {
            URL url = getClass().getResource("/ru/an/bookstore/statements.properties");
            FileInputStream fis = new FileInputStream(url.getFile());
            property.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private final AuthorsDAO authorsDAO = new AuthorsDAO();
    private final GenresDAO genresDAO = new GenresDAO();
    private final AuthorsBookDAO authorsBookDAO = new AuthorsBookDAO();
    private final GenresBookDAO genresBookDAO = new GenresBookDAO();
    private final PriceDAO priceDAO = new PriceDAO();
    private final ImagesDAO imagesDAO = new ImagesDAO();

//    private final static String FIND_ALL =
//            "SELECT \n" +
//                    "    b.id_book,\n" +
//                    "    b.name_book,\n" +
//                    "    b.isbn,\n" +
//                    "    b.year_publication,\n" +
//                    "    b.number_pages,\n" +
//                    "    b.annotation,\n" +
//                    "    p.name_publishing,\n" +
//                    "    p.id_pub,\n" +
//                    "    COALESCE(\n" +
//                    "        (SELECT string_agg(DISTINCT g.genr, ', ') \n" +
//                    "         FROM store.genres_book gb \n" +
//                    "         JOIN store.genres g ON g.id_genr = gb.genr \n" +
//                    "         WHERE gb.book = b.id_book), '') AS genres,\n" +
//                    "    COALESCE(\n" +
//                    "        (SELECT string_agg(DISTINCT a.surname || ' ' || a.name_author, ', ') \n" +
//                    "         FROM store.authors_book ab \n" +
//                    "         JOIN store.authors a ON a.id_authors = ab.author \n" +
//                    "         WHERE ab.book = b.id_book), '') AS authors,\n" +
//                    "    (SELECT price FROM store.price \n" +
//                    "     WHERE id_books = b.id_book \n" +
//                    "     ORDER BY date_time DESC LIMIT 1) AS price,\n" +
//                    "    (SELECT quantity FROM store.warehouse \n" +
//                    "     WHERE book = b.id_book LIMIT 1) AS quantity\n" +
//                    "FROM store.book_catalog b\n" +
//                    "LEFT JOIN store.publishing_houses p ON p.id_pub = b.publishing_houses\n" +
//                    "ORDER BY b.id_book";
//
//    private final static String FIND_BY_ID =
//            "SELECT\n" +
//                    "    b.id_book,\n" +
//                    "    b.name_book,\n" +
//                    "    b.isbn,\n" +
//                    "    b.year_publication,\n" +
//                    "    b.number_pages,\n" +
//                    "    b.annotation,\n" +
//                    "    p.name_publishing,\n" +
//                    "    p.id_pub\n" +
//                    "FROM store.book_catalog b\n" +
//                    "LEFT JOIN store.publishing_houses p ON p.id_pub = b.publishing_houses\n" +
//                    "WHERE b.id_book = ?";
//
//    private final static String INSERT = "INSERT INTO store.book_catalog " +
//            "(name_book, publishing_houses, year_publication, isbn, number_pages, annotation) " +
//            "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_book";
//
//    private final static String UPDATE = "UPDATE store.book_catalog SET " +
//            "name_book = ?, publishing_houses = ?, year_publication = ?, isbn = ?, number_pages = ?, annotation = ? " +
//            "WHERE id_book = ?";
//
//    private final static String DELETE = "DELETE FROM store.book_catalog WHERE id_book = ?";

    @Override
    public BookCatalog findById(Long id) {
        BookCatalog book = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("book_catalog.find_by_id"));
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                book = mapSimpleRow(rs);

                // Загружаем авторов
                List<Authors> authors = authorsBookDAO.findAuthorsOnlyByBook(id);
                List<AuthorsBook> authorsBookList = new ArrayList<>();
                for (Authors author : authors) {
                    AuthorsBook ab = new AuthorsBook();
                    ab.setAuthor(author);
                    ab.setBook(book);
                    authorsBookList.add(ab);
                }
                book.setAuthorsBook(authorsBookList);

                // Загружаем жанры
                List<Genres> genres = genresBookDAO.findGenresOnlyByBook(id);
                List<GenresBook> genresBookList = new ArrayList<>();
                for (Genres genre : genres) {
                    GenresBook gb = new GenresBook();
                    gb.setGenr(genre);
                    gb.setBook(book);
                    genresBookList.add(gb);
                }
                book.setGenresBooks(genresBookList);

                // Загружаем изображения
                book.setImages(imagesDAO.findByBook(id));

                // Загружаем текущую цену
                Price currentPrice = priceDAO.findCurrentByBook(id);
                if (currentPrice != null) {
                    book.setPrice(currentPrice.getPrice());
                }

                // Формируем строки для отображения
                StringBuilder authorsStr = new StringBuilder();
                for (Authors a : authors) {
                    if (authorsStr.length() > 0) authorsStr.append(", ");
                    authorsStr.append(a.getSurname()).append(" ").append(a.getNameAuthor());
                }
                book.setAuthors(authorsStr.toString());

                StringBuilder genresStr = new StringBuilder();
                for (Genres g : genres) {
                    if (genresStr.length() > 0) genresStr.append(", ");
                    genresStr.append(g.getGenr());
                }
                book.setGenres(genresStr.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return book;
    }

    @Override
    public Collection<BookCatalog> findAll() {
        List<BookCatalog> books = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("book_catalog.find_all"));
            rs = ps.executeQuery();
            while (rs.next()) {
                BookCatalog book = new BookCatalog();
                book.setIdBook(rs.getLong("id_book"));
                book.setNameBook(rs.getString("name_book"));
                book.setIsbn(rs.getString("isbn"));
                book.setGenres(rs.getString("genres"));
                book.setAuthors(rs.getString("authors"));
                book.setPublishingName(rs.getString("name_publishing"));

                int year = rs.getInt("year_publication");
                if (!rs.wasNull() && year > 0) {
                    book.setYearPublication(LocalDate.of(year, 1, 1));
                }

                book.setPrice(rs.getBigDecimal("price"));
                book.setQuantity(rs.getInt("quantity"));
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return books;
    }

    @Override
    public BookCatalog save(BookCatalog entity) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("book_catalog.insert"), Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getNameBook());
            ps.setObject(2, entity.getPublishingHouses() != null ? entity.getPublishingHouses().getIdPub() : null, Types.BIGINT);
            ps.setInt(3, entity.getYearPublication() != null ? entity.getYearPublication().getYear() : 0);
            ps.setString(4, entity.getIsbn());
            ps.setInt(5, entity.getNumberPages());
            ps.setString(6, entity.getAnnotation());
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                Long newId = rs.getLong(1);
                entity.setIdBook(newId);

                if (entity.getPrice() != null) {
                    priceDAO.save(newId, entity.getPrice());
                }

                if (entity.getAuthorsBook() != null) {
                    for (AuthorsBook ab : entity.getAuthorsBook()) {
                        if (ab.getAuthor() != null && ab.getAuthor().getIdAuthors() != null) {
                            authorsBookDAO.save(newId, ab.getAuthor().getIdAuthors());
                        }
                    }
                }

                if (entity.getGenresBooks() != null) {
                    for (GenresBook gb : entity.getGenresBooks()) {
                        if (gb.getGenr() != null && gb.getGenr().getIdGenr() != null) {
                            genresBookDAO.save(newId, gb.getGenr().getIdGenr());
                        }
                    }
                }

                if (entity.getImages() != null) {
                    for (Images img : entity.getImages()) {
                        if (img.getImagePath() != null && !img.getImagePath().isEmpty()) {
                            imagesDAO.save(newId, img.getImagePath());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return entity;
    }

    @Override
    public BookCatalog update(BookCatalog entity) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("book_catalog.update"));
            ps.setString(1, entity.getNameBook());
            ps.setObject(2, entity.getPublishingHouses() != null ? entity.getPublishingHouses().getIdPub() : null, Types.BIGINT);
            ps.setInt(3, entity.getYearPublication() != null ? entity.getYearPublication().getYear() : 0);
            ps.setString(4, entity.getIsbn());
            ps.setInt(5, entity.getNumberPages());
            ps.setString(6, entity.getAnnotation());
            ps.setLong(7, entity.getIdBook());
            ps.executeUpdate();

            if (entity.getPrice() != null) {
                priceDAO.save(entity.getIdBook(), entity.getPrice());
            }

            authorsBookDAO.deleteByBook(entity.getIdBook());
            if (entity.getAuthorsBook() != null) {
                for (AuthorsBook ab : entity.getAuthorsBook()) {
                    if (ab.getAuthor() != null && ab.getAuthor().getIdAuthors() != null) {
                        authorsBookDAO.save(entity.getIdBook(), ab.getAuthor().getIdAuthors());
                    }
                }
            }

            genresBookDAO.deleteByBook(entity.getIdBook());
            if (entity.getGenresBooks() != null) {
                for (GenresBook gb : entity.getGenresBooks()) {
                    if (gb.getGenr() != null && gb.getGenr().getIdGenr() != null) {
                        genresBookDAO.save(entity.getIdBook(), gb.getGenr().getIdGenr());
                    }
                }
            }

            if (entity.getImages() != null && !entity.getImages().isEmpty()) {
                for (Images img : entity.getImages()) {
                    if (img.getIdImage() == null && img.getImagePath() != null && !img.getImagePath().isEmpty()) {
                        imagesDAO.save(entity.getIdBook(), img.getImagePath());
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, ps, conn);
        }
        return entity;
    }

    @Override
    public void delete(BookCatalog entity) {
        deleteById(entity.getIdBook());
    }

    @Override
    public void deleteById(Long id) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("book_catalog.delete"));
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e instanceof PSQLException pgEx && pgEx.getServerErrorMessage() != null) {
                throw new RuntimeException(pgEx.getServerErrorMessage().getMessage());
            }
            throw new RuntimeException(e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public List<BookCatalog> search(String text) {
        List<BookCatalog> books = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBHelper.getConnection();
            ps = conn.prepareStatement(property.getProperty("book_catalog.search"));
            if (text == null || text.trim().isEmpty()) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, text.trim());
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                BookCatalog book = new BookCatalog();
                book.setIdBook(rs.getLong("id_book"));
                book.setNameBook(rs.getString("name_book"));
                book.setIsbn(rs.getString("isbn"));
                int year = rs.getInt("year_publication");
                if (!rs.wasNull()) {
                    book.setYearPublication(LocalDate.of(year, 1, 1));
                }
                book.setNumberPages(rs.getInt("number_pages"));
                book.setGenres(rs.getString("genres"));
                book.setAuthors(rs.getString("authors"));
                book.setPrice(rs.getBigDecimal("price"));
                book.setQuantity(rs.getInt("quantity"));
                book.setPublishingName(rs.getString("publishing_name"));
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }
        return books;
    }

    public List<Authors> searchAuthors(String searchText) {
        return authorsDAO.search(searchText);
    }

    public List<Genres> searchGenres(String searchText) {
        return genresDAO.search(searchText);
    }

    private BookCatalog mapSimpleRow(ResultSet rs) throws SQLException {
        BookCatalog book = new BookCatalog();
        book.setIdBook(rs.getLong("id_book"));
        book.setNameBook(rs.getString("name_book"));
        book.setIsbn(rs.getString("isbn"));
        int year = rs.getInt("year_publication");
        if (!rs.wasNull() && year > 0) {
            book.setYearPublication(LocalDate.of(year, 1, 1));
        }
        book.setNumberPages(rs.getInt("number_pages"));
        book.setAnnotation(rs.getString("annotation"));
        book.setPublishingName(rs.getString("name_publishing"));

        PublishingHouses pub = new PublishingHouses();
        pub.setIdPub(rs.getLong("id_pub"));
        pub.setNamePublishing(rs.getString("name_publishing"));
        book.setPublishingHouses(pub);

        return book;
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
}