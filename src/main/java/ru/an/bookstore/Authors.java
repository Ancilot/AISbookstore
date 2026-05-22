package ru.an.bookstore;

public class Authors {
    private Long idAuthors;
    private String surname;
    private String nameAuthor;
    private String patronymic;

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
