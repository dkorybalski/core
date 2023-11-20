package pl.edu.amu.wmi.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.model.user.*;
import pl.edu.amu.wmi.service.SessionDataService;
import pl.edu.amu.wmi.service.StudentService;
import pl.edu.amu.wmi.service.SupervisorService;
import pl.edu.amu.wmi.service.UserService;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final SupervisorService supervisorService;

    private final StudentService studentService;

    private final UserService userService;

    private final SessionDataService sessionDataService;

    @Autowired
    public UserController(SupervisorService supervisorService, StudentService studentService, UserService userService, SessionDataService sessionDataService) {
        this.supervisorService = supervisorService;
        this.studentService = studentService;
        this.userService = userService;
        this.sessionDataService = sessionDataService;
    }

    @GetMapping("")
    public ResponseEntity<UserDTO> getUser(@RequestHeader("study-year") String studyYear) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.getUser(userDetails.getUsername(), studyYear));
    }

    @PutMapping("/study-year")
    public ResponseEntity<UserDTO> updateStudyYear(@RequestHeader("study-year") String studyYear, @RequestBody ActualStudyYearDTO updatedStudyYear) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        sessionDataService.updateActualStudyYear(updatedStudyYear.studyYear(), userDetails.getUsername());
        return ResponseEntity.ok(userService.getUser(userDetails.getUsername(), studyYear));
    }

    @GetMapping("/supervisor")
    public ResponseEntity<List<SupervisorDTO>> getSupervisors() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok()
                .body(supervisorService.findAll());
    }

    @PostMapping("/supervisor")
    public ResponseEntity<SupervisorDTO> createSupervisor(@RequestHeader("study-year") String studyYear ,@RequestBody SupervisorCreationRequestDTO supervisor) {
        return ResponseEntity.ok()
                .body(supervisorService.create(supervisor, studyYear));
    }


    @GetMapping("/student")
    public ResponseEntity<List<StudentDTO>> getStudents(@RequestHeader("study-year") String studyYear) {
        return ResponseEntity.ok()
                .body(studentService.findAll(studyYear));
    }

    @PostMapping("/student")
    public ResponseEntity<StudentDTO> createStudent(@RequestHeader("study-year") String studyYear, @RequestBody StudentCreationRequestDTO student) {
        return ResponseEntity.ok()
                .body(studentService.create(student, studyYear));
    }
}
