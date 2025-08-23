package crudjava.crudjava.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String productName, int requested, int available) {
        super("Недостаточно товара '" + productName + "' на складе. Запрошено: " + requested + ", доступно: " + available);
    }
}
