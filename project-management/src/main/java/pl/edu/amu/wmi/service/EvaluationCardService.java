package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.entity.Project;

public interface EvaluationCardService {
    void addEmptyGradesToEvaluationCard(Project project, String studyYear);
}
