package org.skytel.laneguard.events.anpr.requests;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Getter
@XmlRootElement(name = "EventNotificationAlert", namespace = "http://www.isapi.org/ver20/XMLSchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventNotificationAlert {

    @XmlElement(name = "ipAddress", namespace = "http://www.isapi.org/ver20/XMLSchema")
    private String ipAddress;

    @XmlElement(name = "macAddress", namespace = "http://www.isapi.org/ver20/XMLSchema")
    private String macAddress;

    @XmlElement(name = "eventType", namespace = "http://www.isapi.org/ver20/XMLSchema")
    private String eventType;

    @XmlElement(name = "ANPR", namespace = "http://www.isapi.org/ver20/XMLSchema")
    private Anpr anpr;

    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Anpr {
        @XmlElement(name = "licensePlate", namespace = "http://www.isapi.org/ver20/XMLSchema")
        private String licensePlate;

        @XmlElement(name = "direction", namespace = "http://www.isapi.org/ver20/XMLSchema")
        private String direction;
    }

    public static Optional<EventNotificationAlert> parseEventNotificationAlert(MultipartFile xmlFile) {
        try {
            var jaxbContext = JAXBContext.newInstance(EventNotificationAlert.class);
            var unmarshaller = jaxbContext.createUnmarshaller();
            return Optional.of((EventNotificationAlert) unmarshaller.unmarshal(xmlFile.getInputStream()));
        } catch (IOException | JAXBException e) {
            log.error("Error parsing XML file: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public boolean isValidAnprEvent() {
        return Optional.ofNullable(this.anpr)
                .filter(anpr -> "ANPR".equals(this.eventType)
                        && "forward".equals(this.anpr.direction)
                        && !"unknown".equals(this.anpr.licensePlate))
                .isPresent();
    }
}
