package com.ms_agenda_profesional.agenda.service;

import com.ms_agenda_profesional.agenda.dto.AppointmentDTO;

import java.util.List;

public interface AppointmentService {
    AppointmentDTO createAppointment(AppointmentDTO appointmentDTO);
    AppointmentDTO getAppointmentById(String id);
    List<AppointmentDTO> getAppointmentsByPatientId(String patientId);
    List<AppointmentDTO> getAppointmentsByPractitionerId(String practitionerId);
    AppointmentDTO updateStatus(String id, String status);
    void deleteAppointment(String id);
}
