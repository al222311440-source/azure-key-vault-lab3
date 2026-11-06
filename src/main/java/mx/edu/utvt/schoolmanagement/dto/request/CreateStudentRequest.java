package mx.edu.utvt.schoolmanagement.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateStudentRequest(
        @NotBlank @Size(max = 30) @Pattern(regexp = "[A-Za-z0-9-]+") String studentNumber,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String paternalSurname,
        @Size(max = 100) String maternalSurname,
        @NotBlank @Email @Size(max = 254) String email,
        @Past LocalDate dateOfBirth) {
}
