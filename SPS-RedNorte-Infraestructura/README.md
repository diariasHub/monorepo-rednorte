# 🏥 Arquitectura de Microservicios: RedNorte

Este repositorio contiene la orquestación (Docker Compose) de la plataforma RedNorte. El proyecto está dividido en los siguientes microservicios independientes:

| Módulo / Servicio | Descripción | Repositorio |
| :--- | :--- | :--- |
| 🌐 **Frontend** | Portal de pacientes y administrativos (React) | [Ir al código]() |
| 🛡️ **API Gateway** | Enrutamiento y validación JWT | [Ir al código](https://github.com/diariasHub/SPS-rednorte-api-gateway) |
| 👤 **MS Usuarios** | Gestión de pacientes y médicos (FHIR Patient,Practitioner) | [Ir al código](https://github.com/diariasHub/SPS-ms-usuarios-identidad.git) |
| 🏥 **MS Centros** | Administración de ubicaciones (FHIR Organization,Location) | [Ir al código](https://github.com/diariasHub/SPS-ms-red-centros.git) |
| 📅 **MS Agenda-profesional** | Gestión de bloques de tiempo (FHIR Schedule,Slot) | [Ir al código](https://github.com/diariasHub/SPS-ms-agenda-profesional.git) |
| 🚑 **MS reservas-citas** | Gestión de la toma, modificación y cancelación de horas programadas (FHIR: Appointment)| [Ir al código](https://github.com/diariasHub/SPS-ms-reservas-citas.git) |
| 🚑 **MS urgencias-flujo** |  Control de la sala de espera dinámica y el triaje (C1-C5) (FHIR Encounter) | [Ir al código](https://github.com/diariasHub/ms-urgencias-flujo.git) |
| 🚑 **MS ficha-clinica** | Registro de datos médicos durante la atención. (FHIR: Observation) | [Ir al código](https://github.com/diariasHub/SPS-ms-ficha-clinica.git) |
| 🚑 **MS notificaciones** |  Sistema asíncrono para enviar alertas y confirmaciones (SMS, correos) (FHIR: Communication) | [Ir al código](https://github.com/diariasHub/SPS-ms-notificaciones.git) |

> **Nota:** Todos los microservicios se comunican con un servidor central **HAPI FHIR** y una base de datos **PostgreSQL** compartida, configurada en este mismo repositorio.


Crear una carpeta guardar estos 2 achivos y ejcutar una shell con "docker-compose up -d" tener docker desktop con las imagenes corriendo.

DETALLE DE MICROSERVICIOS UTILIZADOS.

Microservicios de Negocio (Spring Boot)
Estos son los 7 contenedores independientes que procesan la lógica del hospital e interactúan con el servidor FHIR central.
-------------------------------------------------------------------------
ms-usuarios-identidad

Propósito: Gestión de perfiles, autenticación básica y roles.

Recursos FHIR: Patient (Pacientes), Practitioner (Médicos/Personal).
-------------------------------------------------------------------------
ms-red-centros

Propósito: Administración de la estructura física y administrativa del hospital.

Recursos FHIR: Organization (La institución), Location (Edificios, pisos, boxes).
-------------------------------------------------------------------------
ms-agenda-profesional

Propósito: Control de la disponibilidad de los médicos y sus bloques de atención.

Recursos FHIR: Schedule (Agendas), Slot (Bloques de tiempo específicos).
--------------------------------------------------------------------------
ms-reservas-citas

Propósito: Gestión de la toma, modificación y cancelación de horas programadas.

Recursos FHIR: Appointment (La cita médica agendada).
---------------------------------------------------------------------------
ms-urgencias-flujo

Propósito: Control de la sala de espera dinámica y el triaje (C1-C5).

Recursos FHIR: Encounter (El evento de la visita médica y su estado).
-----------------------------------------------------------------------------
ms-ficha-clinica

Propósito: Registro de datos médicos durante la atención.

Recursos FHIR: Observation (Signos vitales, notas), Condition (Diagnósticos, enfermedades base).
--------------------------------------------------------------------------------
ms-notificaciones

Propósito: Sistema asíncrono para enviar alertas y confirmaciones (SMS, correos).

Recursos FHIR: Communication (El mensaje enviado y su payload).
---------------------------------------------------------------------------------
