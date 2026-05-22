package ru.an.bookstore;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoyaltyBase {
    private String cardNumber;
    private Clients client;
    private LocalDate dateCard;
    private BigDecimal ransomAmount;
    private String discount;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Clients getClient() {
        return client;
    }

    public void setClient(Clients client) {
        this.client = client;
    }

    public LocalDate getDateCard() {
        return dateCard;
    }

    public void setDateCard(LocalDate dateCard) {
        this.dateCard = dateCard;
    }

    public BigDecimal getRansomAmount() {
        return ransomAmount;
    }

    public void setRansomAmount(BigDecimal ransomAmount) {
        this.ransomAmount = ransomAmount;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }
}
