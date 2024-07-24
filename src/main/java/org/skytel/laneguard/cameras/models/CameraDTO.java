package org.skytel.laneguard.cameras.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CameraDTO {
    private Long id;
    private String ipAddress;
    private String macAddress;
    private Camera.CameraType type;
}
