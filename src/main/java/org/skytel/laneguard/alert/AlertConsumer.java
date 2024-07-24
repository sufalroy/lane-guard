package org.skytel.laneguard.alert;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class AlertConsumer {

    private final RestTemplate restTemplate;
    private static final String ALERT_ENDPOINT = "http://192.168.1.177/?status=1";

    public AlertConsumer(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean triggerAlert() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    ALERT_ENDPOINT,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Void.class
            );

            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpStatusCodeException ex) {
            log.error("HTTP error: {}", ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("Unexpected error: {}", ex.getMessage());
        }
        return false;
    }
}
