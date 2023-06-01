package pl.edu.amu.wmi.service;

import com.opencsv.exceptions.CsvException;

import java.io.Writer;

public interface DataFeedExportService {

    void exportData(Writer writer, String studyYear) throws CsvException;

}
