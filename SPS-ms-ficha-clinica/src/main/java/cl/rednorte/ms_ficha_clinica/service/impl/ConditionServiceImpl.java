package cl.rednorte.ms_ficha_clinica.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cl.rednorte.ms_ficha_clinica.dto.ConditionDTO;
import cl.rednorte.ms_ficha_clinica.service.ConditionService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import cl.rednorte.ms_ficha_clinica.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConditionServiceImpl implements ConditionService {

    private final IGenericClient fhirClient;

    @Override
    public ConditionDTO createCondition(ConditionDTO conditionDTO) {
        Condition fhirCondition = new Condition();
        
        if (conditionDTO.getPatientId() != null) {
            fhirCondition.setSubject(new Reference("Patient/" + conditionDTO.getPatientId()));
        }
        if (conditionDTO.getEncounterId() != null) {
            fhirCondition.setEncounter(new Reference("Encounter/" + conditionDTO.getEncounterId()));
        }
        
        if (conditionDTO.getCode() != null || conditionDTO.getDescription() != null) {
            CodeableConcept codeableConcept = new CodeableConcept();
            if (conditionDTO.getCode() != null) {
                codeableConcept.addCoding()
                    .setCode(conditionDTO.getCode())
                    .setDisplay(conditionDTO.getDescription());
            } else {
                codeableConcept.setText(conditionDTO.getDescription());
            }
            fhirCondition.setCode(codeableConcept);
        }
        
        if (conditionDTO.getClinicalStatus() != null) {
            CodeableConcept status = new CodeableConcept();
            status.addCoding()
                .setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical")
                .setCode(conditionDTO.getClinicalStatus());
            fhirCondition.setClinicalStatus(status);
        }
        
        if (!fhirCondition.hasRecordedDate()) {
            fhirCondition.setRecordedDate(new Date());
        }
        
        var outcome = fhirClient.create().resource(fhirCondition).execute();
        conditionDTO.setId(outcome.getId().getIdPart());
        return conditionDTO;
    }

    @Override
    public ConditionDTO getConditionById(String id) {
        try {
            Condition condition = fhirClient.read().resource(Condition.class).withId(id).execute();
            return mapToDTO(condition);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Condition not found with id: " + id);
        }
    }

    @Override
    public List<ConditionDTO> getConditionsByPatientId(String patientId) {
        Bundle response = fhirClient.search().forResource(Condition.class)
                .where(Condition.SUBJECT.hasId("Patient/" + patientId))
                .returnBundle(Bundle.class).execute();
        return extractDtoList(response);
    }

    @Override
    public List<ConditionDTO> getConditionsByEncounterId(String encounterId) {
        Bundle response = fhirClient.search().forResource(Condition.class)
                .where(Condition.ENCOUNTER.hasId("Encounter/" + encounterId))
                .returnBundle(Bundle.class).execute();
        return extractDtoList(response);
    }

    @Override
    public List<ConditionDTO> getConditionHistory(String patientId) {
        return getConditionsByPatientId(patientId);
    }

    @Override
    public void deleteCondition(String id) {
        fhirClient.delete().resourceById(new IdType("Condition", id)).execute();
    }

    private List<ConditionDTO> extractDtoList(Bundle bundle) {
        List<ConditionDTO> list = new ArrayList<>();
        if (bundle == null || bundle.getEntry() == null) return list;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Condition) {
                list.add(mapToDTO((Condition) entry.getResource()));
            }
        }
        return list;
    }

    private ConditionDTO mapToDTO(Condition condition) {
        ConditionDTO dto = new ConditionDTO();
        
        if (condition.hasIdElement()) {
            dto.setId(condition.getIdElement().getIdPart());
        }
        
        if (condition.hasSubject()) {
            dto.setPatientId(condition.getSubject().getReferenceElement().getIdPart());
        }
        
        if (condition.hasEncounter()) {
            dto.setEncounterId(condition.getEncounter().getReferenceElement().getIdPart());
        }
        
        if (condition.hasCode() && !condition.getCode().getCoding().isEmpty()) {
            dto.setCode(condition.getCode().getCodingFirstRep().getCode());
            dto.setDescription(condition.getCode().getCodingFirstRep().getDisplay());
        } else if (condition.hasCode() && condition.getCode().hasText()) {
            dto.setDescription(condition.getCode().getText());
        }
        
        if (condition.hasClinicalStatus() && !condition.getClinicalStatus().getCoding().isEmpty()) {
            dto.setClinicalStatus(condition.getClinicalStatus().getCodingFirstRep().getCode());
        }
        
        return dto;
    }
}