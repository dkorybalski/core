package pl.edu.amu.wmi.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.client.DiplomasRestClient;
import pl.edu.amu.wmi.model.diploma.*;

import java.util.List;

@RestController
@RequestMapping("/project-diplomas")
@AllArgsConstructor
public class ProjectDiplomaController {
    private final DiplomasRestClient diplomasRestClient;

    @Secured({"PROJECT_ADMIN", "COORDINATOR"})
    @GetMapping
    public ResponseEntity<?> getDiplomas(HttpServletRequest request) {
        String body = diplomasRestClient.forwardRequest(request)
            .getBody();
        System.out.println(body);
        return ResponseEntity.ok(
            body
        );
    }

    @Secured({"STUDENT", "PROJECT_ADMIN", "COORDINATOR"})
    @GetMapping("/{project-id}")
    public ResponseEntity<?> getDiplomaByProjectId(@PathVariable("project-id") Integer projectId,
                                                   HttpServletRequest request) {
        diplomasRestClient.forwardRequest(request);
        return ResponseEntity.ok(
            new DiplomaDTO("Title en", "Title pl", "DESC", 1,
                List.of(new DiplomaChapterDTO("Chapter 1", "DESC", "student1"))));
    }

    @Secured({"STUDENT"})
    @PutMapping
    public ResponseEntity<?> updateDiploma(@RequestBody DiplomaAddOrUpdateDTO dto,
                                           HttpServletRequest request) {
        diplomasRestClient.forwardRequest(request);
        System.out.println(dto);
        return ResponseEntity.ok()
            .build();
    }

    @Secured({"STUDENT"})
    @PutMapping("/chapters")
    public ResponseEntity<?> updateDiplomaChapter(@RequestBody DiplomaChapterAddOrUpdateDTO dto,
                                                  HttpServletRequest request) {
        diplomasRestClient.forwardRequest(request);
        System.out.println(dto);
        return ResponseEntity.ok()
            .build();
    }
}
