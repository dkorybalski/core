package pl.edu.amu.wmi.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.model.diploma.*;

import java.util.List;

@RestController
@RequestMapping("/project-diplomas")
@AllArgsConstructor
public class ProjectDiplomaController {

    @Secured({"PROJECT_ADMIN", "COORDINATOR"})
    @GetMapping
    public ResponseEntity<List<DiplomaDTO>> getDiplomas() {
        return ResponseEntity.ok(List.of(
            new DiplomaDTO("Title en", "Title pl", "DESC", 1,
                List.of(new DiplomaChapterDTO("Chapter 1", "DESC", "student1"))),
            new DiplomaDTO("Title en 2", "Title pl 2", "DESC 2", 1,
                List.of(new DiplomaChapterDTO("Chapter 1 --", "DESC --", "student1")))
        ));
    }

    @Secured({"STUDENT", "PROJECT_ADMIN", "COORDINATOR"})
    @GetMapping("/{project-id}")
    public ResponseEntity<DiplomaDTO> getDiplomaByProjectId(@PathVariable("project-id") Integer projectId) {
        return ResponseEntity.ok(
            new DiplomaDTO("Title en", "Title pl", "DESC", 1,
                List.of(new DiplomaChapterDTO("Chapter 1", "DESC", "student1"))));
    }

    @Secured({"STUDENT"})
    @PutMapping
    public ResponseEntity<Void> updateDiploma(@RequestBody DiplomaAddOrUpdateDTO dto) {
        System.out.println(dto);
        return ResponseEntity.ok()
            .build();
    }

    @Secured({"STUDENT"})
    @PutMapping("/chapters")
    public ResponseEntity<Void> updateDiplomaChapter(@RequestBody DiplomaChapterAddOrUpdateDTO dto) {
        System.out.println(dto);
        return ResponseEntity.ok()
            .build();
    }
}
