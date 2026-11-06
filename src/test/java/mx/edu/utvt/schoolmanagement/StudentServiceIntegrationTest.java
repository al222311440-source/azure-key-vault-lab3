package mx.edu.utvt.schoolmanagement;

import mx.edu.utvt.schoolmanagement.dto.request.*;
import mx.edu.utvt.schoolmanagement.exception.*;
import mx.edu.utvt.schoolmanagement.repository.StudentRepository;
import mx.edu.utvt.schoolmanagement.service.StudentService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class StudentServiceIntegrationTest {
    @Autowired
    StudentService service;
    @Autowired
    StudentRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private CreateStudentRequest request(String number) {
        return new CreateStudentRequest(number, "Ana", "Lopez", null, "ana@utvtol.edu.mx", null);
    }

    @Test
    void createsReadsUpdatesAndDeactivatesStudent() {
        var created = service.create(request("UTVT-001"));
        assertNotNull(created.id());
        assertTrue(created.active());
        assertEquals("Ana", service.findById(created.id()).firstName());
        var updated = service.update(created.id(),
                new UpdateStudentRequest("UTVT-002", "Ann", "Lopez", null, "ann@utvtol.edu.mx", null));
        assertEquals("UTVT-002", updated.studentNumber());
        assertFalse(service.updateStatus(created.id(), new UpdateStudentStatusRequest(false)).active());
        assertEquals(1, service.findAll(0, 20, false, "UTVT-002").totalElements());
        assertEquals(0, service.findAll(0, 20, true, null).totalElements());
    }

    @Test
    void rejectsDuplicateStudentNumbersOnCreationAndUpdate() {
        service.create(request("UTVT-001"));
        assertThrows(DuplicateStudentNumberException.class, () -> service.create(request("UTVT-001")));
        var second = service.create(request("UTVT-002"));
        assertThrows(DuplicateStudentNumberException.class, () -> service.update(second.id(),
                new UpdateStudentRequest("UTVT-001", "Ana", "Lopez", null, "ana@utvtol.edu.mx", null)));
        assertEquals("UTVT-002", service.findById(second.id()).studentNumber());
    }

    @Test
    void reportsMissingStudent() {
        assertThrows(StudentNotFoundException.class, () -> service.findById(999L));
    }
}
