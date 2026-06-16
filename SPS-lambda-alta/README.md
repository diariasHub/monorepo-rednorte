# FaaS - Función Lambda para Alta de Pacientes

Esta carpeta contiene el código de la función Serverless (Lambda) que forma parte de la arquitectura distribuida del sistema RedNorte. Su propósito es actuar como **Consumidor** de la cola de SQS para procesar eventos asíncronos cuando un paciente recibe el alta médica.

## 🚀 Despliegue en AWS (Consola)

1. Ingresa a la consola de AWS y dirígete al servicio **Lambda**.
2. Haz clic en **Create function**.
3. Selecciona **Author from scratch**.
4. Nombre de la función: `ProcesarAltaUrgencias`.
5. Runtime: **Node.js 20.x** (o superior).
6. Execution Role: Selecciona "Use an existing role" y escoge el rol predeterminado de tu entorno educativo, usualmente `LabRole` o `vockey` (Debe tener permisos para leer de SQS).
7. Dale a **Create function**.

## 📦 Configuración del Código

1. Una vez creada la función, ve a la pestaña **Code**.
2. Copia y pega el contenido del archivo `index.mjs` de este repositorio en el editor de código web de AWS (sobreescribiendo el código por defecto).
3. Haz clic en el botón **Deploy** para guardar los cambios en la nube.

## 🔗 Integración con Amazon SQS (Event Trigger)

Para que esta función se ejecute automáticamente cuando `ms-urgencias-flujo` envía un mensaje de alta:
1. En el panel de control de tu función Lambda, haz clic en **+ Add trigger**.
2. Selecciona **SQS** del menú desplegable.
3. Elige la cola que creaste (ej. `UrgenciasNotificacionesQueue`).
4. Deja el Batch size en `10` y dale a **Add**.

## 🧪 Pruebas (Test)

Para probar manualmente si la función está respondiendo:
1. Ve a la pestaña **Test** en Lambda.
2. Crea un nuevo evento de prueba y usa la plantilla de SQS o pega este JSON simulado que enviaría nuestro servicio Java:
```json
{
  "Records": [
    {
      "messageId": "19dd0b57-b21e-4ac1-bd88-01bbb068cb78",
      "body": "{\"encounterId\": \"1234\", \"diagnostico\": \"Esguince de tobillo\", \"estado\": \"ALTA\", \"accion\": \"LIBERAR_BOX\"}"
    }
  ]
}
```
3. Ejecuta el test y verifica en los logs de ejecución que el mensaje de "✅ ÉXITO: El Box de atención... ha sido liberado" aparece en pantalla.
