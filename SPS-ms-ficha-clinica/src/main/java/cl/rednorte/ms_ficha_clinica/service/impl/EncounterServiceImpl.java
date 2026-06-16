package cl.rednorte.ms_ficha_clinica.service.impl;

import cl.rednorte.ms_ficha_clinica.dto.EncounterDTO;
import cl.rednorte.ms_ficha_clinica.service.EncounterService;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EncounterServiceImpl implements EncounterService {

    private final IGenericClient fhirClient;

    @Override
    public EncounterDTO createEncounter(EncounterDTO encounterDTO) {
        if (encounterDTO.getPatientId() != null && encounterDTO.getPatientName() != null && !encounterDTO.getPatientName().isEmpty()) {
            org.hl7.fhir.r4.model.Patient paciente = new org.hl7.fhir.r4.model.Patient();
            paciente.setId(encounterDTO.getPatientId());
            paciente.addIdentifier().setSystem("http://registrocivil.cl/rut").setValue(encounterDTO.getPatientId());
            paciente.addName().setText(encounterDTO.getPatientName());
            try {
                fhirClient.update().resource(paciente).execute();
            } catch (Exception e) {
                // Ignore if patient update fails
            }
        }

        Encounter fhirEncounter = new Encounter();
        
        if (encounterDTO.getPatientId() != null) {
            Reference patientRef = new Reference("Patient/" + encounterDTO.getPatientId());
            if (encounterDTO.getPatientName() != null && !encounterDTO.getPatientName().isEmpty()) {
                patientRef.setDisplay(encounterDTO.getPatientName());
            }
            fhirEncounter.setSubject(patientRef);
        }
        
        if (encounterDTO.getPeriodStart() != null) {
            fhirEncounter.getPeriod().setStart(encounterDTO.getPeriodStart());
        } else {
            fhirEncounter.getPeriod().setStart(new Date());
        }
        
        if (encounterDTO.getStatus() != null) {
            fhirEncounter.setStatus(Encounter.EncounterStatus.valueOf(encounterDTO.getStatus().toUpperCase()));
        } else {
            fhirEncounter.setStatus(Encounter.EncounterStatus.INPROGRESS);
        }

        var outcome = fhirClient.create().resource(fhirEncounter).execute();
        encounterDTO.setId(outcome.getId().getIdPart());
        encounterDTO.setStatus(fhirEncounter.getStatus().toCode());
        return encounterDTO;
    }

    @Override
    public EncounterDTO getEncounterById(String id) {
        try {
            Encounter fhirEncounter = fhirClient.read().resource(Encounter.class).withId(id).execute();
            return mapToDTO(fhirEncounter);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<EncounterDTO> getEncountersByPatientId(String patientId) {
        Bundle response = fhirClient.search()
                .forResource(Encounter.class)
                .where(Encounter.SUBJECT.hasId("Patient/" + patientId))
                .returnBundle(Bundle.class)
                .execute();

        List<EncounterDTO> encounters = new ArrayList<>();
        for (Bundle.BundleEntryComponent entry : response.getEntry()) {
            if (entry.getResource() instanceof Encounter) {
                encounters.add(mapToDTO((Encounter) entry.getResource()));
            }
        }
        return encounters;
    }

    @Override
    public EncounterDTO updateStatus(String id, String status) {
        try {
            Encounter fhirEncounter = fhirClient.read().resource(Encounter.class).withId(id).execute();
            
            Encounter.EncounterStatus newStatus = Encounter.EncounterStatus.valueOf(status.toUpperCase());
            fhirEncounter.setStatus(newStatus);
            
            if (newStatus == Encounter.EncounterStatus.FINISHED) {
                fhirEncounter.getPeriod().setEnd(new Date());
            }

            fhirClient.update().resource(fhirEncounter).execute();
            return mapToDTO(fhirEncounter);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void deleteEncounter(String id) {
        fhirClient.delete().resourceById(new IdType("Encounter", id)).execute();
    }

    private EncounterDTO mapToDTO(Encounter fhirEncounter) {
        EncounterDTO dto = new EncounterDTO();
        if (fhirEncounter.hasIdElement()) dto.setId(fhirEncounter.getIdElement().getIdPart());
        if (fhirEncounter.hasStatus()) dto.setStatus(fhirEncounter.getStatus().toCode());
        if (fhirEncounter.hasSubject()) {
            dto.setPatientId(fhirEncounter.getSubject().getReferenceElement().getIdPart());
            if (fhirEncounter.getSubject().hasDisplay()) {
                dto.setPatientName(fhirEncounter.getSubject().getDisplay());
            }
        }
        if (fhirEncounter.hasPeriod()) {
            dto.setPeriodStart(fhirEncounter.getPeriod().getStart());
            dto.setPeriodEnd(fhirEncounter.getPeriod().getEnd());
        }
        return dto;
    }
}