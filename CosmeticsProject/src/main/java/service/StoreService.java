package main.java.service;

import main.java.data.DataStorage;
import main.java.exceptions.EmptyFieldException;
import main.java.exceptions.NegativeValueException;
import main.java.exceptions.QuantityExceededException;
import main.java.model.CosmeticProduct;
import main.java.model.Sale;

import java.util.ArrayList;
import java.util.List;
/**
 * Сервисный класс, реализующий бизнес-логику магазина косметических товаров.
 * <p>
 * Класс обеспечивает выполнение основных операций по работе с ассортиментом и продажами:
 * <ul>
 *     <li>добавление новых товаров;</li>
 *     <li>обновление характеристик существующих товаров;</li>
 *     <li>удаление товаров;</li>
 *     <li>регистрация продаж;</li>
 *     <li>расчёт общей выручки;</li>
 *     <li>сохранение данных через абстракцию {@link DataStorage}.</li>
 * </ul>
 *
 * Все изменения ассортимента и продаж автоматически сохраняются в выбранном
 * хранилище данных. При создании экземпляра класса происходит загрузка ранее сохранённых данных.
 */
public class StoreService {
    /** Хранилище данных, реализующее операции загрузки и сохранения. */
    private final DataStorage storage;
    /** Список товаров, находящихся в ассортименте. */
    private List<CosmeticProduct> products;
    /** Список зарегистрированных продаж. */
    private List<Sale> sales;
    /** Генератор идентификаторов для новых товаров. */
    private int nextId = 1;

    /**
     * Создаёт сервис магазина и загружает данные из указанного хранилища.
     * <p>
     * Если в загруженных данных есть товары, значение {@code nextId} автоматически
     * устанавливается равным последнему используемому идентификатору + 1.
     *
     * @param storage объект, реализующий интерфейс {@link DataStorage}
     */
    public StoreService(DataStorage storage) {
        this.storage = storage;

        DataStorage.LoadedData data = storage.load();
        this.products = data.products;
        this.sales = data.sales;

        if (!products.isEmpty()) {
            nextId = products.get(products.size() - 1).getId() + 1;
        }
    }
    /**
     * Возвращает копию списка всех товаров в магазине.
     *
     * @return список товаров
     */
    public List<CosmeticProduct> getAllProducts() {
        return new ArrayList<>(products);
    }

    /**
     * Добавляет новый товар с автоматически присвоенным идентификатором.
     * <p>
     * Выполняется валидация входных данных: название, бренд и категория
     * не могут быть пустыми; цена и количество должны быть неотрицательны.
     *
     * @param name     название товара
     * @param price    цена товара
     * @param qty      количество на складе
     * @param brand    производитель товара
     * @param category категория товара
     * @throws EmptyFieldException      если строковые поля пустые
     * @throws NegativeValueException   если цена или количество меньше 0
     */
    public void addProductAutoId(String name, double price, int qty, String brand, String category) {

        if (name == null || name.isBlank())
            throw new EmptyFieldException("Название не может быть пустым");

        if (brand == null || brand.isBlank())
            throw new EmptyFieldException("Бренд не может быть пустым");

        if (category == null || category.isBlank())
            throw new EmptyFieldException("Категория не может быть пустой");

        if (price < 0)
            throw new NegativeValueException("Цена не может быть отрицательной");

        if (qty < 0)
            throw new NegativeValueException("Количество не может быть отрицательным");

        CosmeticProduct p = new CosmeticProduct(nextId++, name, price, qty, brand, category);
        products.add(p);
        saveData();
    }

    /**
     * Обновляет характеристики указанного товара.
     * <p>
     * Выполняется валидация входных данных: новое название не может быть пустым,
     * цена и количество должны быть неотрицательными.
     *
     * @param product товар, подлежащий обновлению
     * @param newName новое название товара
     * @param newPrice новая цена
     * @param newQty новое количество товара
     * @return сообщение об успешном обновлении
     * @throws EmptyFieldException     если новое название пустое
     * @throws NegativeValueException  если цена или количество отрицательные
     */
    public String updateProduct(CosmeticProduct product, String newName, double newPrice, int newQty) {

        if (newName == null || newName.isBlank())
            throw new EmptyFieldException("Название не может быть пустым");

        if (newPrice < 0)
            throw new NegativeValueException("Цена не может быть отрицательной");

        if (newQty < 0)
            throw new NegativeValueException("Количество не может быть отрицательным");

        product.setName(newName);
        product.setPrice(newPrice);
        product.setQuantity(newQty);

        saveData();
        return "Товар обновлён!";
    }

    /**
     * Удаляет указанный товар из ассортимента.
     *
     * @param product товар, который необходимо удалить
     * @return сообщение об успешном удалении
     */
    public String deleteProduct(CosmeticProduct product) {
        products.remove(product);
        saveData();
        return "Товар удалён!";
    }

    /**
     * Регистрирует продажу указанного товара.
     * <p>
     * Проверяется корректность количества и наличие достаточного остатка.
     *
     * @param product товар, который продаётся
     * @param qty     количество продаваемых единиц
     * @return сообщение об успешной продаже
     * @throws NegativeValueException      если количество меньше или равно нулю
     * @throws QuantityExceededException   если запрошенное количество превышает остаток
     */
    public String sellProduct(CosmeticProduct product, int qty) {

        if (qty <= 0)
            throw new NegativeValueException("Количество должно быть больше нуля");

        if (qty > product.getQuantity())
            throw new QuantityExceededException("Нельзя продать больше товара, чем есть на складе");

        product.decreaseQuantity(qty);
        sales.add(new Sale(product, qty));

        saveData();
        return "Продано!";
    }


    /**
     * Возвращает строковое представление общей выручки,
     * вычисленной на основе зарегистрированных продаж.
     *
     * @return строка с общей суммой выручки в тенге
     */
    public String showTotalRevenue() {
        double sum = 0;
        for (Sale s : sales) sum += s.getTotal();

        return "Общая выручка: " + sum + " тг.";
    }

    /**
     * Сохраняет текущее состояние ассортимента и списка продаж в хранилище данных.
     */
    public void saveData() {
        storage.save(products, sales);
    }
    /**
     * Возвращает копию списка всех продаж.
     *
     * @return список продаж
     */
    public List<Sale> getAllSales() {
        return new ArrayList<>(sales);
    }

    /**
     * Оформляет возврат товара по указанной продаже.
     * <p>
     * Количество товара на складе увеличивается на количество из продажи,
     * а сама продажа удаляется из истории.
     *
     * @param sale запись о продаже, которую необходимо отменить
     * @return сообщение об успешном возврате
     */
    public String refundSale(Sale sale) {
        CosmeticProduct product = sale.getProduct();

        // Находим товар в текущем списке по ID
        CosmeticProduct existingProduct = findProductById(product.getId());

        if (existingProduct != null) {
            // Возвращаем количество на склад
            existingProduct.setQuantity(existingProduct.getQuantity() + sale.getQuantitySold());
        } else {
            // Если товар был удалён — восстанавливаем его
            product.setQuantity(sale.getQuantitySold());
            products.add(product);
        }

        // Удаляем продажу из истории
        sales.remove(sale);
        saveData();

        return "Возврат оформлен! Товар: " + product.getName() +
                ", количество: " + sale.getQuantitySold() + " шт.";
    }

    /**
     * Ищет товар по идентификатору.
     *
     * @param id идентификатор товара
     * @return найденный товар или null, если не найден
     */
    public CosmeticProduct findProductById(int id) {
        for (CosmeticProduct p : products) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

}
