package ru.an.bookstore;

import java.time.LocalDate;

public class BookCatalog {
    private Long idBook;
    private String nameBook;
    private PublishingHouses publishingHouses;
    private LocalDate yearPublication;
    private String isbn;
    private Integer numberPages;
    private Price price;
    private String annotation;
    private Images image;

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

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public Images getImage() {
        return image;
    }

    public void setImage(Images image) {
        this.image = image;
    }
}
