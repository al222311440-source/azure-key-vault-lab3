package mx.edu.utvt.schoolmanagement.service;

import java.util.List;
import java.util.Optional;
import mx.edu.utvt.schoolmanagement.dto.request.CreateStudentRequest;
import mx.edu.utvt.schoolmanagement.dto.request.UpdateStudentRequest;
import mx.edu.utvt.schoolmanagement.dto.request.UpdateStudentStatusRequest;
import mx.edu.utvt.schoolmanagement.dto.response.StudentResponse;
import mx.edu.utvt.schoolmanagement.entity.Student;
import mx.edu.utvt.schoolmanagement.exception.DuplicateStudentNumberException;
import mx.edu.utvt.schoolmanagement.exception.StudentNotFoundException;
import mx.edu.utvt.schoolmanagement.mapper.StudentMapper;
import mx.edu.utvt.schoolmanagement.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    @Mock
    private StudentRepository repository;

    @Mock
    private StudentMapper mapper;

    @InjectMocks
    private StudentService service;

    @Test
    void createsAndPersistsStudent() {
        CreateStudentRequest request = createRequest("STU-001");
        Student student = student("STU-001");
        StudentResponse expected = response(1L, "STU-001", true);
        when(repository.existsByStudentNumber("STU-001")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(student);
        when(repository.saveAndFlush(student)).thenReturn(student);
        when(mapper.toResponse(student)).thenReturn(expected);

        assertEquals(expected, service.create(request));

        verify(repository).existsByStudentNumber("STU-001");
        verify(repository).saveAndFlush(student);
    }

    @Test
    void rejectsDuplicateStudentNumberOnCreation() {
        CreateStudentRequest request = createRequest("STU-001");
        when(repository.existsByStudentNumber("STU-001")).thenReturn(true);

        DuplicateStudentNumberException exception = assertThrows(
                DuplicateStudentNumberException.class, () -> service.create(request));

        assertEquals("Student number already exists: STU-001", exception.getMessage());
        verifyNoInteractions(mapper);
        verify(repository, never()).saveAndFlush(any(Student.class));
    }

    @Test
    void findsStudentById() {
        Student student = student("STU-001");
        StudentResponse expected = response(1L, "STU-001", true);
        when(repository.findById(1L)).thenReturn(Optional.of(student));
        when(mapper.toResponse(student)).thenReturn(expected);

        assertEquals(expected, service.findById(1L));
    }

    @Test
    void reportsMissingStudentById() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        StudentNotFoundException exception = assertThrows(
                StudentNotFoundException.class, () -> service.findById(99L));

        assertEquals("Student not found: 99", exception.getMessage());
        verifyNoInteractions(mapper);
    }

    @Test
    void returnsPagedStudentsUsingRequestedPageAndSize() {
        Student student = student("STU-001");
        StudentResponse expectedStudent = response(1L, "STU-001", true);
        when(repository.findAll(ArgumentMatchers.<Specification<Student>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(student), PageRequest.of(2, 5), 11));
        when(mapper.toResponse(student)).thenReturn(expectedStudent);

        var result = service.findAll(2, 5, true, "STU-001");

        assertEquals(List.of(expectedStudent), result.content());
        assertEquals(2, result.page());
        assertEquals(5, result.size());
        assertEquals(11, result.totalElements());
        assertEquals(3, result.totalPages());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(ArgumentMatchers.<Specification<Student>>any(), pageable.capture());
        assertEquals(PageRequest.of(2, 5, Sort.by("id")), pageable.getValue());
    }

    @Test
    void updatesStudentDetails() {
        Student student = student("STU-001");
        UpdateStudentRequest request = new UpdateStudentRequest(
                "STU-002", "Ana Maria", "Lopez", "Diaz", "ana.maria@utvtol.edu.mx", null);
        StudentResponse expected = response(1L, "STU-002", true);
        when(repository.findById(1L)).thenReturn(Optional.of(student));
        when(repository.existsByStudentNumberAndIdNot("STU-002", 1L)).thenReturn(false);
        when(repository.saveAndFlush(student)).thenReturn(student);
        when(mapper.toResponse(student)).thenReturn(expected);

        assertEquals(expected, service.update(1L, request));

        verify(mapper).update(student, request);
        verify(repository).saveAndFlush(student);
    }

    @Test
    void rejectsStudentNumberUsedByAnotherStudentOnUpdate() {
        Student student = student("STU-001");
        UpdateStudentRequest request = new UpdateStudentRequest(
                "STU-002", "Ana", "Lopez", null, "ana@utvtol.edu.mx", null);
        when(repository.findById(1L)).thenReturn(Optional.of(student));
        when(repository.existsByStudentNumberAndIdNot("STU-002", 1L)).thenReturn(true);

        assertThrows(DuplicateStudentNumberException.class, () -> service.update(1L, request));

        verify(mapper, never()).update(any(Student.class), any(UpdateStudentRequest.class));
        verify(repository, never()).saveAndFlush(any(Student.class));
    }

    @Test
    void updatesStudentActiveStatus() {
        Student student = student("STU-001");
        StudentResponse expected = response(1L, "STU-001", false);
        when(repository.findById(1L)).thenReturn(Optional.of(student));
        when(repository.saveAndFlush(student)).thenReturn(student);
        when(mapper.toResponse(student)).thenReturn(expected);

        assertEquals(expected, service.updateStatus(1L, new UpdateStudentStatusRequest(false)));

        assertFalse(student.isActive());
        verify(repository).saveAndFlush(student);
    }

    private CreateStudentRequest createRequest(String studentNumber) {
        return new CreateStudentRequest(studentNumber, "Ana", "Lopez", null, "ana@utvtol.edu.mx", null);
    }

    private Student student(String studentNumber) {
        Student student = new Student();
        student.setStudentNumber(studentNumber);
        student.setFirstName("Ana");
        student.setPaternalSurname("Lopez");
        student.setEmail("ana@utvtol.edu.mx");
        return student;
    }

    private StudentResponse response(Long id, String studentNumber, boolean active) {
        return new StudentResponse(id, studentNumber, "Ana", "Lopez", null, "ana@utvtol.edu.mx", null, active);
    }
}