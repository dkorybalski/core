package pl.edu.amu.wmi.service.projectdefense;

import com.opencsv.exceptions.CsvException;

import java.io.PrintWriter;

public interface ProjectDefenseSummaryService {

    void exportDefenseScheduleSummaryData(PrintWriter writer, String studyYear) throws CsvException;
}
