package ru.an.bookstore;

public class CompositionInvoice {
    private Long idComposition;
    private Invoice invoice;
    private BookCatalog book;
    private Integer quantity;
    private Price price;

    public Long getIdComposition() {
        return idComposition;
    }

    public void setIdComposition(Long idComposition) {
        this.idComposition = idComposition;
    }



    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public BookCatalog getBook() {
        return book;
    }

    public void setBook(BookCatalog book) {
        this.book = book;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }
}
