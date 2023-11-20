package pl.edu.amu.wmi.service.grade;

import pl.edu.amu.wmi.entity.Grade;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.model.grade.GradeDetailsDTO;

import java.util.List;


public interface GradeService {

    GradeDetailsDTO findByProjectIdAndSemester(Semester semester, Long id);

    void updateProjectGradesForSemester(Semester semester, List<Grade> projectGradesForSemester, GradeDetailsDTO projectGradeDetails);

}
