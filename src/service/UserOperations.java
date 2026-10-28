package service;

import model.User;
import exception.InvalidInputException;

public interface UserOperations {

    void validateRegistration(User user) throws InvalidInputException;

    void validateLogin(String username, String password)
            throws InvalidInputException;
}