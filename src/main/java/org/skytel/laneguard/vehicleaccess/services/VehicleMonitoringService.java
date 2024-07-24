package org.skytel.laneguard.vehicleaccess.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.skytel.laneguard.alert.AlertMessage;
import org.skytel.laneguard.vehicleaccess.models.VehicleAccessLog;
import org.skytel.laneguard.vehicleaccess.models.VehicleAccessLogDTO;
import org.skytel.laneguard.vehicleaccess.models.VehicleAccessLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VehicleMonitoringService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private VehicleAccessLogMapper vehicleAccessLogMapper;

    private final Map<String, VehicleAccessLog> activeMonitoringLogs = new ConcurrentHashMap<>();

    public void startMonitoring(VehicleAccessLog log) {
        activeMonitoringLogs.put(log.getId(), log);
        sendLogUpdate();
    }

    public void updateMonitoring(VehicleAccessLog log) {
        if (activeMonitoringLogs.containsKey(log.getId())) {
            activeMonitoringLogs.put(log.getId(), log);
            sendLogUpdate();
        }
    }

    public void stopMonitoring(String logId) {
        VehicleAccessLog log = activeMonitoringLogs.remove(logId);
        if (log != null) {
            sendLogUpdate();
        }
    }

    public List<VehicleAccessLogDTO> getActiveMonitoringLogs() {
        return activeMonitoringLogs.values().stream()
                .map(vehicleAccessLogMapper::toDTO)
                .collect(Collectors.toList());
    }

    private void sendLogUpdate() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<VehicleAccessLogDTO> logDTOs = activeMonitoringLogs.values().stream()
                    .map(vehicleAccessLogMapper::toDTO)
                    .collect(Collectors.toList());
            String jsonMessage = objectMapper.writeValueAsString(logDTOs);
            log.info("Sending WebSocket message: {}", jsonMessage);
            messagingTemplate.convertAndSend("/topic/monitor", jsonMessage);
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize VehicleAccessLogDTOs", ex);
            messagingTemplate.convertAndSend("/topic/monitor", "[]");
        }
    }

    public void sendAlert(String message) {
        AlertMessage alert = new AlertMessage("VEHICLE_ALERT", message, LocalDateTime.now().toString());
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(alert);
            log.info("Sending WebSocket message: {}", jsonMessage);
            messagingTemplate.convertAndSend("/topic/alerts", jsonMessage);
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize AlertMessage", ex);
            messagingTemplate.convertAndSend("/topic/alerts", "{}");
        }
    }
}