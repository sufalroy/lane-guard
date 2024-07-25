package org.skytel.laneguard.vehicleaccess.repositores;

import org.skytel.laneguard.vehicleaccess.models.VehicleAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface VehicleAccessLogRepository extends JpaRepository<VehicleAccessLog, String>, JpaSpecificationExecutor<VehicleAccessLog> {
    Optional<VehicleAccessLog> findTopByLicensePlateAndStatusOrderByEntryTimeDesc(String licensePlate, VehicleAccessLog.AccessStatus accessStatus);
    List<VehicleAccessLog> findVehicleAccessLogByStatus(VehicleAccessLog.AccessStatus accessStatus);
}