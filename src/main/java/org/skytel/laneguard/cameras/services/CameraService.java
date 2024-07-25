package org.skytel.laneguard.cameras.services;

import org.skytel.laneguard.exceptions.ConflictException;
import org.skytel.laneguard.cameras.models.CameraMapper;
import org.skytel.laneguard.cameras.models.Camera;
import org.skytel.laneguard.cameras.models.CameraDTO;
import org.skytel.laneguard.cameras.repositories.CameraRepository;
import org.skytel.laneguard.gates.services.GateService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class CameraService {

    private final CameraRepository cameraRepository;
    private final CameraMapper cameraMapper;
    private final GateService gateService;

    public CameraService(CameraRepository cameraRepository, CameraMapper cameraMapper, GateService gateService) {
        this.cameraRepository = cameraRepository;
        this.cameraMapper = cameraMapper;
        this.gateService = gateService;
    }

    public CameraDTO createCamera(CameraDTO cameraDTO) {
        return Optional.of(cameraDTO)
                .filter(dto -> validateCameraUniqueness(null, dto))
                .map(cameraMapper::toEntity)
                .map(cameraRepository::save)
                .map(cameraMapper::toDTO)
                .orElseThrow(() -> new ConflictException("Camera with the same IP address or MAC address already exists."));
    }

    public Optional<CameraDTO> getCamera(Long id) {
        return cameraRepository.findById(id).map(cameraMapper::toDTO);
    }

    public List<CameraDTO> getAllCameras() {
        return cameraRepository.findAllByOrderByIdAsc().stream()
                .map(cameraMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<CameraDTO> updateCamera(Long id, CameraDTO cameraDTO) {
        return cameraRepository.findById(id)
                .filter(camera -> validateCameraUniqueness(id, cameraDTO))
                .map(camera -> updateCameraFields(camera, cameraDTO))
                .map(cameraRepository::save)
                .map(cameraMapper::toDTO);
    }

    public boolean deleteCamera(Long id) {
        return cameraRepository.findById(id)
                .map(camera -> {
                    cameraRepository.delete(camera);
                    return true;
                })
                .orElse(false);
    }

    private boolean validateCameraUniqueness(Long id, CameraDTO cameraDTO) {
        Supplier<Boolean> isIpUnique = () ->
                cameraRepository.findByIpAddress(cameraDTO.getIpAddress())
                        .map(Camera::getId)
                        .filter(foundId -> !foundId.equals(id))
                        .isEmpty();

        Supplier<Boolean> isMacUnique = () ->
                cameraRepository.findByMacAddress(cameraDTO.getMacAddress())
                        .map(Camera::getId)
                        .filter(foundId -> !foundId.equals(id))
                        .isEmpty();

        return isIpUnique.get() && isMacUnique.get();
    }

    private Camera updateCameraFields(Camera camera, CameraDTO cameraDTO) {
        camera.setIpAddress(cameraDTO.getIpAddress());
        camera.setMacAddress(cameraDTO.getMacAddress());
        camera.setType(cameraDTO.getType());
        return camera;
    }

    public List<CameraDTO> getCamerasByTypeAndAvailability(Camera.CameraType type, Boolean available) {
        List<Camera> cameras = cameraRepository.findByType(type);

        if (available == null) {
            return cameras.stream()
                    .map(cameraMapper::toDTO)
                    .collect(Collectors.toList());
        }

        Set<Long> assignedCameraIds = gateService.getAssignedCameraIds(type);

        return cameras.stream()
                .filter(camera -> available == !assignedCameraIds.contains(camera.getId()))
                .map(cameraMapper::toDTO)
                .collect(Collectors.toList());
    }
}