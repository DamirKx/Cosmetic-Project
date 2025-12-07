package main.java.ui;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.data.JsonDataStorage;
import main.java.exceptions.EmptyFieldException;
import main.java.exceptions.InvalidFormatException;
import main.java.exceptions.NegativeValueException;
import main.java.exceptions.QuantityExceededException;
import main.java.model.CosmeticProduct;
import main.java.model.Sale;
import main.java.service.StoreService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JavaFX UI приложения магазина косметики.
 *
 * <p>Компоненты:
 * <ul>
 *     <li>Вкладки: Товары, Продажи, Выручка</li>
 *     <li>Поиск по отдельным полям: название, бренд, категория</li>
 *     <li>Добавление/обновление/удаление/продажа товара</li>
 *     <li>Диаграммы (выручка и количество по категориям)</li>
 *     <li>Логирование действий (добавление/удаление/обновление/продажа/обновление выручки)</li>
 * </ul>
 *
 * Класс ожидает наличие {@link main.java.data.DataStorage} реализованного в {@link JsonDataStorage}
 * и корректного {@link main.java.service.StoreService} (см. твой проект).
 */
public class StoreApp extends Application {

    private static final Logger log = LogManager.getLogger(StoreApp.class);

    private TableView<CosmeticProduct> tableView;
    private TableView<Sale> salesTable;
    private Label revenueLabel;
    private VBox revenueByCategoryBox;
    private VBox countByCategoryBox;

    private StoreService storeService;

    /** Путь к файлу с товарами */
    private static final String PRODUCTS_FILE_PATH =
            System.getProperty("user.dir") + "/src/main/java/data/products.json";

    /** Путь к файлу с продажами */
    private static final String SALES_FILE_PATH =
            System.getProperty("user.dir") + "/src/main/java/data/sales.json";

    /** Категории, используемые в ComboBox'ах */
    private static final String[] CATEGORIES = {
            "Уход", "Макияж", "Волосы", "Парфюмерия", "Маникюр", "Гигиена", "Другое"
    };

