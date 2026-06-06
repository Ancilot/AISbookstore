package ru.an.bookstore;

import java.time.LocalDate;

public class Invoice {
    private Long idInvoice;
    private Suppliers supplier;
    private Warehouse warehouse;
    private LocalDate dateInvoice;
    private Integer quantity;
    private Price price;

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Price getPrice() {
        return price;
    }

    public Long getIdInvoice() {
        return idInvoice;
    }

    public void setIdInvoice(Long idInvoice) {
        this.idInvoice = idInvoice;
    }

    public Suppliers getSupplier() {
        return supplier;
    }

    public void setSupplier(Suppliers supplier) {
        this.supplier = supplier;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public LocalDate getDateInvoice() {
        return dateInvoice;
    }

    public void setDateInvoice(LocalDate dateInvoice) {
        this.dateInvoice = dateInvoice;
    }
}
