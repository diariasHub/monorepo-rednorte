package com.ms_agenda_profesional.agenda.service.impl;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.ms_agenda_profesional.agenda.dto.AppointmentDTO;
import com.ms_agenda_profesional.agenda.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final IGenericClient fhirClient;

    @Override
    public AppointmentDTO createAppointment(AppointmentDTO appointmentDTO) {
        if (appointmentDTO.getPatientId() != null && appointmentDTO.getPatientName() != null && !appointmentDTO.getPatientName().isEmpty()) {
            org.hl7.fhir.r4.model.Patient paciente = new org.hl7.fhir.r4.model.Patient();
            paciente.setId(appointmentDTO.getPatientId());
            paciente.addIdentifier().setSystem("http://registrocivil.cl/rut").setValue(appointmentDTO.getPatientId());
            paciente.addName().setText(appointmentDTO.getPatientName());
            try {
                fhirClient.update().resource(paciente).execute();
            } catch (Exception e) {
                // Ignore if patient update fails
            }
        }

        Appointment appointment = new Appointment();

        if (appointmentDTO.getStatus() != null) {
            appointment.setStatus(Appointment.AppointmentStatus.valueOf(appointmentDTO.getStatus().toUpperCase()));
        } else {
            appointment.setStatus(Appointment.AppointmentStatus.BOOKED);
        }

        if (appointmentDTO.getDescription() != null) {
            appointment.setDescription(appointmentDTO.getDescription());
        }

        if (appointmentDTO.getStart() != null) {
            appointment.setStart(appointmentDTO.getStart());
        }

        if (appointmentDTO.getEnd() != null) {
            appointment.setEnd(appointmentDTO.getEnd());
        }

        // Add Patient participant
        if (appointmentDTO.getPatientId() != null) {
            Appointment.AppointmentParticipantComponent patientParticipant = new Appointment.AppointmentParticipantComponent();
            Reference patientRef = new Reference("Patient/" + appointmentDTO.getPatientId());
            if (appointmentDTO.getPatientName() != null && !appointmentDTO.getPatientName().isEmpty()) {
                patientRef.setDisplay(appointmentDTO.getPatientName());
            }
            patientParticipant.setActor(patientRef);
            patientParticipant.setStatus(Appointment.ParticipationStatus.ACCEPTED);
            appointment.addParticipant(patientParticipant);
        }

        // Add Practitioner participant
        if (appointmentDTO.getPractitionerId() != null) {
            Appointment.AppointmentParticipantComponent practitionerParticipant = new Appointment.AppointmentParticipantComponent();
            practitionerParticipant.setActor(new Reference("Practitioner/" + appointmentDTO.getPractitionerId()));
            practitionerParticipant.setStatus(Appointment.ParticipationStatus.ACCEPTED);
            appointment.addParticipant(practitionerParticipant);
        }

        var outcome = fhirClient.create().resource(appointment).execute();
        appointmentDTO.setId(outcome.getId().getIdPart());
        appointmentDTO.setStatus(appointment.getStatus().toCode());
        return appointmentDTO;
    }

    @Override
    public AppointmentDTO getAppointmentById(String id) {
        try {
            Appointment appointment = fhirClient.read().resource(Appointment.class).withId(id).execute();
            return mapToDTO(appointment);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<AppointmentDTO> getAppointmentsByPatientId(String patientId) {
        Bundle response = fhirClient.search().forResource(Appointment.class)
                .where(new ReferenceClientParam("actor").hasId("Patient/" + patientId))
                .returnBundle(Bundle.class)
                .execute();
        return extractDtoList(response);
    }

    @Override
    public List<AppointmentDTO> getAppointmentsByPractitionerId(String practitionerId) {
        Bundle response = fhirClient.search().forResource(Appointment.class)
                .where(new ReferenceClientParam("actor").hasId("Practitioner/" + practitionerId))
                .returnBundle(Bundle.class)
                .execute();
        return extractDtoList(response);
    }

    @Override
    public AppointmentDTO updateStatus(String id, String status) {
        try {
            Appointment appointment = fhirClient.read().resource(Appointment.class).withId(id).execute();
            Appointment.AppointmentStatus newStatus = Appointment.AppointmentStatus.valueOf(status.toUpperCase());
            appointment.setStatus(newStatus);
            fhirClient.update().resource(appointment).execute();
            return mapToDTO(appointment);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void deleteAppointment(String id) {
        fhirClient.delete().resourceById(new IdType("Appointment", id)).execute();
    }

    private List<AppointmentDTO> extractDtoList(Bundle bundle) {
        List<AppointmentDTO> list = new ArrayList<>();
        if (bundle == null || bundle.getEntry() == null) return list;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Appointment) {
                list.add(mapToDTO((Appointment) entry.getResource()));
            }
        }
        return list;
    }

    private AppointmentDTO mapToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        
        if (appointment.hasIdElement()) dto.setId(appointment.getIdElement().getIdPart());
        if (appointment.hasStatus()) dto.setStatus(appointment.getStatus().toCode());
        if (appointment.hasDescription()) dto.setDescription(appointment.getDescription());
        if (appointment.hasStart()) dto.setStart(appointment.getStart());
        if (appointment.hasEnd()) dto.setEnd(appointment.getEnd());
        
        if (appointment.hasParticipant()) {
            for (Appointment.AppointmentParticipantComponent participant : appointment.getParticipant()) {
                if (participant.hasActor() && participant.getActor().hasReference()) {
                    String ref = participant.getActor().getReference();
                    if (ref.startsWith("Patient/")) {
                        dto.setPatientId(ref.substring("Patient/".length()));
                        if (participant.getActor().hasDisplay()) {
                            dto.setPatientName(participant.getActor().getDisplay());
                        }
                    } else if (ref.startsWith("Practitioner/")) {
                        dto.setPractitionerId(ref.substring("Practitioner/".length()));
                    }
                }
            }
        }
        
        return dto;
    }
}
