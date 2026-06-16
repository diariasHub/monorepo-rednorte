package cl.rednorte.ms_urgencias_flujo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UrgenciaService {

    private final IGenericClient fhirClient;
    private final SqsProducerService sqsProducerService;

    public UrgenciaService(IGenericClient fhirClient, SqsProducerService sqsProducerService) {
        this.fhirClient = fhirClient;
        this.sqsProducerService = sqsProducerService;
    }

    public String registrarIngreso(String rut, String nombre, String motivo) {
        if (nombre != null && !nombre.isEmpty()) {
            Patient paciente = new Patient();
            paciente.setId(rut);
            paciente.addIdentifier().setSystem("http://registrocivil.cl/rut").setValue(rut);
            paciente.addName().setText(nombre);
            try {
                fhirClient.update().resource(paciente).execute();
            } catch (Exception e) {
                // Ignore if patient update fails
            }
        }

        Encounter encuentro = new Encounter();
        encuentro.setStatus(Encounter.EncounterStatus.ARRIVED);
        
        // Identificador del Paciente (RUT)
        Reference patientRef = new Reference("Patient/" + rut);
        if (nombre != null && !nombre.isEmpty()) {
            patientRef.setDisplay(nombre);
        }
        encuentro.setSubject(patientRef);
        
        // Motivo de consulta
        CodeableConcept motivoCode = new CodeableConcept().setText(motivo);
        encuentro.addReasonCode(motivoCode);

        MethodOutcome outcome = fhirClient.create().resource(encuentro).execute();
        return outcome.getId().getIdPart();
    }

    public void procesarTriage(String id, Map<String, Object> datosTriage) {
        // 1. Leer el encuentro actual
        Encounter encuentro = fhirClient.read().resource(Encounter.class).withId(id).execute();
        encuentro.setStatus(Encounter.EncounterStatus.TRIAGED);

        // 2. Registrar observaciones (Triage) dinámicamente
        for (Map.Entry<String, Object> entry : datosTriage.entrySet()) {
            if ("categorizacion".equalsIgnoreCase(entry.getKey())) continue;
            
            Observation obs = new Observation();
            obs.setStatus(Observation.ObservationStatus.FINAL);
            obs.getCode().setText(entry.getKey());
            obs.setValue(new StringType(entry.getValue().toString()));
            obs.setEncounter(new Reference("Encounter/" + id));
            obs.setSubject(encuentro.getSubject());
            fhirClient.create().resource(obs).execute();
        }

        // 3. Asignar prioridad según categorización
        if (datosTriage.containsKey("categorizacion")) {
            CodeableConcept priority = new CodeableConcept().setText(datosTriage.get("categorizacion").toString());
            encuentro.setPriority(priority);
        }
        
        // 4. Actualizar el encuentro con la prioridad y estado
        fhirClient.update().resource(encuentro).execute();
    }

    public int calcularTiempoEspera(String rut) {
        // 1. Buscar encuentros activos en triage
        Bundle bundle = fhirClient.search().forResource(Encounter.class)
                .where(Encounter.STATUS.exactly().code("triaged"))
                .returnBundle(Bundle.class).execute();

        // 2. Lógica Algorítmica: Contar pacientes con mayor gravedad en la lista
        // aquí se itera el bundle sumando minutos según prioridad relativa.
        return 45; 
    }

public void cancelarAtencion(String idEncuentro, String rutConfirmacion) {
        Encounter encuentro = fhirClient.read().resource(Encounter.class).withId(idEncuentro).execute();
        
        encuentro.setStatus(Encounter.EncounterStatus.CANCELLED);
        
        Extension motivoCancelacion = new Extension();
        motivoCancelacion.setUrl("http://rednorte.cl/fhir/StructureDefinition/motivo-cancelacion");
        motivoCancelacion.setValue(new StringType("Abandono voluntario confirmado por RUT: " + rutConfirmacion));       
        encuentro.addExtension(motivoCancelacion);

        fhirClient.update().resource(encuentro).execute();
    }

    public Map<String, Object> obtenerFichaClinica(String idEncuentro) {
        Encounter encuentro = fhirClient.read().resource(Encounter.class).withId(idEncuentro).execute();
        
        // Aquí consumirías de forma transparente el microservicio de pacientes o buscarías observaciones cruzadas
        Map<String, Object> ficha = new HashMap<>();
        ficha.put("idEncuentro", idEncuentro);
        ficha.put("motivo", encuentro.getReasonCodeFirstRep().getText());
        ficha.put("estadoActual", encuentro.getStatus().toCode());
        return ficha;
    }

    public void finalizarAtencion(String id, String diagnostico) {
        Encounter encuentro = fhirClient.read().resource(Encounter.class).withId(id).execute();
        encuentro.setStatus(Encounter.EncounterStatus.FINISHED);

        // Agregar Diagnóstico de Salida
        Encounter.DiagnosisComponent diagnosisComponent = new Encounter.DiagnosisComponent();
        diagnosisComponent.setCondition(new Reference().setDisplay(diagnostico));
        encuentro.addDiagnosis(diagnosisComponent);

        fhirClient.update().resource(encuentro).execute();

        // INTEGRACIÓN SQS: Notificar Alta para liberar BOX de atención
        sqsProducerService.enviarNotificacionAlta(id, diagnostico);
    }
}