package ru.redcarpet.stocks.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
