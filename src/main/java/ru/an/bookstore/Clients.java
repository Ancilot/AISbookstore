package ru.an.bookstore;

public class Clients {
    private Long idClient;
    private String surname;
    private String nameClient;
    private String patrontmic;
    private String numberClient;
    private String email;

    public Long getIdClient() {
        return idClient;
    }

    public void setIdClient(Long idClient) {
        this.idClient = idClient;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getNameClient() {
        return nameClient;
    }

    public void setNameClient(String nameClient) {
        this.nameClient = nameClient;
    }

    public String getPatrontmic() {
        return patrontmic;
    }

    public void setPatrontmic(String patrontmic) {
        this.patrontmic = patrontmic;
    }

    public String getNumberClient() {
        return numberClient;
    }

    public void setNumberClient(String numberClient) {
        this.numberClient = numberClient;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
