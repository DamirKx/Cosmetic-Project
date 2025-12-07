package main.java.exceptions;

/**
 * Исключение, выбрасываемое при попытке обращения к товару,
 * который отсутствует в системе.
 * <p>
 * Используется сервисом магазина для сигнализации о том,
 * что запрашиваемый товар не найден по имени или другим параметрам.
 */
public class ProductNotFoundException extends Exception {

    /**
     * Создаёт исключение с указанным текстом сообщения.
     *
     * @param message описание ошибки
     */
    public ProductNotFoundException(String message) {
        super(message);
    }
}
