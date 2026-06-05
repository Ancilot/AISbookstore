package ru.an.bookstore;

import java.util.ArrayList;
import java.util.List;

public class Genres {
    private Long idGenr;
    private String genr;
    private List<GenresBook> genresBooks;

    public Genres() {
        this.genresBooks = new ArrayList<>();
    }

    public List<GenresBook> getGenresBooks() {
        return genresBooks;
    }

    public void setGenresBooks(List<GenresBook> genresBooks) {
        this.genresBooks = genresBooks;
    }

    public Long getIdGenr() {
        return idGenr;
    }

    public void setIdGenr(Long idGenr) {
        this.idGenr = idGenr;
    }

    public String getGenr() {
        return genr;
    }

    public void setGenr(String genr) {
        this.genr = genr;
    }
}
