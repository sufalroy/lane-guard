package org.skytel.laneguard.gates.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.skytel.laneguard.cameras.models.Camera;

@Entity
@Table(name = "gates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne
    @JoinColumn(name = "entry_camera_id")
    private Camera entryCamera;

    @OneToOne
    @JoinColumn(name = "exit_camera_id")
    private Camera exitCamera;
}
