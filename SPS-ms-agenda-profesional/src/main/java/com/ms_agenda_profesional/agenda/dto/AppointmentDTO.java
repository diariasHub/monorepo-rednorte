package com.ms_agenda_profesional.agenda.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDTO {
    private String id;
    private String patientId;
    private String patientName;
    private String practitionerId;
    private String status;
    private String description;
    private Date start;
    private Date end;
}
