package ru.an.bookstore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class InvoiceController {

    private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);

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
        log.debug("Инициализация InvoiceController");
        dpDate.setValue(LocalDate.now());
        loadSuppliers();
    }

    private void loadSuppliers() {
        suppliers.setAll(suppliersDAO.findAll());
        cbSupplier.setItems(suppliers);
        log.debug("Загружено {} поставщиков", suppliers.size());

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
        log.debug("Установлен склад для накладной: warehouseId={}, bookId={}, bookName='{}'",
                warehouse.getIdWarehouse(),
                warehouse.getBook().getIdBook(),
                warehouse.getBook().getNameBook());
    }

    public void onSave(ActionEvent actionEvent) {
        String countText = tfCount.getText();
        String priceText = tfPrice.getText();
        LocalDate date = dpDate.getValue();

        log.debug("Сохранение накладной: quantity='{}', price='{}', date={}, supplier={}",
                countText, priceText, date, cbSupplier.getValue() != null ? cbSupplier.getValue().getNameSupplier() : "null");

        if (countText == null || countText.trim().isEmpty()) {
            log.warn("Ошибка валидации: не указано количество");
            showAlert(resources.getString("invoice.alert.error.enter_quantity"));
            return;
        }

        if (priceText == null || priceText.trim().isEmpty()) {
            log.warn("Ошибка валидации: не указана цена");
            showAlert(resources.getString("invoice.alert.error.enter_price"));
            return;
        }

        if (cbSupplier.getValue() == null) {
            log.warn("Ошибка валидации: не выбран поставщик");
            showAlert(resources.getString("invoice.alert.error.select_supplier"));
            return;
        }

        if (date == null) {
            log.warn("Ошибка валидации: не выбрана дата");
            showAlert(resources.getString("invoice.alert.error.select_date"));
            return;
        }

        if (date.isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации: дата {} в будущем", date);
            showAlert(java.text.MessageFormat.format(
                    resources.getString("invoice.alert.error.date_not_future"),
                    date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
            return;
        }

        try {
            int quantity = Integer.parseInt(countText);
            if (quantity <= 0) {
                log.warn("Ошибка валидации: количество <= 0, value={}", quantity);
                showAlert(resources.getString("invoice.alert.error.quantity_positive"));
                return;
            }

            BigDecimal priceValue = new BigDecimal(priceText);
            if (priceValue.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Ошибка валидации: цена <= 0, value={}", priceValue);
                showAlert(resources.getString("invoice.alert.error.price_positive"));
                return;
            }

            log.info("Создание накладной: bookId={}, bookName='{}', quantity={}, price={}, supplier={}, date={}",
                    warehouse.getBook().getIdBook(),
                    warehouse.getBook().getNameBook(),
                    quantity,
                    priceValue,
                    cbSupplier.getValue().getNameSupplier(),
                    date);

            Invoice invoice = new Invoice();
            invoice.setQuantity(quantity);
            invoice.setWarehouse(warehouse);
            invoice.setSupplier(cbSupplier.getValue());
            invoice.setDateInvoice(date);

            Price price = new Price();
            price.setPrice(priceValue);
            invoice.setPrice(price);

            invoiceDAO.save(invoice);

            log.info("Накладная успешно создана для книги id={}", warehouse.getBook().getIdBook());
            showAlert(resources.getString("invoice.alert.success"));
            stage.close();
        } catch (NumberFormatException e) {
            log.warn("Ошибка парсинга числа: quantity='{}', price='{}'", countText, priceText);
            showAlert(resources.getString("invoice.alert.error.invalid_number"));
        }
    }

    public void onExit(ActionEvent actionEvent) {
        log.debug("Закрытие окна создания накладной");
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