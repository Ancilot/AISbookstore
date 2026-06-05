package ru.an.bookstore;

import java.util.ArrayList;
import java.util.List;

public class Authors {
    private Long idAuthors;
    private String surname;
    private String nameAuthor;
    private String patronymic;
    private List<AuthorsBook> authorsBooks;

    public Authors() {
        this.authorsBooks = new ArrayList<>();
    }

    public List<AuthorsBook> getAuthorsBooks() {
        return authorsBooks;
    }

    public void setAuthorsBooks(List<AuthorsBook> authorsBooks) {
        this.authorsBooks = authorsBooks;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public void setIdAuthors(Long idAuthors) {
        this.idAuthors = idAuthors;
    }

    public String getNameAuthor() {
        return nameAuthor;
    }

    public void setNameAuthor(String nameAuthor) {
        this.nameAuthor = nameAuthor;
    }

    public Long getIdAuthors() {
        return idAuthors;
    }
}
