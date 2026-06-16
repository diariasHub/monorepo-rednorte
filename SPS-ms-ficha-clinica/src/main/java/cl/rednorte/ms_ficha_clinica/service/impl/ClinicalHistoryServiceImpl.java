package cl.rednorte.ms_ficha_clinica.service.impl;

import cl.rednorte.ms_ficha_clinica.dto.ClinicalHistoryDTO;
import cl.rednorte.ms_ficha_clinica.service.ClinicalHistoryService;
import cl.rednorte.ms_ficha_clinica.service.ClinicalNoteService;
import cl.rednorte.ms_ficha_clinica.service.ConditionService;
import cl.rednorte.ms_ficha_clinica.service.EncounterService;
import cl.rednorte.ms_ficha_clinica.service.ObservationService;
import cl.rednorte.ms_ficha_clinica.service.ProcedureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClinicalHistoryServiceImpl implements ClinicalHistoryService {

    private final EncounterService encounterService;
    private final ObservationService observationService;
    private final ConditionService conditionService;
    private final ProcedureService procedureService;
    private final ClinicalNoteService clinicalNoteService;

    @Override
    public ClinicalHistoryDTO getFullClinicalHistory(String patientId) {
        ClinicalHistoryDTO history = new ClinicalHistoryDTO();
        history.setPatientId(patientId);
        history.setEncounters(encounterService.getEncountersByPatientId(patientId));
        history.setObservations(observationService.getObservationsByPatientId(patientId));
        history.setConditions(conditionService.getConditionsByPatientId(patientId));
        history.setProcedures(procedureService.getProceduresByPatientId(patientId));
        history.setClinicalNotes(clinicalNoteService.getClinicalNotesByPatientId(patientId));
        return history;
    }
}
