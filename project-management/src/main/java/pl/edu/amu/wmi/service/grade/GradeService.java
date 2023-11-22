package pl.edu.amu.wmi.service.grade;

import pl.edu.amu.wmi.entity.Grade;
import pl.edu.amu.wmi.enumerations.CriterionCategory;


public interface GradeService {

    void updateSingleGrade(Grade grade, CriterionCategory newSelectedCriterion);

}
