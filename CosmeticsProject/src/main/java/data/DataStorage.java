package main.java.data;

import main.java.model.CosmeticProduct;
import main.java.model.Product;
import main.java.model.Sale;

import java.util.List;

/**
 * Интерфейс для работы с хранилищем данных.
 * Определяет операции сохранения и загрузки информации о товарах и продажах.
 * <p>
 * Реализации данного интерфейса могут использовать различные способы хранения:
 * JSON, БД, файлы и др.
 * <p>
 * Данные хранятся в двух отдельных файлах: для товаров и для продаж.
 */
public interface DataStorage {

    /**
     * Сохраняет список товаров в хранилище.
     *
     * @param products список косметических товаров
     */
    void saveProducts(List<CosmeticProduct> products);

    /**
     * Сохраняет список продаж в хранилище.
     *
     * @param sales список совершённых продаж
     */
    void saveSales(List<Sale> sales);

    /**
     * Сохраняет список товаров и список продаж в выбранное хранилище.
     *
     * @param products список косметических товаров
     * @param sales    список совершённых продаж
     */
    void save(List<CosmeticProduct> products, List<Sale> sales);

    /**
     * Загружает товары из хранилища.
     *
     * @return список косметических товаров
     */
    List<CosmeticProduct> loadProducts();

    /**
     * Загружает продажи из хранилища.
     *
     * @return список продаж
     */
    List<Sale> loadSales();

    /**
     * Загружает все данные из хранилища.
     *
     * @return объект {@link LoadedData}, содержащий товары и продажи.
     */
    LoadedData load();

    /**
     * Класс-контейнер, используемый для передачи загруженных данных.
     * Хранит списки товаров и продаж, полученные из хранилища.
     */
    class LoadedData {
        /** Загруженный список косметических товаров. */
        public List<CosmeticProduct> products;

        /** Загруженный список продаж. */
        public List<Sale> sales;

        /**
         * Создаёт контейнер загруженных данных.
         *
         * @param products список товаров
         * @param sales    список продаж
         */
        public LoadedData(List<CosmeticProduct> products, List<Sale> sales) {
            this.products = products;
            this.sales = sales;
        }
    }
}
