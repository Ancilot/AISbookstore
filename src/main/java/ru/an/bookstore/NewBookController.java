package ru.an.bookstore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;
public class NewBookController {
    private BookCatalog book;
    private Stage stage;
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setBook(BookCatalog book) {
        this.book = book;

        if (book != null) {
            // заполнение полей для редактирования
        }
    }

    public void onExit(ActionEvent actionEvent) {
        stage.close();
    }
}
