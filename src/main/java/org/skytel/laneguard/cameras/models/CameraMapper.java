package org.skytel.laneguard.cameras.models;

import org.springframework.stereotype.Component;

@Component
public class CameraMapper {
    public CameraDTO toDTO(Camera camera) {
        return new CameraDTO(
                camera.getId(),
                camera.getIpAddress(),
                camera.getMacAddress(),
                camera.getType()
        );
    }

    public Camera toEntity(CameraDTO cameraDTO) {
        return Camera.builder()
                .ipAddress(cameraDTO.getIpAddress())
                .macAddress(cameraDTO.getMacAddress())
                .type(cameraDTO.getType())
                .build();
    }
}

