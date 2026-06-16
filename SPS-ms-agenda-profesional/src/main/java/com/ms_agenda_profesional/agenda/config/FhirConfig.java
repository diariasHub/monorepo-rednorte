package com.ms_agenda_profesional.agenda.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirConfig {
    
    @Value("${FHIR_SERVER_URL:http://hapi-fhir:8080/fhir}")
    private String serverUrl;

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4();
    }

    @Bean
    public IGenericClient fhirClient(FhirContext fhirContext) {
        fhirContext.getRestfulClientFactory().setConnectTimeout(5000);
        fhirContext.getRestfulClientFactory().setSocketTimeout(5000);
        return fhirContext.newRestfulGenericClient(serverUrl);
    }
}
