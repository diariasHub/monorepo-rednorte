package cl.rednorte.ms_ficha_clinica.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cl.rednorte.ms_ficha_clinica.dto.ObservationDTO;
import cl.rednorte.ms_ficha_clinica.service.ObservationService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import cl.rednorte.ms_ficha_clinica.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ObservationServiceImpl implements ObservationService {

    private final IGenericClient fhirClient;

    @Override
    public ObservationDTO createObservation(ObservationDTO observationDTO) {
        Observation fhirObs = new Observation();
        
        if (observationDTO.getPatientId() != null) {
            fhirObs.setSubject(new Reference("Patient/" + observationDTO.getPatientId()));
        }
        if (observationDTO.getEncounterId() != null) {
            fhirObs.setEncounter(new Reference("Encounter/" + observationDTO.getEncounterId()));
        }
        
        if (observationDTO.getEffectiveDate() != null) {
            fhirObs.setEffective(new DateTimeType(observationDTO.getEffectiveDate()));
        } else {
            fhirObs.setEffective(new DateTimeType(new Date()));
        }
        
        if (observationDTO.getCode() != null) {
            fhirObs.getCode().addCoding().setCode(observationDTO.getCode()).setDisplay(observationDTO.getCode());
        }
        
        if (observationDTO.getValue() != null) {
            Quantity quantity = new Quantity();
            quantity.setValue(java.math.BigDecimal.valueOf(observationDTO.getValue()));
            if (observationDTO.getUnit() != null) quantity.setUnit(observationDTO.getUnit());
            fhirObs.setValue(quantity);
        }
        
        var outcome = fhirClient.create().resource(fhirObs).execute();
        observationDTO.setId(outcome.getId().getIdPart());
        return observationDTO;
    }

    @Override
    public ObservationDTO getObservationById(String id) {
        try {
            Observation obs = fhirClient.read().resource(Observation.class).withId(id).execute();
            return mapToDTO(obs);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Observation not found with id: " + id);
        }
    }

    @Override
    public List<ObservationDTO> getAllObservations() {
        Bundle response = fhirClient.search().forResource(Observation.class).returnBundle(Bundle.class).execute();
        return extractDtoListFromBundle(response);
    }

    @Override
    public List<ObservationDTO> getObservationsByPatientId(String patientId) {
        Bundle response = fhirClient.search()
                .forResource(Observation.class)
                .where(Observation.SUBJECT.hasId("Patient/" + patientId))
                .returnBundle(Bundle.class)
                .execute();
        return extractDtoListFromBundle(response);
    }

    @Override
    public List<ObservationDTO> getObservationsByEncounterId(String encounterId) {
        Bundle response = fhirClient.search()
                .forResource(Observation.class)
                .where(Observation.ENCOUNTER.hasId("Encounter/" + encounterId))
                .returnBundle(Bundle.class)
                .execute();
        return extractDtoListFromBundle(response);
    }

    @Override
    public List<ObservationDTO> getObservationHistory(String patientId) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        
        Bundle response = fhirClient.search()
                .forResource(Observation.class)
                .where(Observation.SUBJECT.hasId("Patient/" + patientId))
                .where(Observation.DATE.afterOrEquals().day(cal.getTime())) 
                .returnBundle(Bundle.class)
                .execute();
        return extractDtoListFromBundle(response);
    }

    @Override
    public ObservationDTO updateObservation(String id, ObservationDTO observationDTO) {
        Observation fhirObs = new Observation();
        fhirObs.setId(id);
        
        if (observationDTO.getPatientId() != null) {
            fhirObs.setSubject(new Reference("Patient/" + observationDTO.getPatientId()));
        }
        if (observationDTO.getEncounterId() != null) {
            fhirObs.setEncounter(new Reference("Encounter/" + observationDTO.getEncounterId()));
        }
        if (observationDTO.getCode() != null) {
            fhirObs.getCode().addCoding().setCode(observationDTO.getCode()).setDisplay(observationDTO.getCode());
        }
        if (observationDTO.getValue() != null) {
            Quantity quantity = new Quantity();
            quantity.setValue(java.math.BigDecimal.valueOf(observationDTO.getValue()));
            if (observationDTO.getUnit() != null) quantity.setUnit(observationDTO.getUnit());
            fhirObs.setValue(quantity);
        }
        
        fhirClient.update().resource(fhirObs).execute();
        return mapToDTO(fhirObs);
    }

    @Override
    public void deleteObservation(String id) {
        fhirClient.delete().resourceById(new IdType("Observation", id)).execute();
    }

    private ObservationDTO mapToDTO(Observation obs) {
        ObservationDTO dto = new ObservationDTO();
        if (obs.hasIdElement()) dto.setId(obs.getIdElement().getIdPart());
        if (obs.hasSubject()) dto.setPatientId(obs.getSubject().getReferenceElement().getIdPart());
        if (obs.hasEncounter()) dto.setEncounterId(obs.getEncounter().getReferenceElement().getIdPart());
        if (obs.hasEffectiveDateTimeType()) dto.setEffectiveDate(obs.getEffectiveDateTimeType().getValue());
        
        if (obs.hasCode() && !obs.getCode().getCoding().isEmpty()) {
            dto.setCode(obs.getCode().getCodingFirstRep().getCode());
        }
        
        if (obs.hasValueQuantity() && obs.getValueQuantity().getValue() != null) {
            dto.setValue(obs.getValueQuantity().getValue().doubleValue());
            dto.setUnit(obs.getValueQuantity().getUnit());
        }
        return dto;
    }

    private List<ObservationDTO> extractDtoListFromBundle(Bundle bundle) {
        List<ObservationDTO> list = new ArrayList<>();
        if (bundle == null || bundle.getEntry() == null) return list;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Observation) {
                list.add(mapToDTO((Observation) entry.getResource()));
            }
        }
        return list;
    }
}