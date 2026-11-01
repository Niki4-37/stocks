package ru.redcarpet.stocks.model.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
    public ConflictException(String message, Throwable e) {
        super(message, e);
    }
}
