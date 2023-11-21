package pl.edu.amu.wmi.service.grade;

import pl.edu.amu.wmi.entity.Grade;
import pl.edu.amu.wmi.enumerations.CriterionCategory;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.model.grade.GradeDetailsDTO;


public interface GradeService {

    GradeDetailsDTO findByProjectIdAndSemester(Semester semester, Long id);

    void updateSingleGrade(Grade grade, CriterionCategory newSelectedCriterion);

}
