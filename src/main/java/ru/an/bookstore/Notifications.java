package ru.an.bookstore;

import java.time.LocalDate;

public class Notifications {
    private Long idNotification;
    private Clients client;
    private String textNotification;
    private LocalDate dateNotification;
    private String status;

    public Long getIdNotification() {
        return idNotification;
    }

    public void setIdNotification(Long idNotification) {
        this.idNotification = idNotification;
    }

    public Clients getClient() {
        return client;
    }

    public void setClient(Clients client) {
        this.client = client;
    }

    public String getTextNotification() {
        return textNotification;
    }

    public void setTextNotification(String textNotification) {
        this.textNotification = textNotification;
    }

    public LocalDate getDateNotification() {
        return dateNotification;
    }

    public void setDateNotification(LocalDate dateNotification) {
        this.dateNotification = dateNotification;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
