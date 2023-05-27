package pl.edu.amu.wmi.service;

import org.springframework.web.multipart.MultipartFile;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;

public interface DataFeedService {

    DataFeedType getType();

    void saveRecords(MultipartFile data, String studyYear);
}