    @Override
    public void start(Stage primaryStage) {
        // Инициализация сервисного слоя
        storeService = new StoreService(new JsonDataStorage(PRODUCTS_FILE_PATH, SALES_FILE_PATH));
        log.info("StoreApp стартует");

        // TabPane — увеличим шрифт заголовков вкладок
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        // Стили для tab headers (простое и часто достаточное)
        tabs.setStyle("-fx-font-size: 14px; -fx-tab-min-width: 120;");

        Tab tabProducts = new Tab("Товары", createProductsTab());
        Tab tabSales = new Tab("Продажи", createSalesTab());
        Tab tabRevenue = new Tab("Выручка", createRevenueTab());

        tabs.getTabs().addAll(tabProducts, tabSales, tabRevenue);

        // При переключении вкладок — обновляем данные
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldT, newT) -> {
            if (newT == tabProducts) refreshProductsTable();
            if (newT == tabSales) refreshSalesTable();
            if (newT == tabRevenue) refreshRevenueTab();
        });

        Scene scene = new Scene(tabs, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Косметический магазин");
        primaryStage.show();
    }

    // ========================= PRODUCTS TAB =========================

    /**
     * Создает контент вкладки "Товары".
     *
     * @return VBox с интерфейсом управления товарами
     */
    private VBox createProductsTab() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(12));

        // Поиск: отдельные поля для названия, бренда и категории
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField nameSearch = new TextField();
        nameSearch.setPromptText("Название");

        TextField brandSearch = new TextField();
        brandSearch.setPromptText("Бренд");

        ComboBox<String> categorySearch = new ComboBox<>();
        categorySearch.getItems().addAll(CATEGORIES);
        categorySearch.getItems().add(0, "Все категории");
        categorySearch.setValue("Все категории");

        Button btnSearch = styledButton("Поиск", "#2196F3");
        Button btnClear = styledButton("Сброс", "#9E9E9E");

        searchBox.getChildren().addAll(new Label("Поиск:"), nameSearch, brandSearch, categorySearch, btnSearch, btnClear);

        // Панель действий
        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        Button btnAdd = styledButton("Добавить", "#4CAF50");
        Button btnUpdate = styledButton("Обновить", "#1976D2");
        Button btnDelete = styledButton("Удалить", "#F44336");
        Button btnSell = styledButton("Продать", "#FF9800");

        actionBox.getChildren().addAll(btnAdd, btnUpdate, btnDelete, btnSell);

        // Таблица товаров
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CosmeticProduct, Integer> cId = new TableColumn<>("ID");
        cId.setPrefWidth(70);
        cId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());

        TableColumn<CosmeticProduct, String> cName = new TableColumn<>("Название");
        cName.setPrefWidth(320);
        cName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));

        TableColumn<CosmeticProduct, Double> cPrice = new TableColumn<>("Цена (тг.)");
        cPrice.setPrefWidth(120);
        cPrice.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrice()).asObject());

        TableColumn<CosmeticProduct, Integer> cQty = new TableColumn<>("Кол-во");
        cQty.setPrefWidth(100);
        cQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantity()).asObject());

        TableColumn<CosmeticProduct, String> cBrand = new TableColumn<>("Бренд");
        cBrand.setPrefWidth(180);
        cBrand.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBrand()));

        TableColumn<CosmeticProduct, String> cCategory = new TableColumn<>("Категория");
        cCategory.setPrefWidth(160);
        cCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));

        tableView.getColumns().addAll(cId, cName, cPrice, cQty, cBrand, cCategory);

        // Контекстное меню (правый клик) + двойной клик для редактирования
        tableView.setRowFactory(tv -> {
            TableRow<CosmeticProduct> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();

            MenuItem miUpdate = new MenuItem("Обновить");
            MenuItem miDelete = new MenuItem("Удалить");
            MenuItem miSell = new MenuItem("Продать");

            miUpdate.setOnAction(e -> openUpdateDialog(row.getItem()));
            miDelete.setOnAction(e -> {
                CosmeticProduct p = row.getItem();
                if (p != null && confirm("Удаление", "Удалить товар: " + p.getName() + " ?")) {
                    storeService.deleteProduct(p);
                    log.info("Товар удалён (контекст): id={}, name={}", p.getId(), p.getName());
                    refreshProductsTable();
                }
            });
            miSell.setOnAction(e -> openSellDialog(row.getItem()));

            menu.getItems().addAll(miUpdate, miDelete, miSell);

            row.setOnContextMenuRequested(e -> {
                if (!row.isEmpty()) menu.show(row, e.getScreenX(), e.getScreenY());
            });

            row.setOnMouseClicked(ev -> {
                if (!row.isEmpty() && ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == 2) {
                    openUpdateDialog(row.getItem());
                }
            });
            return row;
        });

        refreshProductsTable();

        // Обработчики кнопок
        btnAdd.setOnAction(e -> {
            openAddDialog();
            refreshProductsTable();
        });

        btnUpdate.setOnAction(e -> {
            CosmeticProduct sel = tableView.getSelectionModel().getSelectedItem();
            if (sel == null) { info("Выберите товар для обновления"); return; }
            openUpdateDialog(sel);
            refreshProductsTable();
        });

        btnDelete.setOnAction(e -> {
            CosmeticProduct sel = tableView.getSelectionModel().getSelectedItem();
            if (sel == null) { info("Выберите товар для удаления"); return; }
            if (confirm("Удаление", "Удалить товар: " + sel.getName() + " ?")) {
                storeService.deleteProduct(sel);
                log.info("Товар удалён (кнопка): id={}, name={}", sel.getId(), sel.getName());
                refreshProductsTable();
            }
        });

        btnSell.setOnAction(e -> {
            CosmeticProduct sel = tableView.getSelectionModel().getSelectedItem();
            if (sel == null) { info("Выберите товар для продажи"); return; }
            openSellDialog(sel);
            refreshProductsTable();
            refreshSalesTable();
        });

        // Поиск по трем полям
        btnSearch.setOnAction(e -> {
            String nameQ = Optional.ofNullable(nameSearch.getText()).orElse("").trim().toLowerCase();
            String brandQ = Optional.ofNullable(brandSearch.getText()).orElse("").trim().toLowerCase();
            String catQ = Optional.ofNullable(categorySearch.getValue()).orElse("").trim();

            List<CosmeticProduct> filtered = storeService.getAllProducts().stream()
                    .filter(p -> (nameQ.isEmpty() || (p.getName()!=null && p.getName().toLowerCase().contains(nameQ)))
                            && (brandQ.isEmpty() || (p.getBrand()!=null && p.getBrand().toLowerCase().contains(brandQ)))
                            && (catQ.equals("Все категории") || catQ.isEmpty() || (p.getCategory()!=null && p.getCategory().equals(catQ)))
                    ).collect(Collectors.toList());

            tableView.getItems().setAll(filtered);
        });

        btnClear.setOnAction(e -> {
            nameSearch.clear();
            brandSearch.clear();
            categorySearch.setValue("Все категории");
            refreshProductsTable();
        });

        // Собираем UI
        root.getChildren().addAll(searchBox, actionBox, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return root;
    }

    // ========================= SALES TAB =========================

    /** Полный список продаж для фильтрации */
    private List<Sale> allSalesList = new ArrayList<>();

    /**
     * Создает вкладку "Продажи" с таблицей продаж, поиском и возвратом.
     *
     * @return VBox с таблицей продаж и элементами управления
     */
    private VBox createSalesTab() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));

        Label title = new Label("История продаж");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        // === Поиск ===
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField nameSearch = new TextField();
        nameSearch.setPromptText("Название товара");

        ComboBox<String> categorySearch = new ComboBox<>();
        categorySearch.getItems().addAll(CATEGORIES);
        categorySearch.getItems().add(0, "Все категории");
        categorySearch.setValue("Все категории");

        DatePicker dateFrom = new DatePicker();
        dateFrom.setPromptText("Дата с");
        dateFrom.setPrefWidth(130);

        DatePicker dateTo = new DatePicker();
        dateTo.setPromptText("Дата по");
        dateTo.setPrefWidth(130);

        Button btnSearch = styledButton("Поиск", "#2196F3");
        Button btnClear = styledButton("Сброс", "#9E9E9E");

        searchBox.getChildren().addAll(
                new Label("Поиск:"), nameSearch, categorySearch,
                new Label("С:"), dateFrom, new Label("По:"), dateTo,
                btnSearch, btnClear
        );

        // === Кнопки действий ===
        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        Button btnRefresh = styledButton("Обновить", "#2196F3");
        Button btnRefund = styledButton("Возврат", "#F44336");

        actionBox.getChildren().addAll(btnRefresh, btnRefund);

        // === Таблица ===
        salesTable = new TableView<>();
        salesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Sale, String> colName = new TableColumn<>("Товар");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProduct().getName()));
        colName.setPrefWidth(280);

        TableColumn<Sale, String> colBrand = new TableColumn<>("Бренд");
        colBrand.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProduct().getBrand()));
        colBrand.setPrefWidth(120);

        TableColumn<Sale, Integer> colQty = new TableColumn<>("Кол-во");
        colQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantitySold()).asObject());
        colQty.setPrefWidth(80);

        TableColumn<Sale, Double> colTotal = new TableColumn<>("Сумма (тг.)");
        colTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotal()).asObject());
        colTotal.setPrefWidth(120);

        TableColumn<Sale, String> colDate = new TableColumn<>("Дата");
        colDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));
        colDate.setPrefWidth(160);

        TableColumn<Sale, String> colCategory = new TableColumn<>("Категория");
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProduct().getCategory()));
        colCategory.setPrefWidth(120);

        salesTable.getColumns().addAll(colName, colBrand, colQty, colTotal, colDate, colCategory);

        // Контекстное меню для возврата
        salesTable.setRowFactory(tv -> {
            TableRow<Sale> row = new TableRow<>();
            ContextMenu menu = new ContextMenu();

            MenuItem miRefund = new MenuItem("Оформить возврат");
            miRefund.setOnAction(e -> {
                Sale sale = row.getItem();
                if (sale != null) {
                    openRefundDialog(sale);
                }
            });

            menu.getItems().add(miRefund);

            row.setOnContextMenuRequested(e -> {
                if (!row.isEmpty()) menu.show(row, e.getScreenX(), e.getScreenY());
            });

            return row;
        });

        refreshSalesTable();

        // === Обработчики ===
        btnRefresh.setOnAction(e -> refreshSalesTable());

        btnRefund.setOnAction(e -> {
            Sale selected = salesTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                info("Выберите продажу для возврата");
                return;
            }
            openRefundDialog(selected);
        });

        btnSearch.setOnAction(e -> {
            String nameFilter = nameSearch.getText().trim().toLowerCase();
            String categoryFilter = categorySearch.getValue();
            java.time.LocalDate fromDate = dateFrom.getValue();
            java.time.LocalDate toDate = dateTo.getValue();

            List<Sale> filtered = allSalesList.stream()
                    .filter(s -> nameFilter.isEmpty() ||
                            s.getProduct().getName().toLowerCase().contains(nameFilter))
                    .filter(s -> "Все категории".equals(categoryFilter) ||
                            categoryFilter.equals(s.getProduct().getCategory()))
                    .filter(s -> fromDate == null ||
                            !s.getDateTime().toLocalDate().isBefore(fromDate))
                    .filter(s -> toDate == null ||
                            !s.getDateTime().toLocalDate().isAfter(toDate))
                    .collect(Collectors.toList());

            salesTable.getItems().setAll(filtered);
            log.info("Поиск продаж: найдено {} записей", filtered.size());
        });

        btnClear.setOnAction(e -> {
            nameSearch.clear();
            categorySearch.setValue("Все категории");
            dateFrom.setValue(null);
            dateTo.setValue(null);
            refreshSalesTable();
        });

        root.getChildren().addAll(title, searchBox, actionBox, salesTable);
        VBox.setVgrow(salesTable, Priority.ALWAYS);
        return root;
    }

    /**
     * Открывает диалог подтверждения возврата товара.
     *
     * @param sale продажа для возврата
     */
    private void openRefundDialog(Sale sale) {
        String msg = String.format(
                "Оформить возврат?\n\nТовар: %s\nКоличество: %d шт.\nСумма: %.2f тг.\nДата продажи: %s",
                sale.getProduct().getName(),
                sale.getQuantitySold(),
                sale.getTotal(),
                sale.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );

        if (confirm("Возврат товара", msg)) {
            String result = storeService.refundSale(sale);
            log.info("Возврат: {}", result);
            info(result);
            refreshSalesTable();
            refreshProductsTable();
        }
    }

    // ========================= REVENUE TAB =========================

    /**
     * Создает вкладку "Выручка" с общей суммой, разбивкой по категориям и количеством по категориям.
     *
     * @return VBox с компонентами выручки
     */
    private VBox createRevenueTab() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(12));

        Label title = new Label("Выручка");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        revenueLabel = new Label(storeService.showTotalRevenue());
        revenueLabel.setFont(Font.font("Segoe UI", 16));

        Button btnRefresh = styledButton("Обновить", "#2196F3");
        btnRefresh.setOnAction(e -> refreshRevenueTab());

        HBox content = new HBox(20);
        content.setAlignment(Pos.TOP_LEFT);

        // Левый блок — суммарная информация и диаграммы (кнопка открыть)
        VBox left = new VBox(12);
        left.setPrefWidth(600);
        left.getChildren().addAll(new Label("Общая выручка:"), revenueLabel, btnRefresh);

        Button btnCharts = styledButton("Показать диаграммы", "#1976D2");
        btnCharts.setOnAction(e -> openChartsWindow());

        left.getChildren().add(btnCharts);

        // Правый блок — две колонки: выручка по категориям и количество проданных по категориям
        VBox right = new VBox(10);
        right.setPrefWidth(520);

        Label l1 = new Label("Выручка по категориям:");
        l1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        revenueByCategoryBox = new VBox(6);

        Label l2 = new Label("Количество проданных товаров по категориям:");
        l2.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        countByCategoryBox = new VBox(6);

        right.getChildren().addAll(l1, revenueByCategoryBox, new Separator(), l2, countByCategoryBox);

        content.getChildren().addAll(left, right);
        root.getChildren().addAll(title, content);
        return root;
    }

    /**
     * Обновляет содержимое вкладки "Выручка": общую сумму, выручку по категориям и количество по категориям.
     */
    private void refreshRevenueTab() {
        String total = storeService.showTotalRevenue();
        revenueLabel.setText(total);
        log.info("Обновление выручки: {}", total);

        Map<String, Double> revenueByCat = new HashMap<>();
        Map<String, Integer> countByCat = new HashMap<>();

        for (Sale s : storeService.getAllSales()) {
            String cat = Optional.ofNullable(s.getProduct().getCategory()).orElse("Другое");
            revenueByCat.put(cat, revenueByCat.getOrDefault(cat, 0.0) + s.getTotal());
            countByCat.put(cat, countByCat.getOrDefault(cat, 0) + s.getQuantitySold());
        }

        // Отобразим отсортированные результаты
        revenueByCategoryBox.getChildren().clear();
        countByCategoryBox.getChildren().clear();

        revenueByCat.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> revenueByCategoryBox.getChildren().add(
                        new Label(e.getKey() + ": " + String.format("%.2f тг.", e.getValue()))
                ));

        countByCat.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> countByCategoryBox.getChildren().add(
                        new Label(e.getKey() + ": " + e.getValue() + " шт.")
                ));
    }

    // ========================= DIALOGS =========================

    /**
     * Открывает диалог добавления товара. Использует ComboBox для выбора категории.
     */
    private void openAddDialog() {
        Stage w = new Stage();
        w.initModality(Modality.APPLICATION_MODAL);
        w.setTitle("Добавить товар");

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));

        TextField name = new TextField();
        TextField price = new TextField();
        TextField qty = new TextField();
        TextField brand = new TextField();

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll(CATEGORIES);
        catBox.setValue(CATEGORIES[0]);

        name.setPromptText("Название");
        price.setPromptText("Цена (число)");
        qty.setPromptText("Кол-во (целое)");
        brand.setPromptText("Бренд");

        Button btnSave = styledButton("Сохранить", "#4CAF50");
        btnSave.setOnAction(e -> {
            try {
                double pr = Double.parseDouble(price.getText().trim());
                int q = Integer.parseInt(qty.getText().trim());
                String cat = catBox.getValue();

                storeService.addProductAutoId(
                        name.getText().trim(),
                        pr,
                        q,
                        brand.getText().trim(),
                        cat
                );

                tableView.refresh(); // мгновенное обновление

                log.info("Добавлен товар: name='{}', price={}, qty={}, brand='{}', cat='{}'",
                        name.getText(), pr, q, brand.getText(), cat);

                w.close();

            } catch (InvalidFormatException | EmptyFieldException |
                     NegativeValueException ex1) {
                info(ex1.getMessage());

            } catch (NumberFormatException ex2) {
                info("Введите корректную цену и количество");

            } catch (Exception ex3) {
                log.error("Ошибка при добавлении: {}", ex3.getMessage(), ex3);
                info("Ошибка при добавлении: " + ex3.getMessage());
            }
        });

        root.getChildren().addAll(
                new Label("Название"), name,
                new Label("Цена"), price,
                new Label("Кол-во"), qty,
                new Label("Бренд"), brand,
                new Label("Категория"), catBox,
                btnSave
        );

        w.setScene(new Scene(root, 420, 460));
        w.showAndWait();
    }


    /**
     * Открывает диалог обновления товара.
     *
     * @param p редактируемый товар
     */
    private void openUpdateDialog(CosmeticProduct p) {
        if (p == null) return;

        Stage w = new Stage();
        w.initModality(Modality.APPLICATION_MODAL);
        w.setTitle("Обновить товар");

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));

        TextField name = new TextField(p.getName());
        TextField price = new TextField(String.valueOf(p.getPrice()));
        TextField qty = new TextField(String.valueOf(p.getQuantity()));
        TextField brand = new TextField(Optional.ofNullable(p.getBrand()).orElse(""));

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll(CATEGORIES);
        catBox.setValue(Optional.ofNullable(p.getCategory()).orElse(CATEGORIES[0]));

        Button btnSave = styledButton("Сохранить", "#1976D2");
        btnSave.setOnAction(e -> {
            try {
                double pr = Double.parseDouble(price.getText().trim());
                int q = Integer.parseInt(qty.getText().trim());
                String newName = name.getText().trim();
                String newBrand = brand.getText().trim();
                String newCat = catBox.getValue();

                storeService.updateProduct(p, newName, pr, q);

                // обновляем brand/category
                safeSetField(p, "brand", newBrand);
                safeSetField(p, "category", newCat);

                storeService.saveData();
                tableView.refresh(); // обновляет строку

                log.info(
                        "Обновлён товар: id={}, name='{}', price={}, qty={}, brand='{}', category='{}'",
                        p.getId(), newName, pr, q, newBrand, newCat
                );

                w.close();

            } catch (InvalidFormatException | EmptyFieldException |
                     NegativeValueException ex1) {
                info(ex1.getMessage());

            } catch (NumberFormatException ex2) {
                info("Введите корректные числовые значения");

            } catch (Exception ex3) {
                log.error("Ошибка при обновлении: {}", ex3.getMessage(), ex3);
                info("Ошибка при обновлении: " + ex3.getMessage());
            }
        });

        root.getChildren().addAll(
                new Label("Название"), name,
                new Label("Цена"), price,
                new Label("Кол-во"), qty,
                new Label("Бренд"), brand,
                new Label("Категория"), catBox,
                btnSave
        );

        w.setScene(new Scene(root, 420, 440));
        w.showAndWait();
    }


    /**
     * Открывает диалог продажи товара.
     *
     * @param p товар для продажи
     */
    private void openSellDialog(CosmeticProduct p) {
        if (p == null) return;

        Stage w = new Stage();
        w.initModality(Modality.APPLICATION_MODAL);
        w.setTitle("Продажа");

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));

        TextField qty = new TextField();
        qty.setPromptText("Количество");

        Button btnSell = styledButton("Продать", "#FF9800");
        btnSell.setOnAction(e -> {
            try {
                int q = Integer.parseInt(qty.getText().trim());

                storeService.sellProduct(p, q);

                log.info("Продано: id={}, name='{}', qty={}", p.getId(), p.getName(), q);

                tableView.refresh(); // обновляет количество в таблице
                w.close();

            } catch (QuantityExceededException | NegativeValueException ex1) {
                info(ex1.getMessage());

            } catch (NumberFormatException ex2) {
                info("Введите корректное количество");

            } catch (Exception ex3) {
                log.error("Ошибка при продаже: {}", ex3.getMessage(), ex3);
                info("Ошибка при продаже: " + ex3.getMessage());
            }
        });

        root.getChildren().addAll(
                new Label("Товар: " + p.getName()),
                new Label("Количество"),
                qty,
                btnSell
        );

        w.setScene(new Scene(root, 320, 180));
        w.showAndWait();
    }




    // ========================= CHARTS =========================

    /**
     * Открывает окно с диаграммами:
     *  - круговая по выручке по категориям
     *  - круговая по количеству проданных единиц по категориям
     *  - столбчатые графики (выручка / количество)
     */
    private void openChartsWindow() {
        Stage w = new Stage();
        w.initModality(Modality.APPLICATION_MODAL);
        w.setTitle("Диаграммы по категориям");

        Map<String, Double> revenueByCat = new HashMap<>();
        Map<String, Integer> countByCat = new HashMap<>();
        for (Sale s : storeService.getAllSales()) {
            String cat = Optional.ofNullable(s.getProduct().getCategory()).orElse("Другое");
            revenueByCat.put(cat, revenueByCat.getOrDefault(cat, 0.0) + s.getTotal());
            countByCat.put(cat, countByCat.getOrDefault(cat, 0) + s.getQuantitySold());
        }

        // Pie chart for revenue
        PieChart pieRevenue = new PieChart();
        pieRevenue.setTitle("Выручка по категориям (тг.)");
        revenueByCat.forEach((k, v) -> pieRevenue.getData().add(new PieChart.Data(k, v)));

        // Pie chart for counts
        PieChart pieCount = new PieChart();
        pieCount.setTitle("Количество проданных единиц по категориям");
        countByCat.forEach((k, v) -> pieCount.getData().add(new PieChart.Data(k, v)));

        // Bar chart for revenue
        CategoryAxis xAxisR = new CategoryAxis();
        NumberAxis yAxisR = new NumberAxis();
        BarChart<String, Number> barRevenue = new BarChart<>(xAxisR, yAxisR);
        barRevenue.setTitle("Выручка по категориям");
        xAxisR.setLabel("Категория");
        yAxisR.setLabel("Сумма (тг.)");
        XYChart.Series<String, Number> seriesR = new XYChart.Series<>();
        revenueByCat.forEach((k, v) -> seriesR.getData().add(new XYChart.Data<>(k, v)));
        barRevenue.getData().add(seriesR);

        // Bar chart for counts
        CategoryAxis xAxisC = new CategoryAxis();
        NumberAxis yAxisC = new NumberAxis();
        BarChart<String, Number> barCount = new BarChart<>(xAxisC, yAxisC);
        barCount.setTitle("Количество проданных единиц");
        xAxisC.setLabel("Категория");
        yAxisC.setLabel("Кол-во (шт.)");
        XYChart.Series<String, Number> seriesC = new XYChart.Series<>();
        countByCat.forEach((k, v) -> seriesC.getData().add(new XYChart.Data<>(k, v)));
        barCount.getData().add(seriesC);

        VBox chartsLeft = new VBox(10, pieRevenue, barRevenue);
        VBox chartsRight = new VBox(10, pieCount, barCount);
        HBox root = new HBox(12, chartsLeft, chartsRight);
        root.setPadding(new Insets(12));

        w.setScene(new Scene(root, 1100, 700));
        w.showAndWait();
    }



    // ========================= HELPERS =========================

    /**
     * Безопасная установка приватного поля через reflection, если публичный сеттер не работает.
     *
     * @param obj       объект
     * @param fieldName имя поля (например "brand", "category")
     * @param value     новое значение
     */
    private void safeSetField(Object obj, String fieldName, String value) {
        try {
            // Сначала пробуем вызвать сеттер (если есть)
            String setter = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            try {
                obj.getClass().getMethod(setter, String.class).invoke(obj, value);
                return;
            } catch (NoSuchMethodException ignored) {
                // если нет — пробуем через reflection
            }
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (NoSuchFieldException nsf) {
            log.warn("Поле '{}' не найдено в классе {}", fieldName, obj.getClass().getName());
        } catch (Exception ex) {
            log.error("Не удалось установить поле '{}' через reflection: {}", fieldName, ex.getMessage(), ex);
        }
    }

    /**
     * Обновляет таблицу товаров данными из сервиса.
     */
    private void refreshProductsTable() {
        tableView.getItems().setAll(storeService.getAllProducts());
    }

    /**
     * Обновляет таблицу продаж данными из сервиса.
     */
    private void refreshSalesTable() {
        allSalesList = storeService.getAllSales();
        if (salesTable != null) {
            salesTable.getItems().setAll(allSalesList);
        }
    }

    /**
     * Небольшое информационное окно.
     *
     * @param text текст сообщения
     */
    private void info(String text) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(text);
        a.showAndWait();
    }

    /**
     * Диалог подтверждения.
     *
     * @param title заголовок
     * @param msg   сообщение
     * @return true, если пользователь подтвердил
     */
    private boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        Optional<ButtonType> res = a.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }

    /**
     * Утилитный метод — создаёт стилизованную кнопку.
     *
     * @param text  текст
     * @param color фон
     * @return Button
     */
    private Button styledButton(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
        b.setPrefHeight(36);
        return b;
    }

    @Override
    public void stop() {
        try {
            storeService.saveData();
            log.info("Приложение остановлено, данные сохранены");
        } catch (Exception ex) {
            log.error("Ошибка при сохранении данных при остановке: {}", ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
