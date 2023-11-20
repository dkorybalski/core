package pl.edu.amu.wmi.service;

public interface SessionDataService {

    void updateActualStudyYear(String updatedStudyYear, String indexNumber);

    String findActualStudyYear(String indexNumber);
}
