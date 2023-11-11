package pl.edu.amu.wmi.service.exportdata;

import pl.edu.amu.wmi.model.enumeration.DataFeedType;

import java.io.Writer;

public interface DataFeedExportService {

    void exportData(Writer writer, String studyYear) throws Exception;

    DataFeedType getType();

}
