package ru.an.bookstore;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.IOException;

public class ReportController {
    public void setStage(Stage stage) {
    }

    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void onMain(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource("main.fxml"));

            Scene scene = new Scene(loader.load(), 1200, 600);

            Stage stage = (Stage) ((MenuItem) actionEvent.getSource())
                    .getParentPopup()
                    .getOwnerWindow();

            stage.setTitle("Главная");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClient(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource("clients.fxml"));

            Scene scene = new Scene(loader.load(), 1200, 600);

            Stage stage = (Stage) ((MenuItem) actionEvent.getSource())
                    .getParentPopup()
                    .getOwnerWindow();

            stage.setTitle("Клиенты");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onWarehouse(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource("warehouse.fxml"));

            Scene scene = new Scene(loader.load(), 1200, 600);

            Stage stage = (Stage) ((MenuItem) actionEvent.getSource())
                    .getParentPopup()
                    .getOwnerWindow();

            stage.setTitle("Склад");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onOreder(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource("orders.fxml"));

            Scene scene = new Scene(loader.load(), 1200, 600);

            Stage stage = (Stage) ((MenuItem) actionEvent.getSource())
                    .getParentPopup()
                    .getOwnerWindow();

            stage.setTitle("Отчеты");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
