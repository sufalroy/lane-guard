package org.skytel.laneguard.vehicleaccess.services;

import org.skytel.laneguard.vehicleaccess.models.VehicleAccessLog;
import org.skytel.laneguard.vehicleaccess.repositores.VehicleAccessLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class VehicleAccessLogService {
    private final VehicleAccessLogRepository vehicleAccessLogRepository;

    public VehicleAccessLogService(VehicleAccessLogRepository vehicleAccessLogRepository) {
        this.vehicleAccessLogRepository = vehicleAccessLogRepository;
    }

    public Page<VehicleAccessLog> getVehicleAccessLogs(
            String licensePlate,
            LocalDateTime startTime,
            LocalDateTime endTime,
            VehicleAccessLog.AccessStatus status,
            String entryGateName,
            String exitGateName,
            String entryCameraIpAddress,
            String exitCameraIpAddress,
            Pageable pageable) {

        Specification<VehicleAccessLog> spec = Stream.of(
                        createLicensePlateSpecification(licensePlate),
                        createGreaterThanOrEqualSpecification("entryTime", startTime),
                        createLessThanOrEqualSpecification("entryTime", endTime),
                        createEqualSpecification("status", status),
                        createNestedEqualSpecification("entryGate", "name", entryGateName),
                        createNestedEqualSpecification("exitGate", "name", exitGateName),
                        createNestedEqualSpecification("entryCamera", "ipAddress", entryCameraIpAddress),
                        createNestedEqualSpecification("exitCamera", "ipAddress", exitCameraIpAddress)
                )
                .flatMap(Optional::stream)
                .reduce(Specification.where(null), Specification::and);

        return vehicleAccessLogRepository.findAll(spec, pageable);
    }

    public Optional<VehicleAccessLog> getVehicleAccessLog(String id) {
        return vehicleAccessLogRepository.findById(id);
    }

    private Optional<Specification<VehicleAccessLog>> createLicensePlateSpecification(String licensePlate) {
        return Optional.ofNullable(licensePlate)
                .map(v -> (Specification<VehicleAccessLog>) (root, query, cb) ->
                        cb.like(cb.lower(root.get("licensePlate")), "%" + v.toLowerCase() + "%"));
    }

    private <T> Optional<Specification<VehicleAccessLog>> createEqualSpecification(String attribute, T value) {
        return Optional.ofNullable(value)
                .map(v -> (Specification<VehicleAccessLog>) (root, query, cb) -> cb.equal(root.get(attribute), v));
    }

    private Optional<Specification<VehicleAccessLog>> createGreaterThanOrEqualSpecification(String attribute, LocalDateTime value) {
        return Optional.ofNullable(value)
                .map(v -> (Specification<VehicleAccessLog>) (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(attribute), v));
    }

    private Optional<Specification<VehicleAccessLog>> createLessThanOrEqualSpecification(String attribute, LocalDateTime value) {
        return Optional.ofNullable(value)
                .map(v -> (Specification<VehicleAccessLog>) (root, query, cb) -> cb.lessThanOrEqualTo(root.get(attribute), v));
    }

    private Optional<Specification<VehicleAccessLog>> createNestedEqualSpecification(String nestedAttribute, String subAttribute, String value) {
        return Optional.ofNullable(value)
                .map(v -> (Specification<VehicleAccessLog>) (root, query, cb) ->
                        cb.equal(root.get(nestedAttribute).get(subAttribute), v));
    }
}