package ru.an.bookstore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Checks {
    private Long idCheck;
    private LocalDateTime dateTime;
    private Clients client;
    private BigDecimal sumCheck;
    private String status;

    public Clients getClient() {
        return client;
    }

    public void setClient(Clients client) {
        this.client = client;
    }

    public Long getIdCheck() {
        return idCheck;
    }

    public void setIdCheck(Long idCheck) {
        this.idCheck = idCheck;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public BigDecimal getSumCheck() {
        return sumCheck;
    }

    public void setSumCheck(BigDecimal sumCheck) {
        this.sumCheck = sumCheck;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
