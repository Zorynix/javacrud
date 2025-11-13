package crudjava.crudjava.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(
        CustomerNotFoundException ex
    ) {
        log.warn("Customer not found: {}", ex.getMessage());
        return createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Клиент не найден",
            ex.getMessage()
        );
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
        ProductNotFoundException ex
    ) {
        log.warn("Product not found: {}", ex.getMessage());
        return createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Товар не найден",
            ex.getMessage()
        );
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(
        OrderNotFoundException ex
    ) {
        log.warn("Order not found: {}", ex.getMessage());
        return createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Заказ не найден",
            ex.getMessage()
        );
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
        DuplicateEmailException ex
    ) {
        log.warn("Duplicate email attempt: {}", ex.getMessage());
        return createErrorResponse(
            HttpStatus.CONFLICT,
            "Email уже используется",
            ex.getMessage()
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
        InsufficientStockException ex
    ) {
        log.warn("Insufficient stock: {}", ex.getMessage());
        return createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Недостаточно товара на складе",
            ex.getMessage()
        );
    }

    @ExceptionHandler(CustomerHasOrdersException.class)
    public ResponseEntity<ErrorResponse> handleCustomerHasOrders(
        CustomerHasOrdersException ex
    ) {
        log.warn("Cannot delete customer with orders: {}", ex.getMessage());
        return createErrorResponse(
            HttpStatus.CONFLICT,
            "Невозможно удалить клиента с заказами",
            ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
        MethodArgumentNotValidException ex
    ) {
        log.warn("Validation error: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex
            .getBindingResult()
            .getAllErrors()
            .forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Ошибка валидации данных",
            "Проверьте правильность введенных данных",
            LocalDateTime.now(),
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            errorResponse
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex
    ) {
        log.warn("Type mismatch error: {}", ex.getMessage());
        return createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Неверный тип параметра",
            "Параметр '" + ex.getName() + "' имеет неверный формат"
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
        IllegalArgumentException ex
    ) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Неверные данные запроса",
            ex.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Внутренняя ошибка сервера",
            "Произошла непредвиденная ошибка. Пожалуйста, обратитесь в службу поддержки"
        );
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(
        HttpStatus status,
        String title,
        String message
    ) {
        ErrorResponse error = new ErrorResponse(
            status.value(),
            title,
            message,
            LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(error);
    }
}
