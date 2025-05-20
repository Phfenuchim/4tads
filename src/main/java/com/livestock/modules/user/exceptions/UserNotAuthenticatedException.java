package com.livestock.modules.user.exceptions;

public class UserNotAuthenticatedException extends RuntimeException {
    public UserNotAuthenticatedException(String errorMessage) {
        super(errorMessage);
    }
}
