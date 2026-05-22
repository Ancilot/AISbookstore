package ru.an.bookstore;

public class GenresBook {
    private Long idGb;
    private Genres genr;
    private BookCatalog book;

    public Long getIdGb() {
        return idGb;
    }

    public void setIdGb(Long idGb) {
        this.idGb = idGb;
    }

    public Genres getGenr() {
        return genr;
    }

    public void setGenr(Genres genr) {
        this.genr = genr;
    }

    public BookCatalog getBook() {
        return book;
    }

    public void setBook(BookCatalog book) {
        this.book = book;
    }
}
