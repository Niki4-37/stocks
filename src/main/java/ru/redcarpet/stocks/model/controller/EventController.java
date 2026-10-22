package ru.redcarpet.stocks.model.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.model.dto.CreateEventRequest;
import ru.redcarpet.stocks.model.service.EventService;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody CreateEventRequest request) {
        eventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("New event created!");
    }
}
