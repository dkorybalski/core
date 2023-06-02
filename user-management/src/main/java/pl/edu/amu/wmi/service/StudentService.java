package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.entity.AbstractEntity;
import pl.edu.amu.wmi.model.user.StudentDTO;

import java.util.List;

public interface StudentService {

    List<StudentDTO> findAll();

    AbstractEntity findById(Long id);
}
