package mx.edu.utvt.schoolmanagement.mapper;

import java.time.LocalDate;
import mx.edu.utvt.schoolmanagement.dto.request.CreateStudentRequest;
import mx.edu.utvt.schoolmanagement.dto.request.UpdateStudentRequest;
import mx.edu.utvt.schoolmanagement.dto.response.StudentResponse;
import mx.edu.utvt.schoolmanagement.entity.Student;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentMapperTest {
    private final StudentMapper mapper = new StudentMapper();

    @Test
    void mapsCreateRequestToEntity() {
        LocalDate dateOfBirth = LocalDate.of(2005, 4, 12);
        CreateStudentRequest request = new CreateStudentRequest(
                "STU-001", "Ana", "Lopez", "Diaz", "ana@utvtol.edu.mx", dateOfBirth);

        Student student = mapper.toEntity(request);

        assertEquals("STU-001", student.getStudentNumber());
        assertEquals("Ana", student.getFirstName());
        assertEquals("Lopez", student.getPaternalSurname());
        assertEquals("Diaz", student.getMaternalSurname());
        assertEquals("ana@utvtol.edu.mx", student.getEmail());
        assertEquals(dateOfBirth, student.getDateOfBirth());
        assertTrue(student.isActive());
    }

    @Test
    void updatesEditableFieldsWithoutChangingActiveStatus() {
        Student student = new Student();
        student.setStudentNumber("STU-001");
        student.setFirstName("Ana");
        student.setPaternalSurname("Lopez");
        student.setEmail("ana@utvtol.edu.mx");
        student.setActive(false);
        LocalDate dateOfBirth = LocalDate.of(2005, 4, 12);
        UpdateStudentRequest request = new UpdateStudentRequest(
                "STU-002", "Ana Maria", "Garcia", "Diaz", "ana.maria@utvtol.edu.mx", dateOfBirth);

        mapper.update(student, request);

        assertEquals("STU-002", student.getStudentNumber());
        assertEquals("Ana Maria", student.getFirstName());
        assertEquals("Garcia", student.getPaternalSurname());
        assertEquals("Diaz", student.getMaternalSurname());
        assertEquals("ana.maria@utvtol.edu.mx", student.getEmail());
        assertEquals(dateOfBirth, student.getDateOfBirth());
        assertFalse(student.isActive());
    }

    @Test
    void mapsEntityToResponse() {
        LocalDate dateOfBirth = LocalDate.of(2005, 4, 12);
        Student student = new Student();
        student.setStudentNumber("STU-001");
        student.setFirstName("Ana");
        student.setPaternalSurname("Lopez");
        student.setMaternalSurname("Diaz");
        student.setEmail("ana@utvtol.edu.mx");
        student.setDateOfBirth(dateOfBirth);
        student.setActive(false);

        StudentResponse response = mapper.toResponse(student);

        assertEquals(new StudentResponse(
                null, "STU-001", "Ana", "Lopez", "Diaz", "ana@utvtol.edu.mx", dateOfBirth, false), response);
    }
}