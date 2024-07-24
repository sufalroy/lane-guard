package org.skytel.laneguard.gates.models;

import org.skytel.laneguard.cameras.models.Camera;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GateMapper {
    public GateDTO toDTO(Gate gate) {
        return new GateDTO(
                gate.getId(),
                gate.getName(),
                Optional.ofNullable(gate.getEntryCamera()).map(Camera::getId).orElse(null),
                Optional.ofNullable(gate.getExitCamera()).map(Camera::getId).orElse(null),
                Optional.ofNullable(gate.getEntryCamera()).map(Camera::getIpAddress).orElse(null),
                Optional.ofNullable(gate.getExitCamera()).map(Camera::getIpAddress).orElse(null)

        );
    }

    public Gate toEntity(GateDTO gateDTO) {
        return Gate.builder()
                .name(gateDTO.getName())
                .build();
    }
}
