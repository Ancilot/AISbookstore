package ru.an.bookstore;

public class AuthorsBook {
    private Long idAb;
    private Authors author;
    private BookCatalog book;



    public Long getIdAb() {
        return idAb;
    }

    public void setIdAb(Long idAb) {
        this.idAb = idAb;
    }

    public BookCatalog getBook() {
        return book;
    }

    public void setBook(BookCatalog book) {
        this.book = book;
    }

    public Authors getAuthor() {
        return author;
    }

    public void setAuthor(Authors author) {
        this.author = author;
    }
}
