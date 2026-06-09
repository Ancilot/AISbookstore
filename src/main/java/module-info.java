module ru.an.bookstore {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires org.postgresql.jdbc;
    requires org.slf4j;


    opens ru.an.bookstore to javafx.fxml;
    exports ru.an.bookstore;
}