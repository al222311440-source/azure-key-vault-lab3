package mx.edu.utvt.schoolmanagement.mapper;

import mx.edu.utvt.schoolmanagement.entity.Student;
import mx.edu.utvt.schoolmanagement.dto.request.*;
import mx.edu.utvt.schoolmanagement.dto.response.StudentResponse;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {
    public Student toEntity(CreateStudentRequest request) {
        Student student = new Student();
        student.setStudentNumber(request.studentNumber());
        student.setFirstName(request.firstName());
        student.setPaternalSurname(request.paternalSurname());
        student.setMaternalSurname(request.maternalSurname());
        student.setEmail(request.email());
        student.setDateOfBirth(request.dateOfBirth());
        return student;
    }

    public void update(Student student, UpdateStudentRequest request) {
        student.setStudentNumber(request.studentNumber());
        student.setFirstName(request.firstName());
        student.setPaternalSurname(request.paternalSurname());
        student.setMaternalSurname(request.maternalSurname());
        student.setEmail(request.email());
        student.setDateOfBirth(request.dateOfBirth());
    }

    public StudentResponse toResponse(Student s) {
        return new StudentResponse(s.getId(), s.getStudentNumber(), s.getFirstName(), s.getPaternalSurname(),
                s.getMaternalSurname(), s.getEmail(), s.getDateOfBirth(), s.isActive());
    }
}
