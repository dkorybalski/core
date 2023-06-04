package pl.edu.amu.wmi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.StudentDAO;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.mapper.StudentUserMapper;
import pl.edu.amu.wmi.model.user.StudentDTO;
import pl.edu.amu.wmi.service.StudentService;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentUserMapper studentUserMapper;

    private final StudentDAO studentDAO;

    @Autowired
    public StudentServiceImpl(StudentUserMapper studentUserMapper, StudentDAO studentDAO) {
        this.studentUserMapper = studentUserMapper;
        this.studentDAO = studentDAO;
    }

    @Override
    public List<StudentDTO> findAll() {
        // TODO: 6/4/2023 add search by study year
        return studentUserMapper.mapToDtoList(studentDAO.findAll());
    }

    @Override
    public Student findById(Long id) {
        return studentDAO.findById(id).get();
    }
}
