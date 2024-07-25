package org.skytel.laneguard.vehicleaccess.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.skytel.laneguard.alert.AlertMessage;
import org.skytel.laneguard.alert.AlertType;
import org.skytel.laneguard.vehicleaccess.models.VehicleAccessLog;
import org.skytel.laneguard.vehicleaccess.models.VehicleAccessLogDTO;
import org.skytel.laneguard.vehicleaccess.models.VehicleAccessLogMapper;
import org.skytel.laneguard.vehicleaccess.repositores.VehicleAccessLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class VehicleAccessLogService {

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    VehicleAccessLogMapper vehicleAccessLogMapper;

    @Autowired
    VehicleAccessLogRepository vehicleAccessLogRepository;

    private static final String ALERT_TOPIC = "/topic/alerts";
    private static final String MONITOR_TOPIC = "/topic/monitor";

    public Page<VehicleAccessLog> getVehicleAccessLogs(
            String licensePlate,
            LocalDateTime startTime,
            LocalDateTime endTime,
            VehicleAccessLog.AccessStatus status,
            String entryGateName,
            String exitGateName,
            String entryCameraIpAddress,
            String exitCameraIpAddress,
            Pageable pageable) {

        Specification<VehicleAccessLog> spec = Stream.of(
                        createLicensePlateSpecification(licensePlate),
                        createGreaterThanOrEqualSpecification("entryTime", startTime),
                        createLessThanOrEqualSpecification("entryTime", endTime),
                        createEqualSpecification("status", status),
                        createNestedEqualSpecification("entryGate", "name", entryGateName),
                        createNestedEqualSpecification("exitGate", "name", exitGateName),
                        createNestedEqualSpecification("entryCamera", "ipAddress", entryCameraIpAddress),
                        createNestedEqualSpecification("exitCamera", "ipAddress", exitCameraIpAddress)
                )
                .flatMap(Optional::stream)
                .reduce(Specification.where(null), Specification::and);

        return vehicleAccessLogRepository.findAll(spec, pageable);
    }

    public Optional<VehicleAccessLog> getVehicleAccessLog(String id) {
        return vehicleAccessLogRepository.findById(id);
    }

    public List<VehicleAccessLogDTO> getActiveVehicleAccessLogs() {
        return vehicleAccessLogRepository.findVehicleAccessLogByStatus(VehicleAccessLog.AccessStatus.IN_PROGRESS).stream()
                .map(vehicleAccessLogMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void sendActiveVehicleAccessLogs() {
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(
                    vehicleAccessLogRepository.findVehicleAccessLogByStatus(VehicleAccessLog.AccessStatus.IN_PROGRESS).stream()
                            .map(vehicleAccessLogMapper::toDTO)
                            .collect(Collectors.toList())
            );
            log.info("Sending WebSocket logs messages: {}", jsonMessage);
            messagingTemplate.convertAndSend(MONITOR_TOPIC, jsonMessage);
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize VehicleAccessLogDTOs", ex);
            messagingTemplate.convertAndSend(MONITOR_TOPIC, "[]");
        }
    }

    public void sendVehicleAccessAlert(String message, AlertType alertType) {
        AlertMessage alert = new AlertMessage(alertType.toString(), message, LocalDateTime.now().toString());
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(alert);
            log.info("Sending WebSocket alert messages: {}", jsonMessage);
            messagingTemplate.convertAndSend(ALERT_TOPIC, jsonMessage);
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize AlertMessage", ex);
            messagingTemplate.convertAndSend(ALERT_TOPIC, "{}");
        }
    }

    private Optional<Specification<VehicleAccessLog>> createLicensePlateSpecification(String licensePlate) {
        return Optional.ofNullable(licensePlate)
                .map(v -> (Specification<VehicleAccessLog>) (root, query, cb) ->
                        cb.like(cb.lower(root.get("licensePlate")), "%" + v.toLowerCase() + "%"));
    }

    private <T> Optional<Specification<VehicleAccessLog>> createEqualSpecification(String attribute, T value) {
        return Optional.ofNullable(value)
                .map(v -> (Specification<VehicleAccessLog>) (root, query, cb) -> cb.equal(root.get(attribute), v));
    }

    private Optional<Specification<VehicleAccessLog>> createGreaterThanOrEqualSpecification(String attribute, LocalDateTime value) {
        return Optional.ofNullable(value)
                .map(v -> (Specification<VehicleAccessLog>) (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(attribute), v));
    }

    private Optional<Specification<VehicleAccessLog>> createLessThanOrEqualSpecification(String attribute, LocalDateTime value) {
        return Optional.ofNullable(value)
                .map(v -> (Specification<VehicleAccessLog>) (root, query, cb) -> cb.lessThanOrEqualTo(root.get(attribute), v));
    }

    private Optional<Specification<VehicleAccessLog>> createNestedEqualSpecification(String nestedAttribute, String subAttribute, String value) {
        return Optional.ofNullable(value)
                .map(v -> (Specification<VehicleAccessLog>) (root, query, cb) ->
                        cb.equal(root.get(nestedAttribute).get(subAttribute), v));
    }
}