package org.skytel.laneguard.vehicleaccess.controller;


import org.skytel.laneguard.vehicleaccess.models.VehicleAccessLogDTO;
import org.skytel.laneguard.vehicleaccess.services.VehicleMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
public class VehicleMonitoringController {

    private final VehicleMonitoringService monitoringService;

    public VehicleMonitoringController(VehicleMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/active")
    public ResponseEntity<List<VehicleAccessLogDTO>> getActiveMonitoringLogs() {
        return ResponseEntity.ok(monitoringService.getActiveMonitoringLogs());
    }
}
