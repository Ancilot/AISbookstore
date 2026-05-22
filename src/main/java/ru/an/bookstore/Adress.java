package ru.an.bookstore;

public class Adress {
    private Long idAdress;
    private String country;
    private String region;
    private String city;
    private String street;
    private String house;

    public void setIdAdress(Long idAdress) {
        this.idAdress = idAdress;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public Long getIdAdress() {
        return idAdress;
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getHouse() {
        return house;
    }
}
