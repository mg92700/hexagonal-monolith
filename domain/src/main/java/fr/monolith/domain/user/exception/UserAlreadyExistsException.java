package fr.monolith.domain.user.exception;

/**
 * Exception métier — une valeur unique (email, login) est déjà utilisée.
 */
public class UserAlreadyExistsException extends RuntimeException {

    private final String field;
    private final String value;

    public UserAlreadyExistsException(String field, String value) {
        super("User already exists with " + field + ": " + value);
        this.field = field;
        this.value = value;
    }

    public static UserAlreadyExistsException withEmail(String email) {
        return new UserAlreadyExistsException("email", email);
    }

    public static UserAlreadyExistsException withLogin(String login) {
        return new UserAlreadyExistsException("login", login);
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}