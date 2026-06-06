package ru.an.bookstore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceController {

    @FXML private TextField tfCount;
    @FXML private TextField tfPrice;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Suppliers> cbSupplier;

    private Stage stage;
    private Warehouse warehouse;
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final SuppliersDAO suppliersDAO = new SuppliersDAO();
    private ObservableList<Suppliers> suppliers = FXCollections.observableArrayList();

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
            showAlert("Ошибка", "Введите количество");
            return;
        }

        // Валидация цены
        if (priceText == null || priceText.trim().isEmpty()) {
            showAlert("Ошибка", "Введите цену");
            return;
        }

        // Валидация поставщика
        if (cbSupplier.getValue() == null) {
            showAlert("Ошибка", "Выберите поставщика");
            return;
        }

        // Валидация даты (здесь и должна быть!)
        if (date == null) {
            showAlert("Ошибка", "Выберите дату");
            return;
        }

        if (date.isAfter(LocalDate.now())) {
            showAlert("Ошибка",
                    "Дата накладной не может быть позже сегодняшней даты!\n" +
                            "Выбрана: " + date + "\n" +
                            "Сегодня: " + LocalDate.now());
            return;
        }

        try {
            int quantity = Integer.parseInt(countText);
            if (quantity <= 0) {
                showAlert("Ошибка", "Количество должно быть больше 0");
                return;
            }

            BigDecimal priceValue = new BigDecimal(priceText);
            if (priceValue.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert("Ошибка", "Цена должна быть больше 0");
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

            showAlert("Успех", "Накладная сохранена. Количество на складе обновлено автоматически.");
            stage.close();
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректные числовые значения");
        }
    }

    public void onExit(ActionEvent actionEvent) {
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}