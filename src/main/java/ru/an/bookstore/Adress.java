package ru.an.bookstore;

import java.util.Objects;

public class Adress {
    private Long idAdress;
    private String country;
    private String region;
    private String city;
    private String street;
    private String house;

    public Adress() {
    }

    public Adress(String country, String region, String city, String street, String house) {
        this.country = country;
        this.region = region;
        this.city = city;
        this.street = street;
        this.house = house;
    }

    public Long getIdAdress() {
        return idAdress;
    }

    public void setIdAdress(Long idAdress) {
        this.idAdress = idAdress;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s %s",
                country, region, city, street, house);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Adress adress = (Adress) o;
        return Objects.equals(idAdress, adress.idAdress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAdress);
    }
}