package com.ms_agenda_profesional.agenda.controller;

import com.ms_agenda_profesional.agenda.dto.AppointmentDTO;
import com.ms_agenda_profesional.agenda.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AgendaController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentDTO> createAppointment(@RequestBody AppointmentDTO appointmentDTO) {
        return ResponseEntity.ok(appointmentService.createAppointment(appointmentDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable String id) {
        AppointmentDTO dto = appointmentService.getAppointmentById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatientId(@PathVariable String patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(patientId));
    }

    @GetMapping("/practitioner/{practitionerId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPractitionerId(@PathVariable String practitionerId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPractitionerId(practitionerId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentDTO> updateStatus(@PathVariable String id, @RequestParam String status) {
        AppointmentDTO dto = appointmentService.updateStatus(id, status);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}