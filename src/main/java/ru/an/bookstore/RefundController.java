package ru.an.bookstore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class RefundController {

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

    @FXML
    void initialize() {
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
        loadClientChecks();
    }

    private void loadClientChecks() {
        // Загружаем все чеки клиента и их детали
        // Для простоты - загружаем все composition_check для чеков клиента
        // Нужно получить все checks клиента, затем все composition_check
        // Здесь используем метод, который получит все товары из чеков клиента
        allCheckItems = compositionCheckDAO.findByClient(client.getIdClient());
        refundItems.setAll(allCheckItems);
    }

    @FXML
    public void onFind(ActionEvent actionEvent) {
        String searchText = tfSearch.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            refundItems.setAll(allCheckItems);
        } else {
            // Ищем среди загруженных товаров
            ObservableList<CompositionCheck> filtered = refundItems.filtered(item ->
                    item.getBook().getNameBook().toLowerCase().contains(searchText.toLowerCase()));
            refundItems.setAll(filtered);
        }
    }

    @FXML
    public void onRefund(ActionEvent actionEvent) {
        CompositionCheck selected = tvRefundItems.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите товар для возврата");
            return;
        }

        String returnQtyText = tfReturnQuantity.getText();
        if (returnQtyText == null || returnQtyText.trim().isEmpty()) {
            showAlert("Ошибка", "Введите количество для возврата");
            return;
        }

        int returnQuantity;
        try {
            returnQuantity = Integer.parseInt(returnQtyText);
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректное количество");
            return;
        }

        if (returnQuantity <= 0) {
            showAlert("Ошибка", "Количество должно быть больше 0");
            return;
        }

        if (returnQuantity > selected.getQuantity()) {
            showAlert("Ошибка", "Нельзя вернуть больше, чем куплено. Доступно: " + selected.getQuantity());
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение возврата");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы уверены, что хотите вернуть " + returnQuantity + " шт.\n" +
                "Книга: " + selected.getBook().getNameBook() + "\n" +
                "Цена за шт.: " + selected.getPriceTime());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                compositionCheckDAO.returnFromCheck(selected.getIdComposition(), returnQuantity);
                showAlert("Успех", "Возврат оформлен успешно!");

                // Обновляем список
                loadClientChecks();
                tfReturnQuantity.clear();
            } catch (RuntimeException e) {
                showAlert("Ошибка", e.getMessage());
            }
        }
    }

    @FXML
    public void onBack(ActionEvent actionEvent) {
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