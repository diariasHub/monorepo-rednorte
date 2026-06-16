package cl.rednorte.ms_ficha_clinica.controller;

import cl.rednorte.ms_ficha_clinica.dto.EncounterDTO;
import cl.rednorte.ms_ficha_clinica.service.EncounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/encounters")
@RequiredArgsConstructor
public class EncounterController {
    
    private final EncounterService encounterService;
    
    @PostMapping
    public ResponseEntity<EncounterDTO> createEncounter(@RequestBody EncounterDTO encounterDTO) {
        return ResponseEntity.ok(encounterService.createEncounter(encounterDTO));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EncounterDTO> getEncounterById(@PathVariable String id) {
        EncounterDTO dto = encounterService.getEncounterById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<EncounterDTO>> getEncountersByPatientId(@PathVariable String patientId) {
        return ResponseEntity.ok(encounterService.getEncountersByPatientId(patientId));
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<EncounterDTO> updateStatus(@PathVariable String id, @RequestParam String status) {
        EncounterDTO dto = encounterService.updateStatus(id, status);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEncounter(@PathVariable String id) {
        encounterService.deleteEncounter(id);
        return ResponseEntity.noContent().build();
    }
}
