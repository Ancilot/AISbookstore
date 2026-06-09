package ru.an.bookstore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class InvoiceController {

    @FXML private ResourceBundle resources;
    @FXML private TextField tfCount;
    @FXML private TextField tfPrice;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Suppliers> cbSupplier;

    private Stage stage;
    private Warehouse warehouse;
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final SuppliersDAO suppliersDAO = new SuppliersDAO();
    private ObservableList<Suppliers> suppliers = FXCollections.observableArrayList();

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    @FXML
    void initialize() {
        dpDate.setValue(LocalDate.now());
        loadSuppliers();
    }

    private void loadSuppliers() {
        suppliers.setAll(suppliersDAO.findAll());
        cbSupplier.setItems(suppliers);
        cbSupplier.setCellFactory(lv -> new ListCell<Suppliers>() {
            @Override
            protected void updateItem(Suppliers item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNameSupplier());
            }
        });
        cbSupplier.setButtonCell(new ListCell<Suppliers>() {
            @Override
            protected void updateItem(Suppliers item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNameSupplier());
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public void onSave(ActionEvent actionEvent) {
        String countText = tfCount.getText();
        String priceText = tfPrice.getText();
        LocalDate date = dpDate.getValue();

        if (countText == null || countText.trim().isEmpty()) {
            showAlert(resources.getString("invoice.alert.error.enter_quantity"));
            return;
        }

        if (priceText == null || priceText.trim().isEmpty()) {
            showAlert(resources.getString("invoice.alert.error.enter_price"));
            return;
        }

        if (cbSupplier.getValue() == null) {
            showAlert(resources.getString("invoice.alert.error.select_supplier"));
            return;
        }

        if (date == null) {
            showAlert(resources.getString("invoice.alert.error.select_date"));
            return;
        }

        if (date.isAfter(LocalDate.now())) {
            showAlert(java.text.MessageFormat.format(
                    resources.getString("invoice.alert.error.date_not_future"),
                    date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
            return;
        }

        try {
            int quantity = Integer.parseInt(countText);
            if (quantity <= 0) {
                showAlert(resources.getString("invoice.alert.error.quantity_positive"));
                return;
            }

            BigDecimal priceValue = new BigDecimal(priceText);
            if (priceValue.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(resources.getString("invoice.alert.error.price_positive"));
                return;
            }

            Invoice invoice = new Invoice();
            invoice.setQuantity(quantity);
            invoice.setWarehouse(warehouse);
            invoice.setSupplier(cbSupplier.getValue());
            invoice.setDateInvoice(date);

            Price price = new Price();
            price.setPrice(priceValue);
            invoice.setPrice(price);

            invoiceDAO.save(invoice);

            showAlert(resources.getString("invoice.alert.success"));
            stage.close();
        } catch (NumberFormatException e) {
            showAlert(resources.getString("invoice.alert.error.invalid_number"));
        }
    }

    public void onExit(ActionEvent actionEvent) {
        stage.close();
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(resources.getString("alert.title.information"));
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}