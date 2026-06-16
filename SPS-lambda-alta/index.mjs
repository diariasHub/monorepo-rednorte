export const handler = async (event) => {
    console.log("Recibiendo lote de mensajes desde SQS...");

    for (const record of event.Records) {
        try {
            // El body del mensaje SQS enviado por Java
            const body = JSON.parse(record.body);
            console.log(`[ALTA MÉDICA] Procesando encuentro ID: ${body.encounterId}`);
            console.log(`Diagnóstico: ${body.diagnostico}`);
            console.log(`Acción requerida: ${body.accion}`);

            if (body.accion === "LIBERAR_BOX") {
                // Aquí iría la lógica de integración con DynamoDB o llamadas a otro API
                console.log(`✅ ÉXITO: El Box de atención asociado al encuentro ${body.encounterId} ha sido liberado en el sistema central.`);
            }
        } catch (error) {
            console.error("Error parseando el mensaje SQS: ", error);
        }
    }

    return {
        statusCode: 200,
        body: JSON.stringify('Procesamiento de SQS exitoso!'),
    };
};
