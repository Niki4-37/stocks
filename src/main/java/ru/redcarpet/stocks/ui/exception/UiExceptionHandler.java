package ru.redcarpet.stocks.ui.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.redcarpet.stocks.exception.NotFoundException;

@ControllerAdvice(basePackages = "ru.redcarpet.stocks.ui.controller")
public class UiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String onNotFound(NotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }
}
