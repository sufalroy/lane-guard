package org.skytel.laneguard.cameras.repositories;

import org.skytel.laneguard.cameras.models.Camera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CameraRepository extends JpaRepository<Camera, Long> {
    Optional<Camera> findByIpAddressAndMacAddress(String ipAddress, String macAddress);
    Optional<Camera> findByIpAddress(String ipAddress);
    Optional<Camera> findByMacAddress(String macAddress);
    Optional<Camera> findCameraByIdAndType(Long id, Camera.CameraType type);
    List<Camera> findByType(Camera.CameraType type);
    List<Camera> findAllByOrderByIdAsc();
}
