package crudjava.crudjava.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("Клиент с email '" + email + "' уже существует");
    }
}
