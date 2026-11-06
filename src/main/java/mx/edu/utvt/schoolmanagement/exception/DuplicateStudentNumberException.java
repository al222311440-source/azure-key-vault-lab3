package mx.edu.utvt.schoolmanagement.exception;

public class DuplicateStudentNumberException extends RuntimeException {
    public DuplicateStudentNumberException(String studentNumber) {
        super("Student number already exists: " + studentNumber);
    }
}
