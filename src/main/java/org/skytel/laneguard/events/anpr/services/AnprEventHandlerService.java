package org.skytel.laneguard.events.anpr.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.skytel.laneguard.alert.AlertConsumer;
import org.skytel.laneguard.alert.AlertType;
import org.skytel.laneguard.cameras.models.Camera;
import org.skytel.laneguard.gates.models.Gate;
import org.skytel.laneguard.vehicleaccess.models.VehicleAccessLog;
import org.skytel.laneguard.cameras.repositories.CameraRepository;
import org.skytel.laneguard.gates.repositores.GateRepository;
import org.skytel.laneguard.vehicleaccess.repositores.VehicleAccessLogRepository;
import org.skytel.laneguard.events.anpr.requests.EventNotificationAlert;
import org.skytel.laneguard.vehicleaccess.services.VehicleAccessLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnprEventHandlerService {

    private final AlertConsumer alertConsumer;
    private final GateRepository gateRepository;
    private final CameraRepository cameraRepository;
    private final VehicleAccessLogService vehicleAccessLogService;
    private final VehicleAccessLogRepository vehicleAccessLogRepository;

    @Transactional
    public void handleAnprEvent(EventNotificationAlert event) {
        cameraRepository.findByIpAddressAndMacAddress(event.getIpAddress(), event.getMacAddress())
                .flatMap(camera -> gateRepository.findByEntryCameraOrExitCamera(camera, camera)
                        .map(gate -> new CameraGatePair(camera, gate)))
                .ifPresentOrElse(
                        pair -> processAnprEvent(event.getAnpr(), pair.camera(), pair.gate()),
                        () -> log.warn("Camera or Gate not found for event: {}", event)
                );
    }

    private void processAnprEvent(EventNotificationAlert.Anpr anpr, Camera camera, Gate gate) {
        String licensePlate = anpr.getLicensePlate();
        if (camera.getType() == Camera.CameraType.ENTRY) {
            handleEntryEvent(licensePlate, gate, camera);
        } else {
            handleExitEvent(licensePlate, gate, camera);
        }
    }

    private void handleEntryEvent(String licensePlate, Gate gate, Camera camera) {
        vehicleAccessLogRepository
                .findTopByLicensePlateAndStatusOrderByEntryTimeDesc(licensePlate, VehicleAccessLog.AccessStatus.IN_PROGRESS)
                .ifPresentOrElse(
                        accessLog -> {
                            String alertMessage = String.format("Vehicle %s attempted to re-enter through gate %s while previous entry is still active", licensePlate, gate.getName());
                            log.warn(alertMessage);
                        },
                        () -> createNewEntryLog(licensePlate, gate, camera)
                );
    }

    private void createNewEntryLog(String licensePlate, Gate gate, Camera camera) {
        VehicleAccessLog newLog = VehicleAccessLog.builder()
                .licensePlate(licensePlate)
                .entryTime(LocalDateTime.now())
                .entryGate(gate)
                .entryCamera(camera)
                .status(VehicleAccessLog.AccessStatus.IN_PROGRESS)
                .build();
        vehicleAccessLogRepository.save(newLog);
        vehicleAccessLogService.sendActiveVehicleAccessLogs();
        String alertMessage = String.format("Vehicle %s entered through gate %s", licensePlate, gate.getName());
        vehicleAccessLogService.sendVehicleAccessAlert(alertMessage, AlertType.SUCCESS);
        log.info(alertMessage);
    }

    private void handleExitEvent(String licensePlate, Gate exitGate, Camera exitCamera) {
        vehicleAccessLogRepository
                .findTopByLicensePlateAndStatusOrderByEntryTimeDesc(licensePlate, VehicleAccessLog.AccessStatus.IN_PROGRESS)
                .ifPresentOrElse(
                        accessLog -> completeOrLogAnomaly(accessLog, exitGate, exitCamera),
                        () -> {
                            String alertMessage = String.format("Vehicle %s attempting to exit without a recorded entry at gate %s", licensePlate, exitGate.getName());
                            vehicleAccessLogService.sendVehicleAccessAlert(alertMessage, AlertType.FAILURE);
                            log.warn(alertMessage);
                        }
                );
    }

    private void completeOrLogAnomaly(VehicleAccessLog accessLog, Gate exitGate, Camera exitCamera) {
        if (accessLog.getEntryGate().equals(exitGate)) {
            handleCompleteAccess(accessLog, exitGate, exitCamera);
        } else {
            handleExitAnomaly(accessLog, exitGate);
        }
    }

    private void handleExitAnomaly(VehicleAccessLog accessLog, Gate exitGate) {
        String licensePlate = accessLog.getLicensePlate();
        String entryGate = accessLog.getEntryGate().getName();
        String exitGateName = exitGate.getName();

        String alertMessage = String.format("Vehicle %s attempted to exit from gate %s instead of entry gate %s", licensePlate, exitGateName, entryGate);
        vehicleAccessLogService.sendVehicleAccessAlert(alertMessage, AlertType.FAILURE);
        log.warn(alertMessage);
        alertConsumer.triggerAlert();
    }

    private void handleCompleteAccess(VehicleAccessLog accessLog, Gate exitGate, Camera exitCamera) {
        accessLog.setStatus(VehicleAccessLog.AccessStatus.COMPLETED);
        accessLog.setExitGate(exitGate);
        accessLog.setExitCamera(exitCamera);
        accessLog.setExitTime(LocalDateTime.now());
        vehicleAccessLogRepository.save(accessLog);
        String alertMessage = String.format("Vehicle %s exited through gate %s", accessLog.getLicensePlate(), exitGate.getName());
        vehicleAccessLogService.sendVehicleAccessAlert(alertMessage, AlertType.SUCCESS);
        log.info(alertMessage);
        vehicleAccessLogService.sendActiveVehicleAccessLogs();
    }

    private record CameraGatePair(Camera camera, Gate gate) {
    }
}
