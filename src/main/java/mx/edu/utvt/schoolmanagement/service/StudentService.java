package mx.edu.utvt.schoolmanagement.service;

import mx.edu.utvt.schoolmanagement.dto.request.*;
import mx.edu.utvt.schoolmanagement.dto.response.*;
import mx.edu.utvt.schoolmanagement.entity.Student;
import mx.edu.utvt.schoolmanagement.exception.*;
import mx.edu.utvt.schoolmanagement.mapper.StudentMapper;
import mx.edu.utvt.schoolmanagement.repository.StudentRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudentService {
    private static final Logger log = LoggerFactory.getLogger(StudentService.class);
    private final StudentRepository repository;
    private final StudentMapper mapper;

    public StudentService(StudentRepository repository, StudentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        if (repository.existsByStudentNumber(request.studentNumber()))
            throw new DuplicateStudentNumberException(request.studentNumber());
        StudentResponse response = mapper.toResponse(repository.saveAndFlush(mapper.toEntity(request)));
        log.info("Student created: id={}", response.id());
        return response;
    }

    public StudentResponse findById(Long id) {
        return mapper.toResponse(requireStudent(id));
    }

    public PageResponse<StudentResponse> findAll(int page, int size, Boolean active, String studentNumber) {
        Specification<Student> filters = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (active != null)
                predicates.add(cb.equal(root.get("active"), active));
            if (studentNumber != null && !studentNumber.isBlank())
                predicates.add(cb.equal(root.get("studentNumber"), studentNumber));
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return PageResponse
                .from(repository.findAll(filters, PageRequest.of(page, size, Sort.by("id"))).map(mapper::toResponse));
    }

    @Transactional
    public StudentResponse update(Long id, UpdateStudentRequest request) {
        Student student = requireStudent(id);
        if (repository.existsByStudentNumberAndIdNot(request.studentNumber(), id))
            throw new DuplicateStudentNumberException(request.studentNumber());
        mapper.update(student, request);
        StudentResponse response = mapper.toResponse(repository.saveAndFlush(student));
        log.info("Student updated: id={}", id);
        return response;
    }

    @Transactional
    public StudentResponse updateStatus(Long id, UpdateStudentStatusRequest request) {
        Student student = requireStudent(id);
        student.setActive(request.active());
        StudentResponse response = mapper.toResponse(repository.saveAndFlush(student));
        log.info("Student status updated: id={}, active={}", id, response.active());
        return response;
    }

    private Student requireStudent(Long id) {
        return repository.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
    }
}
