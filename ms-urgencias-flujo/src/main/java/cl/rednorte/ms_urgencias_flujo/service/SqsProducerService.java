package cl.rednorte.ms_urgencias_flujo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import jakarta.annotation.PostConstruct;

@Service
public class SqsProducerService {

    @Value("${aws.sqs.queue.url:}")
    private String queueUrl;

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    private SqsClient sqsClient;

    @PostConstruct
    public void init() {
        try {
            this.sqsClient = SqsClient.builder()
                .region(Region.of(awsRegion))
                .build(); // Utiliza DefaultCredentialsProvider (IAM Role)
            System.out.println("AWS SQS Client initialized for region: " + awsRegion);
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize SQS Client. Check AWS credentials. " + e.getMessage());
        }
    }

    public void enviarNotificacionAlta(String encounterId, String diagnostico) {
        if (queueUrl == null || queueUrl.isEmpty()) {
            System.out.println("SQS Queue URL not configured. Skipping SQS message for Encounter: " + encounterId);
            return;
        }

        if (sqsClient == null) {
            System.err.println("SQS Client is null. Cannot send message.");
            return;
        }

        try {
            String messageBody = String.format("{\"encounterId\": \"%s\", \"diagnostico\": \"%s\", \"estado\": \"ALTA\", \"accion\": \"LIBERAR_BOX\"}", encounterId, diagnostico);
            
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .delaySeconds(0)
                .build();
                
            sqsClient.sendMessage(sendMsgRequest);
            System.out.println("Successfully sent message to SQS for discharge of encounter: " + encounterId);
        } catch (Exception e) {
            System.err.println("Error sending message to SQS: " + e.getMessage());
        }
    }
}
