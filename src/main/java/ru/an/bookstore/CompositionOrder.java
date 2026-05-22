package ru.an.bookstore;

public class CompositionOrder {
    private Long idComposition;
    private Orders orders;
    private BookCatalog book;
    private Integer quantity;

    public Long getIdComposition() {
        return idComposition;
    }

    public void setIdComposition(Long idComposition) {
        this.idComposition = idComposition;
    }

    public Orders getOrders() {
        return orders;
    }

    public void setOrders(Orders orders) {
        this.orders = orders;
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
}
