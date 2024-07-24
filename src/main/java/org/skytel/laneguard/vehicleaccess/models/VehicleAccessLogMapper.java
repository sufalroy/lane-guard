package org.skytel.laneguard.vehicleaccess.models;

import org.springframework.stereotype.Component;

@Component
public class VehicleAccessLogMapper {

    public VehicleAccessLogDTO toDTO(VehicleAccessLog accessLog) {
        return VehicleAccessLogDTO.builder()
                .id(accessLog.getId())
                .licensePlate(accessLog.getLicensePlate())
                .entryTime(VehicleAccessLogDTO.formatDateTime(accessLog.getEntryTime()))
                .exitTime(VehicleAccessLogDTO.formatDateTime(accessLog.getExitTime()))
                .entryGateName(accessLog.getEntryGate().getName())
                .exitGateName(accessLog.getExitGate() != null ? accessLog.getExitGate().getName() : null)
                .entryCameraIpAddress(accessLog.getEntryCamera().getIpAddress())
                .exitCameraIpAddress(accessLog.getExitCamera() != null ? accessLog.getExitCamera().getIpAddress() : null)
                .status(accessLog.getStatus())
                .build();
    }
}
