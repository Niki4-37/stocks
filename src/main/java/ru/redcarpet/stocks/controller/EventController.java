package ru.redcarpet.stocks.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.dto.CreateEventRequest;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody CreateEventRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body("New event created!");
    }
}
