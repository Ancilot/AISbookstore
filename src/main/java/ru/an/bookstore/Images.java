package ru.an.bookstore;

public class Images {
    private Long idImage;
    private String imagePath;
    private BookCatalog bookId;

    public BookCatalog getBookId() {
        return bookId;
    }

    public void setBookId(BookCatalog bookId) {
        this.bookId = bookId;
    }

    public Long getIdImage() {
        return idImage;
    }

    public void setIdImage(Long idImage) {
        this.idImage = idImage;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
