package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.entity.AbstractEntity;
import pl.edu.amu.wmi.model.user.StudentCreationRequestDTO;
import pl.edu.amu.wmi.model.user.StudentDTO;

import java.util.List;

public interface StudentService {

    List<StudentDTO> findAll(String studyYear);

    AbstractEntity findById(Long id);

    StudentDTO create(StudentCreationRequestDTO student, String studyYear);
}
