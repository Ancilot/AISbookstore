package ru.an.bookstore;

import java.math.BigDecimal;

public class CompositionCheck {
    private Long idComposition;
    private Checks checks;
    private BookCatalog book;
    private Integer quantity;
    private BigDecimal priceTime;

    public Long getIdComposition() {
        return idComposition;
    }

    public void setIdComposition(Long idComposition) {
        this.idComposition = idComposition;
    }

    public Checks getChecks() {
        return checks;
    }

    public void setChecks(Checks checks) {
        this.checks = checks;
    }

    public BookCatalog getBook() {
        return book;
    }

    public void setBook(BookCatalog book) {
        this.book = book;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceTime() {
        return priceTime;
    }

    public void setPriceTime(BigDecimal priceTime) {
        this.priceTime = priceTime;
    }
}
