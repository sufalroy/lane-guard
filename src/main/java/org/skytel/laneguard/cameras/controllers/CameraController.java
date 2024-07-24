package org.skytel.laneguard.cameras.controllers;

import org.skytel.laneguard.cameras.models.Camera;
import org.skytel.laneguard.cameras.models.CameraDTO;
import org.skytel.laneguard.cameras.services.CameraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cameras")
public class CameraController {

    private final CameraService cameraService;

    public CameraController(CameraService cameraService) {
        this.cameraService = cameraService;
    }

    @PostMapping
    public ResponseEntity<CameraDTO> createCamera(@RequestBody CameraDTO cameraDTO) {
        return ResponseEntity.ok(cameraService.createCamera(cameraDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CameraDTO> getCamera(@PathVariable Long id) {
        return cameraService.getCamera(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CameraDTO>> getAllCameras() {
        return ResponseEntity.ok(cameraService.getAllCameras());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CameraDTO> updateCamera(@PathVariable Long id, @RequestBody CameraDTO cameraDTO) {
        return cameraService.updateCamera(id, cameraDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCamera(@PathVariable Long id) {
        return cameraService.deleteCamera(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<CameraDTO>> getCamerasByTypeAndAvailability(
            @RequestParam Camera.CameraType type,
            @RequestParam(required = false) Boolean available) {
        List<CameraDTO> cameras = cameraService.getCamerasByTypeAndAvailability(type, available);
        return ResponseEntity.ok(cameras);
    }
}
