package main.java.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import main.java.model.CosmeticProduct;
import main.java.model.Sale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация интерфейса {@link DataStorage}, использующая JSON-файлы
 * для сохранения и загрузки данных. Данные сериализуются с помощью
 * библиотеки Gson.
 * <p>
 * Товары и продажи хранятся в отдельных файлах.
 * <p>
 * Поддерживается автоматическая обработка типа {@link LocalDateTime},
 * а также форматирование JSON с отступами.
 */
public class JsonDataStorage implements DataStorage {

    /** Логгер для записи операций сохранения/загрузки. */
    private static final Logger log = LogManager.getLogger(JsonDataStorage.class);

    /** Путь к JSON-файлу для хранения товаров. */
    private final String productsFilePath;

    /** Путь к JSON-файлу для хранения продаж. */
    private final String salesFilePath;

    /** Настроенный экземпляр Gson, используемый для сериализации. */
    private final Gson gson;

    /**
     * Создаёт новое хранилище, работающее с двумя JSON-файлами.
     *
     * @param productsFilePath путь к файлу для хранения товаров
     * @param salesFilePath    путь к файлу для хранения продаж
     */
    public JsonDataStorage(String productsFilePath, String salesFilePath) {
        this.productsFilePath = productsFilePath;
        this.salesFilePath = salesFilePath;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, type, context) ->
                                new JsonPrimitive(src.toString()))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, type, context) ->
                                LocalDateTime.parse(json.getAsString()))
                .setPrettyPrinting()
                .create();
    }

    /**
     * Сохраняет список товаров в JSON-файл.
     *
     * @param products список косметических товаров
     */
    @Override
    public void saveProducts(List<CosmeticProduct> products) {
        log.info("Сохранение товаров в JSON: количество=" + products.size());

        try (FileWriter writer = new FileWriter(productsFilePath)) {
            gson.toJson(products, writer);
            log.info("Товары успешно сохранены в файл: " + productsFilePath);
        } catch (IOException e) {
            log.error("Ошибка сохранения товаров в JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Сохраняет список продаж в JSON-файл.
     *
     * @param sales список продаж
     */
    @Override
    public void saveSales(List<Sale> sales) {
        log.info("Сохранение продаж в JSON: количество=" + sales.size());

        try (FileWriter writer = new FileWriter(salesFilePath)) {
            gson.toJson(sales, writer);
            log.info("Продажи успешно сохранены в файл: " + salesFilePath);
        } catch (IOException e) {
            log.error("Ошибка сохранения продаж в JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Сохраняет списки товаров и продаж в соответствующие JSON-файлы.
     *
     * @param products список косметических товаров
     * @param sales    список продаж
     */
    @Override
    public void save(List<CosmeticProduct> products, List<Sale> sales) {
        log.info("Сохранение данных: товаров=" + products.size() + ", продаж=" + sales.size());
        saveProducts(products);
        saveSales(sales);
    }

    /**
     * Загружает список товаров из JSON-файла.
     * Если файл отсутствует или пуст — возвращает пустой список.
     *
     * @return список косметических товаров
     */
    @Override
    public List<CosmeticProduct> loadProducts() {
        log.info("Загрузка товаров из файла: " + productsFilePath);

        try (FileReader reader = new FileReader(productsFilePath)) {
            Type type = new TypeToken<List<CosmeticProduct>>() {}.getType();
            List<CosmeticProduct> products = gson.fromJson(reader, type);

            if (products == null) {
                log.warn("Файл товаров пуст. Возвращаем пустой список.");
                return new ArrayList<>();
            }

            log.info("Успешная загрузка товаров: количество=" + products.size());
            return products;

        } catch (IOException e) {
            log.warn("Файл товаров не найден или недоступен: " + productsFilePath);
            return new ArrayList<>();
        }
    }

    /**
     * Загружает список продаж из JSON-файла.
     * Если файл отсутствует или пуст — возвращает пустой список.
     *
     * @return список продаж
     */
    @Override
    public List<Sale> loadSales() {
        log.info("Загрузка продаж из файла: " + salesFilePath);

        try (FileReader reader = new FileReader(salesFilePath)) {
            Type type = new TypeToken<List<Sale>>() {}.getType();
            List<Sale> sales = gson.fromJson(reader, type);

            if (sales == null) {
                log.warn("Файл продаж пуст. Возвращаем пустой список.");
                return new ArrayList<>();
            }

            log.info("Успешная загрузка продаж: количество=" + sales.size());
            return sales;

        } catch (IOException e) {
            log.warn("Файл продаж не найден или недоступен: " + salesFilePath);
            return new ArrayList<>();
        }
    }

    /**
     * Загружает все данные из JSON-файлов.
     *
     * @return объект {@link LoadedData}, содержащий товары и продажи
     */
    @Override
    public LoadedData load() {
        log.info("Загрузка всех данных из файлов");
        List<CosmeticProduct> products = loadProducts();
        List<Sale> sales = loadSales();
        return new LoadedData(products, sales);
    }
}
