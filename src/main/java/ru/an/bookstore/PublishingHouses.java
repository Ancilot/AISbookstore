package ru.an.bookstore;

public class PublishingHouses {
    private Long idPub;
    private String namePublishing;
    private Adress adress;
    private String numberPublishing;
    private String email;

    public Long getIdPub() {
        return idPub;
    }

    public void setIdPub(Long idPub) {
        this.idPub = idPub;
    }

    public String getNamePublishing() {
        return namePublishing;
    }

    public void setNamePublishing(String namePublishing) {
        this.namePublishing = namePublishing;
    }

    public Adress getAdress() {
        return adress;
    }

    public void setAdress(Adress adress) {
        this.adress = adress;
    }

    public String getNumberPublishing() {
        return numberPublishing;
    }

    public void setNumberPublishing(String numberPublishing) {
        this.numberPublishing = numberPublishing;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return getNamePublishing();
    }
}
