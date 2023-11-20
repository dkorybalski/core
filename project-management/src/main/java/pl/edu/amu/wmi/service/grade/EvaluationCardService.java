package pl.edu.amu.wmi.service.grade;

import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.model.grade.GradeDetailsDTO;


public interface EvaluationCardService {

    void addEmptyGradesToEvaluationCard(Project project, String studyYear);

    GradeDetailsDTO updateEvaluationCard(Semester semester, Long projectId, GradeDetailsDTO gradeDetails);

}
