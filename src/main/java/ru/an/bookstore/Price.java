package ru.an.bookstore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Price {
    private Long idPrice;
    private BigDecimal price;
    private LocalDateTime dateTime;

    public BookCatalog getBookId() {
        return bookId;
    }

    public void setBookId(BookCatalog bookId) {
        this.bookId = bookId;
    }

    private BookCatalog bookId;

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getIdPrice() {
        return idPrice;
    }

    public void setIdPrice(Long idPrice) {
        this.idPrice = idPrice;
    }
}
