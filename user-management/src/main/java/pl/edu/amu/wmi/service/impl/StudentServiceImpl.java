package pl.edu.amu.wmi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.StudentDAO;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.mapper.StudentMapper;
import pl.edu.amu.wmi.model.StudentDTO;
import pl.edu.amu.wmi.service.StudentService;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;

    private final StudentDAO studentDAO;

    @Autowired
    public StudentServiceImpl(StudentMapper studentMapper, StudentDAO studentDAO) {
        this.studentMapper = studentMapper;
        this.studentDAO = studentDAO;
    }

    @Override
    public List<StudentDTO> findAll() {
        return studentMapper.mapToDtoList(studentDAO.findAll());
    }

    @Override
    public Student findById(Long id) {
        return studentDAO.findById(id).get();
    }
}
