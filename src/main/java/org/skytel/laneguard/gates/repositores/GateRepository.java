package org.skytel.laneguard.gates.repositores;

import org.skytel.laneguard.cameras.models.Camera;
import org.skytel.laneguard.gates.models.Gate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GateRepository extends JpaRepository<Gate, Long> {
    Optional<Gate> findByEntryCameraOrExitCamera(Camera entryCamera, Camera exitCamera);
    boolean existsByName(String name);
    boolean existsByEntryCamera(Camera camera);
    boolean existsByExitCamera(Camera camera);
    Optional<Gate> findByEntryCamera(Camera camera);
    Optional<Gate> findByExitCamera(Camera camera);
    List<Gate> findAllByOrderByIdAsc();
}

