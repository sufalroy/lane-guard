package org.skytel.laneguard.vehicleaccess.controller;

import org.skytel.laneguard.vehicleaccess.models.VehicleAccessLog;
import org.skytel.laneguard.vehicleaccess.models.VehicleAccessLogDTO;
import org.skytel.laneguard.vehicleaccess.services.VehicleAccessLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class VehicleAccessLogController {

    private final VehicleAccessLogService vehicleAccessLogService;

    public VehicleAccessLogController(VehicleAccessLogService vehicleAccessLogService) {
        this.vehicleAccessLogService = vehicleAccessLogService;
    }

    @GetMapping
    public ResponseEntity<Page<VehicleAccessLog>> getVehicleAccessLogs(
            @RequestParam(required = false) String licensePlate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) VehicleAccessLog.AccessStatus status,
            @RequestParam(required = false) String entryGateName,
            @RequestParam(required = false) String exitGateName,
            @RequestParam(required = false) String entryCameraIpAddress,
            @RequestParam(required = false) String exitCameraIpAddress,
            Pageable pageable) {
        Page<VehicleAccessLog> logs = vehicleAccessLogService.getVehicleAccessLogs(
                licensePlate, startTime, endTime, status,
                entryGateName, exitGateName,
                entryCameraIpAddress, exitCameraIpAddress,
                pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleAccessLog> getVehicleAccessLog(@PathVariable String id) {
        return vehicleAccessLogService.getVehicleAccessLog(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<VehicleAccessLogDTO>> getActiveVehicleAccessLogs() {
        return ResponseEntity.ok(vehicleAccessLogService.getActiveVehicleAccessLogs());
    }
}