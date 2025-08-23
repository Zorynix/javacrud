package crudjava.crudjava.exception;

public class CustomerHasOrdersException extends RuntimeException {
    public CustomerHasOrdersException(Long customerId) {
        super("Нельзя удалить клиента с ID " + customerId + " так как у него есть заказы");
    }
    
    public CustomerHasOrdersException(String message) {
        super(message);
    }
}
