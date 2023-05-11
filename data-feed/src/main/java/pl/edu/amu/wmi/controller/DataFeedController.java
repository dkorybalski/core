package pl.edu.amu.wmi.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.DataFeedService;
import pl.edu.amu.wmi.service.impl.DataFeedServiceFactory;


@RestController
@RequestMapping("/data")
@Slf4j
public class DataFeedController {

    private final DataFeedServiceFactory dataFeedServiceFactory;

    public DataFeedController(DataFeedServiceFactory dataFeedServiceFactory) {
        this.dataFeedServiceFactory = dataFeedServiceFactory;
    }

    @PostMapping("/students")
    public ResponseEntity<Void> createStudents(@RequestParam MultipartFile data) {
        // TODO: 5/11/2023 make it async
        DataFeedService service = DataFeedServiceFactory.getService(DataFeedType.NEW_STUDENT);
        service.saveRecords(data);

        return ResponseEntity.ok().build();
    }

}
