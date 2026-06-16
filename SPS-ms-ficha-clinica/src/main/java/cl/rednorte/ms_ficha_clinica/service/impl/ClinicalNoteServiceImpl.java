package cl.rednorte.ms_ficha_clinica.service.impl;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import cl.rednorte.ms_ficha_clinica.dto.ClinicalNoteDTO;
import cl.rednorte.ms_ficha_clinica.exception.ResourceNotFoundException;
import cl.rednorte.ms_ficha_clinica.service.ClinicalNoteService;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClinicalNoteServiceImpl implements ClinicalNoteService {

    private final IGenericClient fhirClient;

    @Override
    public ClinicalNoteDTO createClinicalNote(ClinicalNoteDTO clinicalNoteDTO) {
        DocumentReference doc = new DocumentReference();
        
        if (clinicalNoteDTO.getPatientId() != null) {
            doc.setSubject(new Reference("Patient/" + clinicalNoteDTO.getPatientId()));
        }
        if (clinicalNoteDTO.getEncounterId() != null) {
            DocumentReference.DocumentReferenceContextComponent context = new DocumentReference.DocumentReferenceContextComponent();
            context.getEncounter().add(new Reference("Encounter/" + clinicalNoteDTO.getEncounterId()));
            doc.setContext(context);
        }
        
        String content = clinicalNoteDTO.getContent();
        if (content != null) {
            doc.setDescription(content);
            Attachment att = new Attachment();
            att.setContentType("text/plain");
            att.setData(content.getBytes(StandardCharsets.UTF_8));
            doc.addContent().setAttachment(att);
        }

        if (clinicalNoteDTO.getCreatedAt() != null) {
            doc.setDate(clinicalNoteDTO.getCreatedAt());
        } else {
            doc.setDate(new Date());
        }

        var outcome = fhirClient.create().resource(doc).execute();
        clinicalNoteDTO.setId(outcome.getId().getIdPart());
        return clinicalNoteDTO;
    }

    @Override
    public ClinicalNoteDTO getClinicalNoteById(String id) {
        try {
            DocumentReference doc = fhirClient.read().resource(DocumentReference.class).withId(id).execute();
            return mapToDTO(doc);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Clinical note not found: " + id);
        }
    }

    @Override
    public List<ClinicalNoteDTO> getClinicalNotesByPatientId(String patientId) {
        Bundle response = fhirClient.search().forResource(DocumentReference.class)
                .where(DocumentReference.SUBJECT.hasId("Patient/" + patientId))
                .returnBundle(Bundle.class)
                .execute();
        return extractDtoList(response);
    }

    @Override
    public List<ClinicalNoteDTO> getClinicalNotesByEncounterId(String encounterId) {
        Bundle response = fhirClient.search().forResource(DocumentReference.class)
            .where(new ReferenceClientParam("context").hasId("Encounter/" + encounterId))
            .returnBundle(Bundle.class)
            .execute();
        return extractDtoList(response);
    }

    @Override
    public void deleteClinicalNote(String id) {
        fhirClient.delete().resourceById(new IdType("DocumentReference", id)).execute();
    }

    private ClinicalNoteDTO mapToDTO(DocumentReference doc) {
        ClinicalNoteDTO dto = new ClinicalNoteDTO();
        
        if (doc.hasIdElement()) dto.setId(doc.getIdElement().getIdPart());
        if (doc.hasSubject()) dto.setPatientId(doc.getSubject().getReferenceElement().getIdPart());
        if (doc.hasContext() && doc.getContext().hasEncounter()) {
            dto.setEncounterId(doc.getContext().getEncounterFirstRep().getReferenceElement().getIdPart());
        }
        if (doc.hasDescription()) dto.setContent(doc.getDescription());
        if (doc.hasContent() && !doc.getContent().isEmpty()) {
            Attachment att = doc.getContentFirstRep().getAttachment();
            if (att != null && att.hasData()) {
                dto.setContent(new String(att.getData(), StandardCharsets.UTF_8));
            }
        }
        if (doc.hasDate()) dto.setCreatedAt(doc.getDate());
        
        return dto;
    }

    private List<ClinicalNoteDTO> extractDtoList(Bundle bundle) {
        List<ClinicalNoteDTO> list = new ArrayList<>();
        if (bundle == null || bundle.getEntry() == null) return list;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof DocumentReference) {
                list.add(mapToDTO((DocumentReference) entry.getResource()));
            }
        }
        return list;
    }
}
