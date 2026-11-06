package mx.edu.utvt.schoolmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import mx.edu.utvt.schoolmanagement.dto.request.*;
import mx.edu.utvt.schoolmanagement.dto.response.*;
import mx.edu.utvt.schoolmanagement.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Students", description = "Student registration, queries and status management")
@RestController
@RequestMapping("/api/v1/students")
public class StudentController {
    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @Operation(summary = "Register a student")
    @ApiResponses({ @ApiResponse(responseCode = "201", description = "Student created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Student number already exists") })
    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody CreateStudentRequest request) {
        StudentResponse response = service.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "List students with pagination and optional filters")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Paginated student list"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters") })
    @GetMapping
    public PageResponse<StudentResponse> findAll(@RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) Boolean active, @RequestParam(required = false) String studentNumber) {
        return service.findAll(page, size, active, studentNumber);
    }

    @Operation(summary = "Get a student by ID")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Student found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID"),
            @ApiResponse(responseCode = "404", description = "Student not found") })
    @GetMapping("/{id}")
    public StudentResponse findById(@PathVariable @Positive Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Replace editable student details")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Student updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "409", description = "Update conflict") })
    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable @Positive Long id, @Valid @RequestBody UpdateStudentRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Activate or deactivate a student")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "409", description = "Concurrent update conflict") })
    @PatchMapping("/{id}/status")
    public StudentResponse updateStatus(@PathVariable @Positive Long id,
            @Valid @RequestBody UpdateStudentStatusRequest request) {
        return service.updateStatus(id, request);
    }
}
