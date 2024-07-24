package org.skytel.laneguard.gates.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GateDTO {
    private Long id;
    private String name;
    private Long entryCameraId;
    private Long exitCameraId;
    private String entryCameraIpAddress;
    private String exitCameraIpAddress;
}
