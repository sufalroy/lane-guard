package org.skytel.laneguard.events.anpr.controllers;

import lombok.extern.slf4j.Slf4j;
import org.skytel.laneguard.events.anpr.requests.EventNotificationAlert;
import org.skytel.laneguard.events.anpr.services.AnprEventHandlerService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("event")
public class AnprEventHandlerController {

    private final AnprEventHandlerService anprEventHandlerService;

    public AnprEventHandlerController(AnprEventHandlerService anprEventHandlerService) {
        this.anprEventHandlerService = anprEventHandlerService;
    }

    @PostMapping(value = "/anpr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> handleAnprEvent(@RequestPart("EventNotificationAlert") MultipartFile xmlFile) {
        EventNotificationAlert.parseEventNotificationAlert(xmlFile)
                .filter(EventNotificationAlert::isValidAnprEvent)
                .ifPresentOrElse(
                        anprEventHandlerService::handleAnprEvent,
                        () -> log.warn("Invalid ANPR event or parsing failed")
                );

        return ResponseEntity.ok().build();
    }
}
