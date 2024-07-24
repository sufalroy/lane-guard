package org.skytel.laneguard.events.anpr.services;

import lombok.extern.slf4j.Slf4j;
import org.skytel.laneguard.alert.AlertConsumer;
import org.skytel.laneguard.cameras.models.Camera;
import org.skytel.laneguard.gates.models.Gate;
import org.skytel.laneguard.vehicleaccess.models.VehicleAccessLog;
import org.skytel.laneguard.cameras.repositories.CameraRepository;
import org.skytel.laneguard.gates.repositores.GateRepository;
import org.skytel.laneguard.vehicleaccess.repositores.VehicleAccessLogRepository;
import org.skytel.laneguard.events.anpr.requests.EventNotificationAlert;
import org.skytel.laneguard.vehicleaccess.services.VehicleMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AnprEventHandlerService {

    @Autowired
    VehicleAccessLogRepository vehicleAccessLogRepository;

    @Autowired
    GateRepository gateRepository;

    @Autowired
    CameraRepository cameraRepository;

    @Autowired
    AlertConsumer alertConsumer;

    @Autowired
    private VehicleMonitoringService monitoringService;

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
        VehicleAccessLog savedLog = vehicleAccessLogRepository.save(newLog);
        monitoringService.startMonitoring(savedLog);
        String alertMessage = String.format("Vehicle %s entered through gate %s", licensePlate, gate.getName());
        monitoringService.sendAlert(alertMessage);
        log.info(alertMessage);
    }

    private void handleExitEvent(String licensePlate, Gate exitGate, Camera exitCamera) {
        vehicleAccessLogRepository
                .findTopByLicensePlateAndStatusOrderByEntryTimeDesc(licensePlate, VehicleAccessLog.AccessStatus.IN_PROGRESS)
                .ifPresentOrElse(
                        accessLog -> completeOrLogAnomaly(accessLog, exitGate, exitCamera),
                        () -> {
                            String alertMessage = String.format("Vehicle %s attempting to exit without a recorded entry at gate %s", licensePlate, exitGate.getName());
                            monitoringService.sendAlert(alertMessage);
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
        monitoringService.sendAlert(alertMessage);
        log.warn(alertMessage);
        alertConsumer.triggerAlert();
    }

    private void handleCompleteAccess(VehicleAccessLog accessLog, Gate exitGate, Camera exitCamera) {
        accessLog.setStatus(VehicleAccessLog.AccessStatus.COMPLETED);
        accessLog.setExitGate(exitGate);
        accessLog.setExitCamera(exitCamera);
        accessLog.setExitTime(LocalDateTime.now());
        VehicleAccessLog updatedLog = vehicleAccessLogRepository.save(accessLog);
        monitoringService.updateMonitoring(updatedLog);
        String alertMessage = String.format("Vehicle %s exited through gate %s", accessLog.getLicensePlate(), exitGate.getName());
        monitoringService.sendAlert(alertMessage);
        log.info(alertMessage);
        monitoringService.stopMonitoring(updatedLog.getId());
    }

    private record CameraGatePair(Camera camera, Gate gate) {
    }
}
