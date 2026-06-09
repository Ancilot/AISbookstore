package ru.an.bookstore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class RefundController {

    private static final Logger log = LoggerFactory.getLogger(RefundController.class);

    @FXML private ResourceBundle resources;
    @FXML private TableView<CompositionCheck> tvRefundItems;
    @FXML private TableColumn<CompositionCheck, String> colBookName;
    @FXML private TableColumn<CompositionCheck, String> colDateTime;
    @FXML private TableColumn<CompositionCheck, Integer> colQuantity;
    @FXML private TableColumn<CompositionCheck, String> colPrice;
    @FXML private TableColumn<CompositionCheck, String> colStatus;
    @FXML private TextField tfSearch;
    @FXML private TextField tfReturnQuantity;

    private Stage stage;
    private Clients client;
    private List<CompositionCheck> allCheckItems;
    private final CompositionCheckDAO compositionCheckDAO = new CompositionCheckDAO();

    private ObservableList<CompositionCheck> refundItems = FXCollections.observableArrayList();

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    @FXML
    void initialize() {
        log.debug("Инициализация RefundController");

        colBookName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getBook().getNameBook()));
        colDateTime.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getChecks().getDateTime().toString()));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getPriceTime().toString()));
        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getChecks().getStatus()));

        tvRefundItems.setItems(refundItems);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setClient(Clients client) {
        this.client = client;
        log.info("Открытие возвратов для клиента id={}, name={} {}",
                client.getIdClient(), client.getSurname(), client.getNameClient());
        loadClientChecks();
    }

    private void loadClientChecks() {
        allCheckItems = compositionCheckDAO.findByClient(client.getIdClient());
        refundItems.setAll(allCheckItems);
        log.debug("Загружено {} позиций для возврата для клиента id={}",
                allCheckItems.size(), client.getIdClient());
    }

    @FXML
    public void onFind(ActionEvent actionEvent) {
        String searchText = tfSearch.getText();
        log.debug("Поиск книг для возврата: searchText='{}', clientId={}", searchText, client.getIdClient());

        if (searchText == null || searchText.trim().isEmpty()) {
            refundItems.setAll(allCheckItems);
        } else {
            ObservableList<CompositionCheck> filtered = refundItems.filtered(item ->
                    item.getBook().getNameBook().toLowerCase().contains(searchText.toLowerCase()));
            refundItems.setAll(filtered);
            log.debug("Найдено {} позиций по запросу '{}'", filtered.size(), searchText);
        }
    }

    @FXML
    public void onRefund(ActionEvent actionEvent) {
        CompositionCheck selected = tvRefundItems.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.warn("Попытка возврата без выбора позиции, clientId={}", client.getIdClient());
            showAlert(resources.getString("refund.alert.error.select_book"));
            return;
        }

        String returnQtyText = tfReturnQuantity.getText();
        if (returnQtyText == null || returnQtyText.trim().isEmpty()) {
            log.warn("Попытка возврата без указания количества, clientId={}, bookId={}",
                    client.getIdClient(), selected.getBook().getIdBook());
            showAlert(resources.getString("refund.alert.error.enter_quantity"));
            return;
        }

        int returnQuantity;
        try {
            returnQuantity = Integer.parseInt(returnQtyText);
        } catch (NumberFormatException e) {
            log.warn("Ошибка парсинга количества возврата: value='{}'", returnQtyText);
            showAlert(resources.getString("refund.alert.error.invalid_quantity"));
            return;
        }

        if (returnQuantity <= 0) {
            log.warn("Попытка возврата с неположительным количеством: {}", returnQuantity);
            showAlert(resources.getString("refund.alert.error.quantity_positive"));
            return;
        }

        if (returnQuantity > selected.getQuantity()) {
            log.warn("Попытка возврата большего количества: requested={}, available={}",
                    returnQuantity, selected.getQuantity());
            showAlert(java.text.MessageFormat.format(
                    resources.getString("refund.alert.error.too_many"),
                    selected.getQuantity()));
            return;
        }

        log.info("Запрос на возврат товара: clientId={}, compositionId={}, bookName='{}', quantity={}, price={}",
                client.getIdClient(), selected.getIdComposition(), selected.getBook().getNameBook(),
                returnQuantity, selected.getPriceTime());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(resources.getString("refund.alert.confirm.title"));
        confirm.setHeaderText(null);
        confirm.setContentText(java.text.MessageFormat.format(
                resources.getString("refund.alert.confirm.text"),
                returnQuantity,
                selected.getBook().getNameBook(),
                selected.getPriceTime()));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                compositionCheckDAO.returnFromCheck(selected.getIdComposition(), returnQuantity);
                log.info("Возврат успешно выполнен: compositionId={}, quantity={}",
                        selected.getIdComposition(), returnQuantity);
                showAlert(resources.getString("refund.alert.success"));

                loadClientChecks();
                tfReturnQuantity.clear();
            } catch (RuntimeException e) {
                log.error("Ошибка выполнения возврата: compositionId={}, quantity={}",
                        selected.getIdComposition(), returnQuantity, e);
                showAlert(resources.getString("refund.alert.error.general"));
            }
        } else {
            log.debug("Возврат товара отменён пользователем");
        }
    }

    @FXML
    public void onBack(ActionEvent actionEvent) {
        log.debug("Закрытие окна возвратов для клиента id={}", client.getIdClient());
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