package pl.edu.amu.wmi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data")
public class DataFeedController {

    @GetMapping("/")
    public ResponseEntity<String> testController() {
        return ResponseEntity.ok()
                .body("Success!");
    }

}
