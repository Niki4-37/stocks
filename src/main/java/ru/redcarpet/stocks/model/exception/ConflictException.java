package ru.redcarpet.stocks.model.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
