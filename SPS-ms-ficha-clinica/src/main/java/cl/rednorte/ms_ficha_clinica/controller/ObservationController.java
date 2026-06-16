package cl.rednorte.ms_ficha_clinica.controller;

import cl.rednorte.ms_ficha_clinica.dto.ObservationDTO;
import cl.rednorte.ms_ficha_clinica.service.ObservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/observations")
@RequiredArgsConstructor
public class ObservationController {

    private final ObservationService observationService;

    @PostMapping
    public ResponseEntity<ObservationDTO> createObservation(@RequestBody ObservationDTO observationDTO) {
        return ResponseEntity.ok(observationService.createObservation(observationDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObservationDTO> getObservationById(@PathVariable String id) {
        ObservationDTO dto = observationService.getObservationById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<ObservationDTO>> getAllObservations() {
        return ResponseEntity.ok(observationService.getAllObservations());
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ObservationDTO>> getObservationsByPatientId(@PathVariable String patientId) {
        return ResponseEntity.ok(observationService.getObservationsByPatientId(patientId));
    }

    @GetMapping("/encounter/{encounterId}")
    public ResponseEntity<List<ObservationDTO>> getObservationsByEncounterId(@PathVariable String encounterId) {
        return ResponseEntity.ok(observationService.getObservationsByEncounterId(encounterId));
    }

    @GetMapping("/patient/{patientId}/history")
    public ResponseEntity<List<ObservationDTO>> getObservationHistory(@PathVariable String patientId) {
        return ResponseEntity.ok(observationService.getObservationHistory(patientId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ObservationDTO> updateObservation(@PathVariable String id, @RequestBody ObservationDTO observationDTO) {
        return ResponseEntity.ok(observationService.updateObservation(id, observationDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteObservation(@PathVariable String id) {
        observationService.deleteObservation(id);
        return ResponseEntity.noContent().build();
    }
}
