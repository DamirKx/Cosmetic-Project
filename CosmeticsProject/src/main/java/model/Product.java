package main.java.model;

/**
 * Базовый класс, представляющий товар.
 * Содержит основную информацию: идентификатор, название,
 * цену и количество на складе.
 * <p>
 * Реализует интерфейс {@link Sellable}, что позволяет
 * использовать объект в операциях продажи.
 */
public class Product implements Sellable {

    /** Уникальный идентификатор товара. */
    private int id;

    /** Название товара. */
    private String name;

    /** Цена товара. */
    private double price;

    /** Количество товара на складе. */
    private int quantity;

    /**
     * Создаёт новый объект товара.
     *
     * @param id        уникальный ID товара
     * @param name      название
     * @param price     цена
     * @param quantity  доступное количество
     */
    public Product(int id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    /**
     * Возвращает идентификатор товара.
     *
     * @return ID товара
     */
    public int getId() {
        return id;
    }

    /**
     * Возвращает название товара.
     *
     * @return название
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает новое название товара.
     *
     * @param name новое название
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Устанавливает новую цену товара.
     *
     * @param price новая цена
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Возвращает цену товара.
     *
     * @return цена
     */
    public double getPrice() {
        return price;
    }

    /**
     * Возвращает текущее количество товара на складе.
     *
     * @return количество
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Устанавливает количество товара.
     *
     * @param quantity новое количество
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Уменьшает количество товара на складе.
     *
     * @param amount значение, на которое нужно уменьшить
     */
    public void decreaseQuantity(int amount) {
        if (amount <= quantity) {
            quantity -= amount;
        } else {
            System.out.println("Недостаточно товара на складе");
        }
    }

    /**
     * Возвращает краткое текстовое описание товара.
     *
     * @return строка-описание
     */
    public String getDescription() {
        return name + " (" + price + " тг.)";
    }

    /**
     * Возвращает строковое представление товара.
     *
     * @return строка с информацией о товаре
     */
    @Override
    public String toString() {
        return String.format(
                "%d | %s | %.2f тг. | Остаток: %d",
                id, name, price, quantity
        );
    }
}
