package ru.an.bookstore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class NewBookController {

    private static final Logger log = LoggerFactory.getLogger(NewBookController.class);

    @FXML
    private ResourceBundle resources;

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    private BookCatalog book;
    private Stage stage;
    private final BookCatalogDAO bookCatalogDAO = new BookCatalogDAO();
    private final AuthorsDAO authorsDAO = new AuthorsDAO();
    private final GenresDAO genresDAO = new GenresDAO();

    // Основные поля
    @FXML private TextField tfNameBook;
    @FXML private ComboBox<PublishingHouses> cbPublisher;
    @FXML private TextField tfYearPublication;
    @FXML private TextField tfIsbn;
    @FXML private TextField tfNumberPages;
    @FXML private TextField tfPrice;
    @FXML private TextField tfAnnotation;
    @FXML private ImageView ivBookImage;
    @FXML private Button btnSelectImage;

    // Таблицы для авторов
    @FXML private TableView<Authors> tvAvailableAuthors;
    @FXML private TableColumn<Authors, String> colAvailableSurname;
    @FXML private TableColumn<Authors, String> colAvailableName;
    @FXML private TableColumn<Authors, String> colAvailablePatronymic;
    @FXML private TextField tfAuthorSearch;
    @FXML private Button btnAuthorSearch;
    @FXML private Button btnAddAuthor;

    @FXML private TableView<Authors> tvBookAuthors;
    @FXML private TableColumn<Authors, String> colBookSurname;
    @FXML private TableColumn<Authors, String> colBookName;
    @FXML private TableColumn<Authors, String> colBookPatronymic;
    @FXML private TextField tfBookAuthorSearch;
    @FXML private Button btnBookAuthorSearch;
    @FXML private Button btnRemoveAuthor;

    // Таблицы для жанров
    @FXML private TableView<Genres> tvAvailableGenres;
    @FXML private TableColumn<Genres, String> colAvailableGenre;
    @FXML private TextField tfGenreSearch;
    @FXML private Button btnGenreSearch;
    @FXML private Button btnAddGenre;

    @FXML private TableView<Genres> tvBookGenres;
    @FXML private TableColumn<Genres, String> colBookGenre;
    @FXML private TextField tfBookGenreSearch;
    @FXML private Button btnBookGenreSearch;
    @FXML private Button btnRemoveGenre;

    private String selectedImagePath;
    private ObservableList<Authors> availableAuthors = FXCollections.observableArrayList();
    private ObservableList<Authors> bookAuthors = FXCollections.observableArrayList();
    private ObservableList<Genres> availableGenres = FXCollections.observableArrayList();
    private ObservableList<Genres> bookGenres = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        log.debug("Инициализация NewBookController");

        // Инициализация таблиц авторов
        colAvailableSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colAvailableName.setCellValueFactory(new PropertyValueFactory<>("nameAuthor"));
        colAvailablePatronymic.setCellValueFactory(new PropertyValueFactory<>("patronymic"));
        tvAvailableAuthors.setItems(availableAuthors);

        colBookSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colBookName.setCellValueFactory(new PropertyValueFactory<>("nameAuthor"));
        colBookPatronymic.setCellValueFactory(new PropertyValueFactory<>("patronymic"));
        tvBookAuthors.setItems(bookAuthors);

        // Инициализация таблиц жанров
        colAvailableGenre.setCellValueFactory(new PropertyValueFactory<>("genr"));
        tvAvailableGenres.setItems(availableGenres);

        colBookGenre.setCellValueFactory(new PropertyValueFactory<>("genr"));
        tvBookGenres.setItems(bookGenres);

        // Загрузка издательств
        loadPublishers();

        // Настройка поиска авторов
        btnAuthorSearch.setOnAction(e -> searchAuthors());
        tfAuthorSearch.setOnAction(e -> searchAuthors());
        btnBookAuthorSearch.setOnAction(e -> searchBookAuthors());
        tfBookAuthorSearch.setOnAction(e -> searchBookAuthors());

        // Настройка поиска жанров
        btnGenreSearch.setOnAction(e -> searchGenres());
        tfGenreSearch.setOnAction(e -> searchGenres());
        btnBookGenreSearch.setOnAction(e -> searchBookGenres());
        tfBookGenreSearch.setOnAction(e -> searchBookGenres());

        // Добавление/удаление авторов
        btnAddAuthor.setOnAction(e -> addAuthorToBook());
        btnRemoveAuthor.setOnAction(e -> removeAuthorFromBook());

        // Добавление/удаление жанров
        btnAddGenre.setOnAction(e -> addGenreToBook());
        btnRemoveGenre.setOnAction(e -> removeGenreFromBook());

        // Выбор изображения
        btnSelectImage.setOnAction(e -> selectImage());
    }

    private void loadPublishers() {
        PublishingHousesDAO publisherDAO = new PublishingHousesDAO();
        ObservableList<PublishingHouses> publishers = FXCollections.observableArrayList();
        publishers.setAll((Collection<PublishingHouses>) publisherDAO.findAll());
        cbPublisher.setItems(publishers);
        log.debug("Загружено {} издательств", publishers.size());
    }

    private void searchAuthors() {
        String searchText = tfAuthorSearch.getText();
        log.debug("Поиск доступных авторов: searchText='{}'", searchText);

        if (searchText == null || searchText.trim().isEmpty()) {
            loadAvailableAuthors();
        } else {
            List<Authors> results = bookCatalogDAO.searchAuthors(searchText);
            List<Long> bookAuthorIds = bookAuthors.stream()
                    .map(Authors::getIdAuthors)
                    .collect(Collectors.toList());
            availableAuthors.setAll(results.stream()
                    .filter(a -> !bookAuthorIds.contains(a.getIdAuthors()))
                    .collect(Collectors.toList()));
            log.debug("Найдено {} авторов по запросу '{}'", availableAuthors.size(), searchText);
        }
    }

    private void searchBookAuthors() {
        String searchText = tfBookAuthorSearch.getText();
        log.debug("Поиск авторов книги: searchText='{}'", searchText);

        if (searchText == null || searchText.trim().isEmpty()) {
            tvBookAuthors.setItems(bookAuthors);
        } else {
            ObservableList<Authors> filtered = bookAuthors.filtered(author ->
                    (author.getSurname() + " " + author.getNameAuthor() + " " +
                            (author.getPatronymic() != null ? author.getPatronymic() : ""))
                            .toLowerCase().contains(searchText.toLowerCase()));
            tvBookAuthors.setItems(filtered);
            log.debug("Найдено {} авторов в книге по запросу '{}'", filtered.size(), searchText);
        }
    }

    private void loadAvailableAuthors() {
        List<Authors> allAuthors = (List<Authors>) authorsDAO.findAll();
        List<Long> bookAuthorIds = bookAuthors.stream()
                .map(Authors::getIdAuthors)
                .collect(Collectors.toList());
        availableAuthors.setAll(allAuthors.stream()
                .filter(a -> !bookAuthorIds.contains(a.getIdAuthors()))
                .collect(Collectors.toList()));
        log.debug("Загружено {} доступных авторов", availableAuthors.size());
    }

    private void addAuthorToBook() {
        Authors selected = tvAvailableAuthors.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Добавление автора в книгу: authorId={}, name={} {}",
                    selected.getIdAuthors(), selected.getSurname(), selected.getNameAuthor());
            bookAuthors.add(selected);
            availableAuthors.remove(selected);
            bookAuthors.sort((a1, a2) -> a1.getSurname().compareTo(a2.getSurname()));
        } else {
            log.warn("Попытка добавления автора без выбора");
        }
    }

    private void removeAuthorFromBook() {
        Authors selected = tvBookAuthors.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Удаление автора из книги: authorId={}, name={} {}",
                    selected.getIdAuthors(), selected.getSurname(), selected.getNameAuthor());
            bookAuthors.remove(selected);
            Authors freshAuthor = authorsDAO.findById(selected.getIdAuthors());
            if (freshAuthor != null) {
                availableAuthors.add(freshAuthor);
                availableAuthors.sort((a1, a2) -> a1.getSurname().compareTo(a2.getSurname()));
            }
        } else {
            log.warn("Попытка удаления автора без выбора");
        }
    }

    private void searchGenres() {
        String searchText = tfGenreSearch.getText();
        log.debug("Поиск доступных жанров: searchText='{}'", searchText);

        if (searchText == null || searchText.trim().isEmpty()) {
            loadAvailableGenres();
        } else {
            List<Genres> results = bookCatalogDAO.searchGenres(searchText);
            List<Long> bookGenreIds = bookGenres.stream()
                    .map(Genres::getIdGenr)
                    .collect(Collectors.toList());
            availableGenres.setAll(results.stream()
                    .filter(g -> !bookGenreIds.contains(g.getIdGenr()))
                    .collect(Collectors.toList()));
            log.debug("Найдено {} жанров по запросу '{}'", availableGenres.size(), searchText);
        }
    }

    private void searchBookGenres() {
        String searchText = tfBookGenreSearch.getText();
        log.debug("Поиск жанров книги: searchText='{}'", searchText);

        if (searchText == null || searchText.trim().isEmpty()) {
            tvBookGenres.setItems(bookGenres);
        } else {
            ObservableList<Genres> filtered = bookGenres.filtered(genre ->
                    genre.getGenr().toLowerCase().contains(searchText.toLowerCase()));
            tvBookGenres.setItems(filtered);
            log.debug("Найдено {} жанров в книге по запросу '{}'", filtered.size(), searchText);
        }
    }

    private void loadAvailableGenres() {
        List<Genres> allGenres = (List<Genres>) genresDAO.findAll();
        List<Long> bookGenreIds = bookGenres.stream()
                .map(Genres::getIdGenr)
                .collect(Collectors.toList());
        availableGenres.setAll(allGenres.stream()
                .filter(g -> !bookGenreIds.contains(g.getIdGenr()))
                .collect(Collectors.toList()));
        log.debug("Загружено {} доступных жанров", availableGenres.size());
    }

    private void addGenreToBook() {
        Genres selected = tvAvailableGenres.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Добавление жанра в книгу: genreId={}, name='{}'",
                    selected.getIdGenr(), selected.getGenr());
            bookGenres.add(selected);
            availableGenres.remove(selected);
            bookGenres.sort((g1, g2) -> g1.getGenr().compareTo(g2.getGenr()));
        } else {
            log.warn("Попытка добавления жанра без выбора");
        }
    }

    private void removeGenreFromBook() {
        Genres selected = tvBookGenres.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Удаление жанра из книги: genreId={}, name='{}'",
                    selected.getIdGenr(), selected.getGenr());
            bookGenres.remove(selected);
            Genres freshGenre = genresDAO.findById(selected.getIdGenr());
            if (freshGenre != null) {
                availableGenres.add(freshGenre);
                availableGenres.sort((g1, g2) -> g1.getGenr().compareTo(g2.getGenr()));
            }
        } else {
            log.warn("Попытка удаления жанра без выбора");
        }
    }

    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resources.getString("book.button.select_image"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            Image image = new Image(selectedFile.toURI().toString());
            ivBookImage.setImage(image);
            log.debug("Выбрано изображение: {}", selectedImagePath);
        }
    }

    private boolean validateFields() {
        if (tfNameBook.getText() == null || tfNameBook.getText().trim().isEmpty()) {
            log.warn("Ошибка валидации: пустое название книги");
            showAlert(resources.getString("book.alert.error.empty_title"));
            return false;
        }
        if (cbPublisher.getValue() == null) {
            log.warn("Ошибка валидации: не выбрано издательство");
            showAlert(resources.getString("book.alert.error.select_publisher"));
            return false;
        }
        try {
            int year = Integer.parseInt(tfYearPublication.getText());
            int currentYear = Year.now().getValue();
            if (year < 1450 || year > currentYear) {
                log.warn("Ошибка валидации: неверный год {} (диапазон 1450-{})", year, currentYear);
                showAlert(java.text.MessageFormat.format(
                        resources.getString("book.alert.error.year_range"),
                        currentYear));
                return false;
            }
        } catch (NumberFormatException e) {
            log.warn("Ошибка валидации: неверный формат года '{}'", tfYearPublication.getText());
            showAlert(resources.getString("book.alert.error.invalid_year"));
            return false;
        }
        if (tfIsbn.getText() == null || tfIsbn.getText().trim().isEmpty()) {
            log.warn("Ошибка валидации: пустой ISBN");
            showAlert(resources.getString("book.alert.error.empty_isbn"));
            return false;
        }

        if (!tfIsbn.getText().matches("\\d{13}")) {
            log.warn("Ошибка валидации: неверный формат ISBN '{}'", tfIsbn.getText());
            showAlert(resources.getString("book.alert.error.isbn_format"));
            return false;
        }
        try {
            int pages = Integer.parseInt(tfNumberPages.getText());
            if (pages <= 0 || pages > 1500) {
                log.warn("Ошибка валидации: неверное количество страниц {}", pages);
                showAlert(resources.getString("book.alert.error.pages_range"));
                return false;
            }
        } catch (NumberFormatException e) {
            log.warn("Ошибка валидации: неверный формат страниц '{}'", tfNumberPages.getText());
            showAlert(resources.getString("book.alert.error.invalid_pages"));
            return false;
        }
        try {
            BigDecimal price = new BigDecimal(tfPrice.getText());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Ошибка валидации: отрицательная цена {}", price);
                showAlert(resources.getString("book.alert.error.negative_price"));
                return false;
            }
        } catch (NumberFormatException e) {
            log.warn("Ошибка валидации: неверный формат цены '{}'", tfPrice.getText());
            showAlert(resources.getString("book.alert.error.invalid_price"));
            return false;
        }
        return true;
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(resources.getString("alert.title.error"));
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccessAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(resources.getString("alert.title.information"));
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void fillFormFromBook() {
        if (book == null) return;

        log.debug("Заполнение формы данными книги id={}, name='{}'", book.getIdBook(), book.getNameBook());

        tfNameBook.setText(book.getNameBook());
        cbPublisher.setValue(book.getPublishingHouses());
        if (book.getYearPublication() != null) {
            tfYearPublication.setText(String.valueOf(book.getYearPublication().getYear()));
        }
        tfIsbn.setText(book.getIsbn());
        tfNumberPages.setText(String.valueOf(book.getNumberPages()));
        if (book.getPrice() != null) {
            tfPrice.setText(book.getPrice().toString());
        }
        tfAnnotation.setText(book.getAnnotation());

        if (book.getAuthorsBook() != null) {
            bookAuthors.setAll(book.getAuthorsBook().stream()
                    .map(AuthorsBook::getAuthor)
                    .collect(Collectors.toList()));
            log.debug("Загружено {} авторов для книги", bookAuthors.size());
        }

        if (book.getGenresBooks() != null) {
            bookGenres.setAll(book.getGenresBooks().stream()
                    .map(GenresBook::getGenr)
                    .collect(Collectors.toList()));
            log.debug("Загружено {} жанров для книги", bookGenres.size());
        }

        if (book.getImages() != null && !book.getImages().isEmpty()) {
            String imagePath = book.getImages().get(0).getImagePath();
            if (imagePath != null && new File(imagePath).exists()) {
                selectedImagePath = imagePath;
                Image image = new Image(new File(imagePath).toURI().toString());
                ivBookImage.setImage(image);
                log.debug("Загружено изображение: {}", imagePath);
            }
        }
    }

    private void fillBookFromForm() {
        if (book == null) {
            book = new BookCatalog();
        }

        book.setNameBook(tfNameBook.getText().trim());
        book.setPublishingHouses(cbPublisher.getValue());
        book.setYearPublication(LocalDate.of(Integer.parseInt(tfYearPublication.getText()), 1, 1));
        book.setIsbn(tfIsbn.getText().trim());
        book.setNumberPages(Integer.parseInt(tfNumberPages.getText()));
        book.setPrice(new BigDecimal(tfPrice.getText()));
        book.setAnnotation(tfAnnotation.getText());

        List<AuthorsBook> authorsBookList = new ArrayList<>();
        for (Authors author : bookAuthors) {
            AuthorsBook ab = new AuthorsBook();
            ab.setAuthor(author);
            ab.setBook(book);
            authorsBookList.add(ab);
        }
        book.setAuthorsBook(authorsBookList);

        List<GenresBook> genresBookList = new ArrayList<>();
        for (Genres genre : bookGenres) {
            GenresBook gb = new GenresBook();
            gb.setGenr(genre);
            gb.setBook(book);
            genresBookList.add(gb);
        }
        book.setGenresBooks(genresBookList);

        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            List<Images> imagesList = new ArrayList<>();
            Images image = new Images();
            image.setImagePath(selectedImagePath);
            imagesList.add(image);
            book.setImages(imagesList);
        }
    }

    @FXML
    public void onSave(ActionEvent actionEvent) {
        log.debug("Сохранение книги");

        if (!validateFields()) {
            return;
        }

        fillBookFromForm();

        if (book.getIdBook() == null) {
            log.info("Создание новой книги: name='{}', isbn='{}'", book.getNameBook(), book.getIsbn());
            BookCatalog saved = bookCatalogDAO.save(book);
            if (saved != null && saved.getIdBook() != null) {
                saved.setAuthorsBook(book.getAuthorsBook());
                saved.setGenresBooks(book.getGenresBooks());
                saved.setImages(book.getImages());
                bookCatalogDAO.update(saved);
                log.info("Книга успешно создана с id={}", saved.getIdBook());
                showSuccessAlert(resources.getString("book.alert.success.add"));
            } else {
                log.error("Ошибка создания книги");
                showAlert(resources.getString("book.alert.error.save"));
            }
        } else {
            log.info("Обновление книги id={}, name='{}'", book.getIdBook(), book.getNameBook());
            BookCatalog updated = bookCatalogDAO.update(book);
            if (updated != null) {
                log.info("Книга id={} успешно обновлена", book.getIdBook());
                showSuccessAlert(resources.getString("book.alert.success.update"));
            } else {
                log.error("Ошибка обновления книги id={}", book.getIdBook());
                showAlert(resources.getString("book.alert.error.update"));
            }
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setBook(BookCatalog book) {
        this.book = book;
        if (book != null && book.getIdBook() != null) {
            log.debug("Загрузка полных данных книги id={}", book.getIdBook());
            BookCatalog fullBook = bookCatalogDAO.findById(book.getIdBook());
            if (fullBook != null) {
                this.book = fullBook;
                fillFormFromBook();
            }
        }
        loadAvailableAuthors();
        loadAvailableGenres();
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
        log.debug("Закрытие окна редактирования книги");
        stage.close();
    }
}