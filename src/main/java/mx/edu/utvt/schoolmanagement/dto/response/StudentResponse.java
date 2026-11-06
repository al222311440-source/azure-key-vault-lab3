package mx.edu.utvt.schoolmanagement.dto.response;

import java.time.LocalDate;
public record StudentResponse(Long id, String studentNumber, String firstName, String paternalSurname,
    String maternalSurname, String email, LocalDate dateOfBirth, boolean active) {}
