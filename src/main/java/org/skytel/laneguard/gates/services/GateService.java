package org.skytel.laneguard.gates.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.skytel.laneguard.exceptions.ConflictException;
import org.skytel.laneguard.exceptions.InvalidRequestException;
import org.skytel.laneguard.cameras.models.Camera;
import org.skytel.laneguard.gates.models.Gate;
import org.skytel.laneguard.gates.models.GateDTO;
import org.skytel.laneguard.gates.models.GateMapper;
import org.skytel.laneguard.cameras.repositories.CameraRepository;
import org.skytel.laneguard.gates.repositores.GateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class GateService {

    private final GateRepository gateRepository;
    private final CameraRepository cameraRepository;
    private final GateMapper gateMapper;

    public GateDTO createGate(GateDTO gateDTO) {
        return Optional.of(gateDTO)
                .filter(dto -> !gateRepository.existsByName(dto.getName()))
                .map(dto -> {
                    validateCamera(dto.getEntryCameraId(), Camera.CameraType.ENTRY, "Entry");
                    validateCamera(dto.getExitCameraId(), Camera.CameraType.EXIT, "Exit");
                    return dto;
                })
                .map(this::buildAndSaveGate)
                .orElseThrow(() -> new ConflictException("Gate with the name '" + gateDTO.getName() + "' already exists."));
    }

    public Optional<GateDTO> getGate(Long id) {
        return gateRepository.findById(id).map(gateMapper::toDTO);
    }

    public List<GateDTO> getAllGates() {
        return gateRepository.findAll().stream()
                .map(gateMapper::toDTO)
                .toList();
    }

    public Optional<GateDTO> updateGate(Long id, GateDTO gateDTO) {
        return gateRepository.findById(id)
                .map(gate -> {
                    validateAndPrepareCamera(gateDTO.getEntryCameraId(), Camera.CameraType.ENTRY, "Entry", id);
                    validateAndPrepareCamera(gateDTO.getExitCameraId(), Camera.CameraType.EXIT, "Exit", id);
                    return updateGateFields(gate, gateDTO);
                })
                .map(gateRepository::save)
                .map(gateMapper::toDTO);
    }

    public boolean deleteGate(Long id) {
        return gateRepository.findById(id)
                .map(gate -> {
                    gateRepository.delete(gate);
                    return true;
                })
                .orElse(false);
    }

    public Set<Long> getAssignedCameraIds(Camera.CameraType type) {
        if (type == Camera.CameraType.ENTRY) {
            return gateRepository.findAll().stream()
                    .map(Gate::getEntryCamera)
                    .filter(Objects::nonNull)
                    .map(Camera::getId)
                    .collect(Collectors.toSet());
        } else {
            return gateRepository.findAll().stream()
                    .map(Gate::getExitCamera)
                    .filter(Objects::nonNull)
                    .map(Camera::getId)
                    .collect(Collectors.toSet());
        }
    }

    private void validateAndPrepareCamera(Long cameraId, Camera.CameraType expectedType, String cameraDescription, Long updatingGateId) {
        cameraRepository.findById(cameraId)
                .filter(camera -> camera.getType() == expectedType)
                .ifPresentOrElse(
                        camera -> prepareAndAssignCamera(camera, expectedType, updatingGateId),
                        () -> {
                            throw new InvalidRequestException(cameraDescription + " camera validation failed");
                        }
                );
    }

    private void prepareAndAssignCamera(Camera camera, Camera.CameraType type, Long updatingGateId) {
        Optional<Gate> currentGateOpt = (type == Camera.CameraType.ENTRY)
                ? gateRepository.findByEntryCamera(camera)
                : gateRepository.findByExitCamera(camera);

        currentGateOpt.ifPresent(currentGate -> {
            if (!currentGate.getId().equals(updatingGateId)) {
                if (type == Camera.CameraType.ENTRY) {
                    currentGate.setEntryCamera(null);
                } else {
                    currentGate.setExitCamera(null);
                }
                gateRepository.save(currentGate);
            }
        });
    }

    private void validateCamera(Long cameraId, Camera.CameraType expectedType, String cameraDescription) {
        cameraRepository.findById(cameraId)
                .filter(camera -> camera.getType() == expectedType)
                .filter(camera -> !isCameraAssigned(camera, expectedType))
                .orElseThrow(() -> new InvalidRequestException(cameraDescription + " camera validation failed"));
    }

    private boolean isCameraAssigned(Camera camera, Camera.CameraType type) {
        return type == Camera.CameraType.ENTRY
                ? gateRepository.existsByEntryCamera(camera)
                : gateRepository.existsByExitCamera(camera);
    }

    private GateDTO buildAndSaveGate(GateDTO gateDTO) {
        Gate gate = gateMapper.toEntity(gateDTO);
        setCameras(gate, gateDTO);
        return gateMapper.toDTO(gateRepository.save(gate));
    }

    private Gate updateGateFields(Gate gate, GateDTO gateDTO) {
        gate.setName(gateDTO.getName());
        setCameras(gate, gateDTO);
        return gate;
    }

    private void setCameras(Gate gate, GateDTO gateDTO) {
        gate.setEntryCamera(cameraRepository.findCameraByIdAndType(gateDTO.getEntryCameraId(), Camera.CameraType.ENTRY).orElse(null));
        gate.setExitCamera(cameraRepository.findCameraByIdAndType(gateDTO.getExitCameraId(), Camera.CameraType.EXIT).orElse(null));
    }
}