package crudjava.crudjava.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> validationErrors;

    public ValidationErrorResponse(int status, String error, String message, LocalDateTime timestamp, Map<String, String> validationErrors) {
        super(status, error, message, timestamp);
        this.validationErrors = validationErrors;
    }
}
