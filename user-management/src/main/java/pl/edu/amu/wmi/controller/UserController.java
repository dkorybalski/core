package pl.edu.amu.wmi.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.model.StudentDTO;
import pl.edu.amu.wmi.model.SupervisorCreationRequestDTO;
import pl.edu.amu.wmi.model.SupervisorDTO;
import pl.edu.amu.wmi.service.StudentService;
import pl.edu.amu.wmi.service.SupervisorService;

import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final SupervisorService supervisorService;

    private final StudentService studentService;

    @Autowired
    public UserController(SupervisorService supervisorService, StudentService studentService) {
        this.supervisorService = supervisorService;
        this.studentService = studentService;
    }

    @GetMapping("/supervisor")
    public ResponseEntity<List<SupervisorDTO>> getSupervisors() {
        return ResponseEntity.ok()
                .body(supervisorService.findAll());
    }

    @PostMapping("/supervisor")
    public ResponseEntity<SupervisorDTO> createSupervisor(@RequestHeader("study-year") String studyYear,
                                                          @RequestHeader("user-index-number") String userIndexNumber,@RequestBody SupervisorCreationRequestDTO supervisor) {
        return ResponseEntity.ok()
                .body(supervisorService.create(supervisor, studyYear));
    }

    @GetMapping("/student")
    public ResponseEntity<List<StudentDTO>> getStudents() {
        return ResponseEntity.ok()
                .body(studentService.findAll());
    }
}
