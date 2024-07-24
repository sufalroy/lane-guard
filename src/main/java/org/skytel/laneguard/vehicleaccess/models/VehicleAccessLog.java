package org.skytel.laneguard.vehicleaccess.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.skytel.laneguard.cameras.models.Camera;
import org.skytel.laneguard.gates.models.Gate;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_access_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleAccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String licensePlate;

    @Column(nullable = false)
    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    @ManyToOne
    @JoinColumn(name = "entry_gate_id", nullable = false)
    private Gate entryGate;

    @ManyToOne
    @JoinColumn(name = "exit_gate_id")
    private Gate exitGate;

    @ManyToOne
    @JoinColumn(name = "entry_camera_id", nullable = false)
    private Camera entryCamera;

    @ManyToOne
    @JoinColumn(name = "exit_camera_id")
    private Camera exitCamera;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessStatus status;

    public enum AccessStatus {
        IN_PROGRESS, COMPLETED
    }
}
