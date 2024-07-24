package org.skytel.laneguard.vehicleaccess.models;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleAccessLogDTO implements Serializable {
    private String id;
    private String licensePlate;
    private String entryTime;
    private String exitTime;
    private String entryGateName;
    private String exitGateName;
    private String entryCameraIpAddress;
    private String exitCameraIpAddress;
    private VehicleAccessLog.AccessStatus status;

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
