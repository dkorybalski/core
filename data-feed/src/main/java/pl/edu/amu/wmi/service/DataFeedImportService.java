package pl.edu.amu.wmi.service;

import com.opencsv.exceptions.CsvException;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;

public interface DataFeedImportService {

    DataFeedType getType();

    void saveRecords(MultipartFile data, String studyYear) throws CsvException;
}
