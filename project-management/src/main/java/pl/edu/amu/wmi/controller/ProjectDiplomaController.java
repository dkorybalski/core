package pl.edu.amu.wmi.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.client.DiplomasRestClient;

@RestController
@RequestMapping("/project-diplomas")
@AllArgsConstructor
public class ProjectDiplomaController {
    private final DiplomasRestClient diplomasRestClient;

    @Secured({"PROJECT_ADMIN", "COORDINATOR"})
    @GetMapping
    public ResponseEntity<?> getDiplomas(HttpServletRequest request) {
        ResponseEntity<String> response = diplomasRestClient.forwardRequest(request);
        return ResponseEntity.status(response.getStatusCode())
            .body(response.getBody());
    }

    @Secured({"STUDENT", "PROJECT_ADMIN", "COORDINATOR"})
    @GetMapping("/{project-id}")
    public ResponseEntity<?> getDiplomaByProjectId(HttpServletRequest request) {
        ResponseEntity<String> response = diplomasRestClient.forwardRequest(request);
        return ResponseEntity.status(response.getStatusCode())
            .body(response.getBody());
    }

    @Secured({"STUDENT"})
    @PutMapping
    public ResponseEntity<?> updateDiploma(HttpServletRequest request) {
        ResponseEntity<String> response = diplomasRestClient.forwardRequest(request);
        return ResponseEntity.status(response.getStatusCode())
            .body(response.getBody());
    }

    @Secured({"STUDENT"})
    @PutMapping("/chapters")
    public ResponseEntity<?> updateDiplomaChapter(HttpServletRequest request) {
        ResponseEntity<String> response = diplomasRestClient.forwardRequest(request);
        return ResponseEntity.status(response.getStatusCode())
            .body(response.getBody());
    }

    @Secured({"PROJECT_ADMIN", "COORDINATOR"})
    @GetMapping("/export")
    public ResponseEntity<?> exportDiplomas(HttpServletRequest request) {
        ResponseEntity<String> response = diplomasRestClient.forwardRequest(request);
        return ResponseEntity.status(response.getStatusCode())
            .body(response.getBody());
    }
}
