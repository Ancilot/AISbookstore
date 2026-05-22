module ru.an.bookstore {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens ru.an.bookstore to javafx.fxml;
    exports ru.an.bookstore;
}