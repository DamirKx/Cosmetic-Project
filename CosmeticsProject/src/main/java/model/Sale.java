package main.java.model;

import java.time.LocalDateTime;

/**
 * Класс, представляющий запись о продаже товара.
 * Содержит информацию о проданном продукте, количестве
 * и времени совершения продажи.
 */
public class Sale {

    /** Проданный косметический товар. */
    private CosmeticProduct product;

    /** Количество проданных единиц товара. */
    private int quantitySold;

    /** Дата и время продажи. */
    private LocalDateTime dateTime;

    /**
     * Создаёт новую запись о продаже.
     * Время продажи фиксируется автоматически.
     *
     * @param product       товар, который был продан
     * @param quantitySold  количество проданных единиц
     */
    public Sale(CosmeticProduct product, int quantitySold) {
        this.product = product;
        this.quantitySold = quantitySold;
        this.dateTime = LocalDateTime.now();
    }

    /**
     * Рассчитывает общую стоимость продажи.
     *
     * @return сумма продажи (цена × количество)
     */
    public double getTotal() {
        return product.getPrice() * quantitySold;
    }

    public CosmeticProduct getProduct() { return product; }
    public int getQuantitySold() { return quantitySold; }
    public LocalDateTime getDateTime() { return dateTime; }




    /**
     * Возвращает строковое представление продажи,
     * включающее название продукта, количество,
     * итоговую сумму и дату.
     *
     * @return строка с информацией о продаже
     */
    @Override
    public String toString() {
        return "Продажа: " + product.getName() +
                " (" + quantitySold + " шт.) на сумму " +
                getTotal() + " тг. [" + dateTime + "]";
    }
}
