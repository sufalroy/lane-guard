package org.skytel.laneguard.gates.controllers;

import org.skytel.laneguard.gates.models.GateDTO;
import org.skytel.laneguard.gates.services.GateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gates")
public class GateController {
    private final GateService gateService;

    public GateController(GateService gateService) {
        this.gateService = gateService;
    }

    @PostMapping
    public ResponseEntity<GateDTO> createGate(@RequestBody GateDTO gateDTO) {
        return ResponseEntity.ok(gateService.createGate(gateDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GateDTO> getGate(@PathVariable Long id) {
        return gateService.getGate(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<GateDTO>> getAllGates() {
        return ResponseEntity.ok(gateService.getAllGates());
    }

    @PutMapping("/{id}")
    public ResponseEntity<GateDTO> updateGate(@PathVariable Long id, @RequestBody GateDTO gateDTO) {
        return gateService.updateGate(id, gateDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGate(@PathVariable Long id) {
        return gateService.deleteGate(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}