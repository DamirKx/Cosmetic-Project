package main.java.model;

/**
 * Класс, представляющий косметический товар.
 * Наследует общие свойства продукта из {@link Product}
 * и добавляет характеристику бренда и категории.
 * <p>
 * Реализует интерфейс {@link Sellable}, что позволяет
 * использовать объект в операциях продажи.
 */
public class CosmeticProduct extends Product implements Sellable {

    /** Бренд косметического продукта. */
    private String brand;

    /** Категория товара. */
    private String category;

    /**
     * Создаёт новый косметический продукт.
     *
     * @param id        уникальный идентификатор товара
     * @param name      название товара
     * @param price     цена
     * @param quantity  количество на складе
     * @param brand     бренд
     * @param category  категория товара
     */
    public CosmeticProduct(int id, String name, double price, int quantity,
                           String brand, String category) {
        super(id, name, price, quantity);
        this.brand = brand;
        this.category = category;
    }

    /** Возвращает бренд товара. */
    public String getBrand() {
        return brand;
    }

    /** Устанавливает новый бренд товара. */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /** Возвращает категорию товара. */
    public String getCategory() {
        return category;
    }

    /** Устанавливает новую категорию товара. */
    public void setCategory(String category) {
        this.category = category;
    }

    /** Расширенное описание продукта. */
    @Override
    public String getDescription() {
        return getName() + " | " + brand + " | категория: " + category;
    }

    /** Полное строковое представление. */
    @Override
    public String toString() {
        return super.toString() + " | " + brand + " | Категория: " + category;
    }
}
