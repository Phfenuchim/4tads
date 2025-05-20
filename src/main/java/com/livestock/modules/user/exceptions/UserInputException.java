package com.livestock.modules.user.exceptions;

public class UserInputException extends RuntimeException {
    public UserInputException(String errorMessage) {
        super(errorMessage);
    }
}
