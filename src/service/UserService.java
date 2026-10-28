package service;

import model.User;
import exception.InvalidInputException;

import java.util.regex.Pattern;

public class UserService implements UserOperations {

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("[A-Za-z0-9._-]{3,50}");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Override
    public void validateRegistration(User user) throws InvalidInputException {
        if (user == null || !user.isValid()) {
            throw new InvalidInputException("required_fields");
        }
        if (!USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
            throw new InvalidInputException("invalid_username");
        }
        if (user.getEmail().length() > 254
                || !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new InvalidInputException("invalid_email");
        }
        if (user.getPassword().length() < 8
                || user.getPassword().length() > 128) {
            throw new InvalidInputException("invalid_password");
        }
    }

    @Override
    public void validateLogin(String username, String password)
            throws InvalidInputException {
        if (username == null || username.isBlank()) {
            throw new InvalidInputException("required_fields");
        }
        if (password == null || password.isBlank()) {
            throw new InvalidInputException("required_fields");
        }
    }

    public void validateExpenseAmount(double amount)
            throws InvalidInputException {

        if (amount <= 0) {
            throw new InvalidInputException(
                    "Expense amount must be greater than zero."
            );
        }
    }
}