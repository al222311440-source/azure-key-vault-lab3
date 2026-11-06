package mx.edu.utvt.schoolmanagement.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateStudentStatusRequest(@NotNull Boolean active) {
}
