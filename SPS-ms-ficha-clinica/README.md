# Microservicio de Ficha Clínica (MS-FICHA-CLINICA)

Este microservicio es el componente central encargado de la gestión de la información clínica de los pacientes, implementando el estándar internacional **HL7 FHIR R4** para asegurar la interoperabilidad y portabilidad de los datos de salud.

## Descripción del Proyecto

El sistema permite el registro, consulta y administración de diversos recursos clínicos: encuentros médicos, observaciones de signos vitales, diagnósticos (condiciones), procedimientos y notas evolutivas. Está diseñado bajo una arquitectura de microservicios robusta, escalable y orientada a la interoperabilidad.

## Especificaciones Técnicas

### Stack Tecnológico
*   **Lenguaje:** Java 21 LTS
*   **Framework:** Spring Boot 4.0.x (basado en Jakarta EE)
*   **Persistencia / Integración clínica:** HAPI FHIR R4 (cliente `IGenericClient`) — los recursos clínicos se almacenan en el servidor FHIR central
*   **Base de Datos:** PostgreSQL (gestión centralizada en la infraestructura; este servicio no administra tablas relacionales directamente)
*   **Estándar de Interoperabilidad:** HAPI FHIR R4
*   **Gestión de Dependencias:** Maven
*   **Librerías principales:** Lombok, Jakarta Validation

### Arquitectura de Software
El proyecto implementa un patrón de diseño en capas para asegurar la separación de preocupaciones y facilitar el mantenimiento:
*   **Controller:** Exposición de endpoints REST y manejo de solicitudes HTTP.
*   **Service:** Implementación de la lógica de negocio y reglas de dominio.
*   **Repository:** Abstracción del acceso a datos y persistencia.
*   **DTO (Data Transfer Objects):** Definición de contratos para el intercambio de datos.
*   **Model/Entity:** Mapeo relacional de base de datos y modelos FHIR.
*   **Mapper:** Lógica de transformación entre entidades y DTOs.

## Requisitos del Sistema

*   Java Development Kit (JDK) 21.
*   Apache Maven 3.8 o superior.
*   Acceso a internet (para conexión a la base de datos externa en Neon).

## Configuración y Despliegue

### Entorno de Desarrollo
El microservicio opera por defecto en el puerto **8001**.

### Configuración
Este microservicio consume y produce recursos FHIR contra un servidor HAPI FHIR centralizado. Las conexiones principales se configuran mediante variables de entorno:

```properties
# URL del servidor FHIR (por defecto apunta al servicio orquestado por la infraestructura)
FHIR_SERVER_URL=http://hapi-fhir:8080/fhir

# Opcional: parámetros de conexión a una BD local/externa en caso de fallback (no requerido para operaciones FHIR)
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/${POSTGRES_DB}
SPRING_DATASOURCE_USERNAME=${POSTGRES_USER}
SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}
```

### Ejecución Local
Para iniciar la aplicación utilizando Maven:
```bash
mvn spring-boot:run
```

### Despliegue con Docker

Este microservicio se despliega a través del `docker-compose.yml` central en `SPS-RedNorte-Infraestructura`. No utilice el `docker-compose.yml` local para despliegues integrados con la plataforma.

### Comandos Principales (desde la infraestructura)
*   **Construir e iniciar**: `docker compose up --build -d`
*   **Ver logs en tiempo real**: `docker compose logs -f ms-ficha-clinica`
*   **Detener servicios**: `docker compose down`

Consulte `SPS-RedNorte-Infraestructura/ARQUITECTURA.md` para guías y detalles.

## Documentación de API

La documentación detallada de cada módulo se encuentra organizada en archivos individuales, facilitando la integración y pruebas mediante Postman u otras herramientas.

| Módulo | Documentación | Descripción |
| :--- | :--- | :--- |
| **Historia Clínica** | [clinical-history-endpoints.md](docs/clinical-history-endpoints.md) | Consulta integral del paciente. |
| **Encuentros** | [encounter-endpoints.md](docs/encounter-endpoints.md) | Gestión de atenciones médicas. |
| **Observaciones** | [observation-endpoints.md](docs/observation-endpoints.md) | Signos vitales y resultados. |
| **Condiciones** | [condition-endpoints.md](docs/condition-endpoints.md) | Diagnósticos y antecedentes. |
| **Procedimientos** | [procedure-endpoints.md](docs/procedure-endpoints.md) | Intervenciones realizadas. |
| **Notas Clínicas** | [clinical-note-endpoints.md](docs/clinical-note-endpoints.md) | Notas de evolución narrativa. |

## Estructura de Directorios

```text
├── docs/                        # Especificaciones técnicas de APIs
├── src/main/java/cl/rednorte/
│   ├── controller/             # Controladores REST
│   ├── service/                # Lógica de negocio
│   ├── repository/             # Capa de datos
│   ├── dto/                    # Modelos de transferencia
│   ├── model/                  # Entidades JPA y mapeos FHIR
│   └── mapper/                 # Transformadores de datos
└── src/main/resources/         # Configuración del sistema
```
