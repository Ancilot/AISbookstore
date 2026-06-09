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
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ResourceBundle;

public class ReportController {

    @FXML
    private ResourceBundle resources;

    private final ReportDAO reportDAO = new ReportDAO();
    private Stage stage;

    @FXML private ComboBox<String> tfPeriod;

    // Продажи
    @FXML private TableView<ReportDAO.SalesReport> tvSales;
    @FXML private TableColumn<ReportDAO.SalesReport, Long> colChecksCount;
    @FXML private TableColumn<ReportDAO.SalesReport, Long> colBooksCount;
    @FXML private TableColumn<ReportDAO.SalesReport, BigDecimal> colTotalAmount;

    // Остатки товаров
    @FXML private TableView<ReportDAO.StockReport> tvStock;
    @FXML private TableColumn<ReportDAO.StockReport, String> colStockName;
    @FXML private TableColumn<ReportDAO.StockReport, Integer> colStockQuantity;
    @FXML private TableColumn<ReportDAO.StockReport, BigDecimal> colStockPrice;
    @FXML private TableColumn<ReportDAO.StockReport, BigDecimal> colStockAmount;

    // Популярность авторов
    @FXML private TableView<ReportDAO.PopularityReport> tvAuthorPopularity;
    @FXML private TableColumn<ReportDAO.PopularityReport, String> colAuthorName;
    @FXML private TableColumn<ReportDAO.PopularityReport, Long> colAuthorSold;
    @FXML private TableColumn<ReportDAO.PopularityReport, BigDecimal> colAuthorAmount;

    // Популярность жанров
    @FXML private TableView<ReportDAO.PopularityReport> tvGenrePopularity;
    @FXML private TableColumn<ReportDAO.PopularityReport, String> colGenreName;
    @FXML private TableColumn<ReportDAO.PopularityReport, Long> colGenreSold;
    @FXML private TableColumn<ReportDAO.PopularityReport, BigDecimal> colGenreAmount;

    // Топ книг
    @FXML private TableView<ReportDAO.PopularityReport> tvBookPopularity;
    @FXML private TableColumn<ReportDAO.PopularityReport, Long> colBookPlace;
    @FXML private TableColumn<ReportDAO.PopularityReport, String> colBookName;
    @FXML private TableColumn<ReportDAO.PopularityReport, Long> colBookSold;
    @FXML private TableColumn<ReportDAO.PopularityReport, BigDecimal> colBookAmount;

    private ObservableList<ReportDAO.SalesReport> salesList = FXCollections.observableArrayList();
    private ObservableList<ReportDAO.StockReport> stockList = FXCollections.observableArrayList();
    private ObservableList<ReportDAO.PopularityReport> authorList = FXCollections.observableArrayList();
    private ObservableList<ReportDAO.PopularityReport> genreList = FXCollections.observableArrayList();
    private ObservableList<ReportDAO.PopularityReport> bookList = FXCollections.observableArrayList();

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    @FXML
    void initialize() {
        tfPeriod.setItems(FXCollections.observableArrayList(
                resources.getString("period.day"),
                resources.getString("period.month"),
                resources.getString("period.year")
        ));
        tfPeriod.setValue(resources.getString("period.month"));

        // Продажи
        colChecksCount.setCellValueFactory(new PropertyValueFactory<>("checksCount"));
        colBooksCount.setCellValueFactory(new PropertyValueFactory<>("booksCount"));
        colTotalAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        tvSales.setItems(salesList);

        // Остатки
        colStockName.setCellValueFactory(new PropertyValueFactory<>("nameBook"));
        colStockQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colStockPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colStockAmount.setCellValueFactory(new PropertyValueFactory<>("stockAmount"));
        tvStock.setItems(stockList);

        // Популярность авторов
        colAuthorName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAuthorSold.setCellValueFactory(new PropertyValueFactory<>("soldQuantity"));
        colAuthorAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        tvAuthorPopularity.setItems(authorList);

        // Популярность жанров
        colGenreName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGenreSold.setCellValueFactory(new PropertyValueFactory<>("soldQuantity"));
        colGenreAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        tvGenrePopularity.setItems(genreList);

        // Топ книг
        colBookPlace.setCellValueFactory(new PropertyValueFactory<>("place"));
        colBookName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colBookSold.setCellValueFactory(new PropertyValueFactory<>("soldQuantity"));
        colBookAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        tvBookPopularity.setItems(bookList);

        refreshAllReports();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void onPeriod(ActionEvent actionEvent) {
        refreshAllReports();
    }

    private void refreshAllReports() {
        String periodValue = tfPeriod.getValue();
        String period;
        if (periodValue.equals(resources.getString("period.day"))) {
            period = "день";
        } else if (periodValue.equals(resources.getString("period.month"))) {
            period = "месяц";
        } else {
            period = "год";
        }

        ReportDAO.SalesReport sales = reportDAO.getSalesReport(period);
        if (sales != null) {
            salesList.clear();
            salesList.add(sales);
        }

        stockList.setAll(reportDAO.getStockReport());
        authorList.setAll(reportDAO.getAuthorPopularity(period));
        genreList.setAll(reportDAO.getGenrePopularity(period));
        bookList.setAll(reportDAO.getBookPopularity(period));
    }

    @FXML
    public void onMain(ActionEvent actionEvent) {
        navigateTo(actionEvent, "main.fxml", resources.getString("app.title"));
    }

    @FXML
    public void onClient(ActionEvent actionEvent) {
        navigateTo(actionEvent, "clients.fxml", resources.getString("app.title.clients"));
    }

    @FXML
    public void onWarehouse(ActionEvent actionEvent) {
        navigateTo(actionEvent, "warehouse.fxml", resources.getString("app.title.warehouse"));
    }

    @FXML
    public void onOreder(ActionEvent actionEvent) {
        navigateTo(actionEvent, "orders.fxml", resources.getString("app.title.orders"));
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }

    private void navigateTo(ActionEvent actionEvent, String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainController.class.getResource(fxml), resources);
            Scene scene = new Scene(loader.load(), 1200, 600);
            Stage stage = (Stage) ((MenuItem) actionEvent.getSource())
                    .getParentPopup().getOwnerWindow();
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}