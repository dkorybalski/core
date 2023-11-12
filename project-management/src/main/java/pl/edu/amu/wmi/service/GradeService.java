package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.model.ProjectGradeDetailsDTO;


public interface GradeService {

    ProjectGradeDetailsDTO findByProjectIdAndSemester(Semester semester, Long id);

}
