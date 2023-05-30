package pl.edu.amu.wmi.service;

import java.io.Writer;

public interface DataFeedExportService {

    void exportData(Writer writer, String studyYear);

}
