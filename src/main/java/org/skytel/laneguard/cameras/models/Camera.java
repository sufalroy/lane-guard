package org.skytel.laneguard.cameras.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "cameras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Camera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ipAddress;

    @Column(nullable = false, unique = true)
    private String macAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CameraType type;

    public enum CameraType {ENTRY, EXIT}
}
