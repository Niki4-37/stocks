package ru.redcarpet.stocks.model.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
