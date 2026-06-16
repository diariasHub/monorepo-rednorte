package cl.rednorte.ms_ficha_clinica.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cl.rednorte.ms_ficha_clinica.dto.ProcedureDTO;
import cl.rednorte.ms_ficha_clinica.service.ProcedureService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcedureServiceImpl implements ProcedureService {

    private final IGenericClient fhirClient;

    @Override
    public ProcedureDTO createProcedure(ProcedureDTO procedureDTO) {
        Procedure fhirProcedure = new Procedure();
        
        if (procedureDTO.getPatientId() != null) {
            fhirProcedure.setSubject(new Reference("Patient/" + procedureDTO.getPatientId()));
        }
        if (procedureDTO.getEncounterId() != null) {
            fhirProcedure.setEncounter(new Reference("Encounter/" + procedureDTO.getEncounterId()));
        }
        
        if (procedureDTO.getCode() != null) {
            fhirProcedure.getCode().addCoding()
                .setCode(procedureDTO.getCode())
                .setDisplay(procedureDTO.getDescription());
        }
        
        if (procedureDTO.getPerformedDate() != null) {
            fhirProcedure.setPerformed(new DateTimeType(procedureDTO.getPerformedDate()));
        } else {
            fhirProcedure.setPerformed(new DateTimeType(new Date()));
        }
        
        if (procedureDTO.getStatus() != null) {
            fhirProcedure.setStatus(Procedure.ProcedureStatus.valueOf(procedureDTO.getStatus().toUpperCase()));
        } else {
            fhirProcedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
        }

        var outcome = fhirClient.create().resource(fhirProcedure).execute();
        procedureDTO.setId(outcome.getId().getIdPart());
        procedureDTO.setStatus(fhirProcedure.getStatus().toCode());
        return procedureDTO;
    }

    @Override
    public ProcedureDTO getProcedureById(String id) {
        try {
            Procedure procedure = fhirClient.read().resource(Procedure.class).withId(id).execute();
            return mapToDTO(procedure);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<ProcedureDTO> getProceduresByPatientId(String patientId) {
        Bundle response = fhirClient.search().forResource(Procedure.class)
                .where(Procedure.SUBJECT.hasId("Patient/" + patientId))
                .returnBundle(Bundle.class)
                .execute();
        return extractDtoList(response);
    }

    @Override
    public List<ProcedureDTO> getProceduresByEncounterId(String encounterId) {
        Bundle response = fhirClient.search().forResource(Procedure.class)
                .where(Procedure.ENCOUNTER.hasId("Encounter/" + encounterId))
                .returnBundle(Bundle.class)
                .execute();
        return extractDtoList(response);
    }

    @Override
    public void deleteProcedure(String id) {
        fhirClient.delete().resourceById(new IdType("Procedure", id)).execute();
    }

    private List<ProcedureDTO> extractDtoList(Bundle bundle) {
        List<ProcedureDTO> list = new ArrayList<>();
        if (bundle == null || bundle.getEntry() == null) return list;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Procedure) {
                list.add(mapToDTO((Procedure) entry.getResource()));
            }
        }
        return list;
    }

    private ProcedureDTO mapToDTO(Procedure procedure) {
        ProcedureDTO dto = new ProcedureDTO();
        
        if (procedure.hasIdElement()) dto.setId(procedure.getIdElement().getIdPart());
        if (procedure.hasSubject()) dto.setPatientId(procedure.getSubject().getReferenceElement().getIdPart());
        if (procedure.hasEncounter()) dto.setEncounterId(procedure.getEncounter().getReferenceElement().getIdPart());
        if (procedure.hasPerformedDateTimeType()) dto.setPerformedDate(procedure.getPerformedDateTimeType().getValue());
        if (procedure.hasStatus()) dto.setStatus(procedure.getStatus().toCode());
        if (procedure.hasCode() && !procedure.getCode().getCoding().isEmpty()) {
            dto.setCode(procedure.getCode().getCodingFirstRep().getCode());
            dto.setDescription(procedure.getCode().getCodingFirstRep().getDisplay());
        }
        
        return dto;
    }
}