package ru.an.bookstore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookCatalog {
    private Long idBook;
    private String nameBook;
    private PublishingHouses publishingHouses;
    private LocalDate yearPublication;
    private String isbn;
    private Integer numberPages;
    private String annotation;
    private List<Price> prices;
    private List<Images> images;
    private List<AuthorsBook> authorsBook;
    private List<GenresBook> genresBooks;
    private String genres;
    private String authors;
    private String publishingName;
    private BigDecimal price;
    private Integer quantity;

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getPublishingName() {
        return publishingName;
    }

    public void setPublishingName(String publishingName) {
        this.publishingName = publishingName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BookCatalog() {
        this.prices = new ArrayList<>();
        // Инициализация коллекции
        this.images = new ArrayList<>();
        this.authorsBook = new ArrayList<>();
        this.genresBooks = new ArrayList<>();
    }

    public List<Price> getPrise() {
        return prices;
    }

    public void setPrise(List<Price> prise) {
        this.prices = prise;
    }

    public List<Images> getImages() {
        return images;
    }

    public void setImages(List<Images> images) {
        this.images = images;
    }

    public List<AuthorsBook> getAuthorsBook() {
        return authorsBook;
    }

    public void setAuthorsBook(List<AuthorsBook> authorsBook) {
        this.authorsBook = authorsBook;
    }

    public List<GenresBook> getGenresBooks() {
        return genresBooks;
    }

    public void setGenresBooks(List<GenresBook> genresBooks) {
        this.genresBooks = genresBooks;
    }

    public Long getIdBook() {
        return idBook;
    }

    public void setIdBook(Long idBook) {
        this.idBook = idBook;
    }

    public String getNameBook() {
        return nameBook;
    }

    public void setNameBook(String nameBook) {
        this.nameBook = nameBook;
    }



    public LocalDate getYearPublication() {
        return yearPublication;
    }

    public void setYearPublication(LocalDate yearPublication) {
        this.yearPublication = yearPublication;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getNumberPages() {
        return numberPages;
    }

    public void setNumberPages(Integer numberPages) {
        this.numberPages = numberPages;
    }



    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public PublishingHouses getPublishingHouses() {
        return publishingHouses;
    }

    public void setPublishingHouses(PublishingHouses publishingHouses) {
        this.publishingHouses = publishingHouses;
    }

}
