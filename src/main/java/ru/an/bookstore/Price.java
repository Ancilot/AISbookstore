package ru.an.bookstore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Price {
    private Long idPrice;
    private BigDecimal price;
    private LocalDateTime dateTime;
    private Long idBooks;

    public Long getIdPrice() {
        return idPrice;
    }

    public void setIdPrice(Long idPrice) {
        this.idPrice = idPrice;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Long getIdBooks() {
        return idBooks;
    }

    public void setIdBooks(Long idBooks) {
        this.idBooks = idBooks;
    }
}