package ru.an.bookstore;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MakingController {

    private static final Logger log = LoggerFactory.getLogger(MakingController.class);

    @FXML private ResourceBundle resources;
    @FXML private TableView<BookCatalog> tvAvailableBooks;
    @FXML private TableColumn<BookCatalog, String> colAvailableName;
    @FXML private TableColumn<BookCatalog, String> colAvailableAuthor;
    @FXML private TableColumn<BookCatalog, String> colAvailableGenre;
    @FXML private TableColumn<BookCatalog, String> colAvailablePrice;
    @FXML private TableColumn<BookCatalog, Integer> colAvailableQuantity;
    @FXML private TableColumn<BookCatalog, String> colAvailableStatus;
    @FXML private TextField tfAvailableSearch;
    @FXML private TextField tfAvailableCount;

    @FXML private TableView<CompositionCheck> tvCheckItems;
    @FXML private TableColumn<CompositionCheck, String> colCheckName;
    @FXML private TableColumn<CompositionCheck, String> colCheckAuthor;
    @FXML private TableColumn<CompositionCheck, String> colCheckGenre;
    @FXML private TableColumn<CompositionCheck, String> colCheckPrice;
    @FXML private TableColumn<CompositionCheck, Integer> colCheckQuantity;
    @FXML private TextField tfCheckSearch;

    private Stage stage;
    private Clients client;
    private Checks currentCheck;
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final ChecksDAO checksDAO = new ChecksDAO();
    private final CompositionCheckDAO compositionCheckDAO = new CompositionCheckDAO();
    private final OrdersDAO ordersDAO = new OrdersDAO();

    private ObservableList<BookCatalog> availableBooks = FXCollections.observableArrayList();
    private ObservableList<CompositionCheck> checkItems = FXCollections.observableArrayList();
    private List<BookCatalog> allAvailableBooks;

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    @FXML
    void initialize() {
        log.debug("Инициализация MakingController");

        colAvailableName.setCellValueFactory(new PropertyValueFactory<>("nameBook"));
        colAvailableAuthor.setCellValueFactory(new PropertyValueFactory<>("authors"));
        colAvailableGenre.setCellValueFactory(new PropertyValueFactory<>("genres"));
        colAvailablePrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAvailableQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colAvailableStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tvAvailableBooks.setItems(availableBooks);

        colCheckName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBook().getNameBook()));
        colCheckAuthor.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBook().getAuthors()));
        colCheckGenre.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBook().getGenres()));
        colCheckPrice.setCellValueFactory(new PropertyValueFactory<>("priceTime"));
        colCheckQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tvCheckItems.setItems(checkItems);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setClient(Clients client) {
        this.client = client;
        log.info("Оформление покупки для клиента id={}, name={} {}",
                client.getIdClient(), client.getSurname(), client.getNameClient());
        loadAvailableBooks();
        createNewCheck();
    }

    private void loadAvailableBooks() {
        allAvailableBooks = warehouseDAO.findBooksForSale(null);
        availableBooks.setAll(allAvailableBooks);
        log.debug("Загружено {} книг для продажи", allAvailableBooks.size());
    }

    private void createNewCheck() {
        log.debug("Создание нового чека для клиента id={}", client.getIdClient());
        currentCheck = checksDAO.createCheck(client.getIdClient());
        if (currentCheck != null) {
            log.info("Создан чек id={} для клиента id={}", currentCheck.getIdCheck(), client.getIdClient());
            refreshCheckItems();
        } else {
            log.error("Не удалось создать чек для клиента id={}", client.getIdClient());
            showAlert(resources.getString("making.alert.error.check_not_created"));
        }
    }

    @FXML
    public void onFindAvailable(ActionEvent actionEvent) {
        String searchText = tfAvailableSearch.getText();
        log.debug("Поиск доступных книг: searchText='{}'", searchText);

        if (searchText == null || searchText.trim().isEmpty()) {
            refreshAvailableBooks();
        } else {
            List<BookCatalog> results = warehouseDAO.findBooksForSale(searchText);
            availableBooks.setAll(results);
            log.debug("Найдено {} книг по запросу '{}'", results.size(), searchText);
        }
    }

    @FXML
    public void onAddToCheck(ActionEvent actionEvent) {
        if (currentCheck == null) {
            log.warn("Попытка добавления книги в чек, но чек не создан");
            showAlert(resources.getString("making.alert.error.check_not_created"));
            return;
        }

        BookCatalog selected = tvAvailableBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.warn("Попытка добавления книги в чек без выбора книги");
            showAlert(resources.getString("making.alert.error.select_book"));
            return;
        }

        String countText = tfAvailableCount.getText();
        if (countText == null || countText.trim().isEmpty()) {
            log.warn("Попытка добавления книги в чек без указания количества");
            showAlert(resources.getString("making.alert.error.enter_quantity"));
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(countText);
        } catch (NumberFormatException e) {
            log.warn("Ошибка парсинга количества: value='{}'", countText);
            showAlert(resources.getString("making.alert.error.invalid_quantity"));
            return;
        }

        if (quantity <= 0) {
            log.warn("Попытка добавления книги с неположительным количеством: {}", quantity);
            showAlert(resources.getString("making.alert.error.quantity_positive"));
            return;
        }
        if (quantity > selected.getQuantity()) {
            log.warn("Недостаточно книг на складе: requested={}, available={}", quantity, selected.getQuantity());
            showAlert(java.text.MessageFormat.format(
                    resources.getString("making.alert.error.insufficient_stock"),
                    selected.getQuantity()));
            return;
        }

        log.info("Добавление книги в чек: checkId={}, bookId={}, bookName='{}', quantity={}",
                currentCheck.getIdCheck(), selected.getIdBook(), selected.getNameBook(), quantity);

        CompositionCheck composition = new CompositionCheck();
        composition.setChecks(currentCheck);
        composition.setBook(selected);
        composition.setQuantity(quantity);
        compositionCheckDAO.save(composition);

        refreshAvailableBooks();
        refreshCheckItems();
        tfAvailableCount.clear();

        log.debug("Книга успешно добавлена в чек, обновлены таблицы");
    }

    @FXML
    public void onFindCheck(ActionEvent actionEvent) {
        if (currentCheck == null) {
            refreshCheckItems();
            return;
        }
        String searchText = tfCheckSearch.getText();
        log.debug("Поиск в чеке: searchText='{}'", searchText);

        if (searchText == null || searchText.trim().isEmpty()) {
            refreshCheckItems();
        } else {
            List<CompositionCheck> items = compositionCheckDAO.searchCheckDetails(currentCheck.getIdCheck(), searchText);
            checkItems.setAll(items);
            log.debug("Найдено {} позиций в чеке по запросу '{}'", items.size(), searchText);
        }
    }

    @FXML
    public void onRemoveFromCheck(ActionEvent actionEvent) {
        if (currentCheck == null) {
            log.warn("Попытка удаления из чека, но чек не найден");
            showAlert(resources.getString("making.alert.error.check_not_found"));
            return;
        }

        CompositionCheck selected = tvCheckItems.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.warn("Попытка удаления из чека без выбора позиции");
            showAlert(resources.getString("making.alert.error.select_for_remove"));
            return;
        }

        log.info("Удаление позиции из чека: checkId={}, bookId={}, bookName='{}'",
                currentCheck.getIdCheck(),
                selected.getBook().getIdBook(),
                selected.getBook().getNameBook());

        if (selected.getIdComposition() != null) {
            compositionCheckDAO.deleteById(selected.getIdComposition());
        } else {
            compositionCheckDAO.deleteByCheckAndBook(currentCheck.getIdCheck(), selected.getBook().getIdBook());
        }

        refreshAvailableBooks();
        refreshCheckItems();
        log.debug("Позиция удалена из чека, таблицы обновлены");
    }

    @FXML
    public void onPurchase(ActionEvent actionEvent) {
        if (currentCheck == null) {
            log.warn("Попытка завершения покупки, но чек не найден");
            showAlert(resources.getString("making.alert.error.check_not_found"));
            return;
        }
        if (checkItems.isEmpty()) {
            log.warn("Попытка завершения пустого чека id={}", currentCheck.getIdCheck());
            showAlert(resources.getString("making.alert.error.empty_check"));
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(resources.getString("making.alert.confirm.purchase_title"));
        confirm.setHeaderText(null);
        confirm.setContentText(resources.getString("making.alert.confirm.purchase_text"));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            log.info("Завершение покупки по чеку id={}, сумма={} позиций",
                    currentCheck.getIdCheck(), checkItems.size());
            checksDAO.completeCheck(currentCheck.getIdCheck());
            log.info("Чек id={} успешно завершён", currentCheck.getIdCheck());
            showAlert(resources.getString("making.alert.success.purchase"));
            stage.close();
        } else {
            log.debug("Покупка отменена пользователем");
        }
    }

    @FXML
    public void onMakeOrderDirect(ActionEvent actionEvent) {
        BookCatalog selected = tvAvailableBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.warn("Попытка создания прямого заказа без выбора книги");
            showAlert(resources.getString("making.alert.error.select_book"));
            return;
        }

        log.debug("Создание прямого заказа для клиента id={}, книга id={}",
                client.getIdClient(), selected.getIdBook());
        showCreateOrderDialog(selected);
    }

    private void showCreateOrderDialog(BookCatalog selectedBook) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MakingController.class.getResource("creating-an-order.fxml"), resources);
            Scene scene = new Scene(loader.load(), 267, 285);

            CreatOrderController controller = loader.getController();
            Stage orderStage = new Stage();

            controller.setStage(orderStage);
            controller.setClient(client);
            controller.setBook(selectedBook);
            controller.setResources(resources);

            orderStage.initModality(Modality.WINDOW_MODAL);
            orderStage.setTitle(resources.getString("app.title.create_order"));
            orderStage.setScene(scene);
            orderStage.showAndWait();

            log.debug("Диалог создания заказа закрыт");

        } catch (IOException e) {
            log.error("Ошибка загрузки FXML creating-an-order.fxml", e);
            e.printStackTrace();
            showAlert(resources.getString("making.alert.error.order_form"));
        }
    }

    @FXML
    public void onBack(ActionEvent actionEvent) {
        if (checkItems.isEmpty() && currentCheck != null) {
            log.info("Отмена оформления покупки, удаление пустого чека id={}", currentCheck.getIdCheck());
            compositionCheckDAO.deleteByCheck(currentCheck.getIdCheck());
            checksDAO.deleteCheck(currentCheck.getIdCheck());
        }
        log.debug("Закрытие окна оформления покупки");
        stage.close();
    }

    private void refreshAvailableBooks() {
        String searchText = tfAvailableSearch.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            allAvailableBooks = warehouseDAO.findBooksForSale(null);
            availableBooks.setAll(allAvailableBooks);
        } else {
            List<BookCatalog> results = warehouseDAO.findBooksForSale(searchText);
            availableBooks.setAll(results);
        }
    }

    private void refreshCheckItems() {
        if (currentCheck != null) {
            List<CompositionCheck> items = compositionCheckDAO.findByCheck(currentCheck.getIdCheck());
            checkItems.setAll(items);
            log.debug("В чеке id={} находится {} позиций", currentCheck.getIdCheck(), items.size());
        } else {
            checkItems.clear();
        }
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(resources.getString("alert.title.information"));
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}