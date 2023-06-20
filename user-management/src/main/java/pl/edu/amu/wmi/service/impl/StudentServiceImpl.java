package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.RoleDAO;
import pl.edu.amu.wmi.dao.StudentDAO;
import pl.edu.amu.wmi.dao.StudyYearDAO;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.entity.StudyYear;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.mapper.StudentUserMapper;
import pl.edu.amu.wmi.model.user.StudentCreationRequestDTO;
import pl.edu.amu.wmi.model.user.StudentDTO;
import pl.edu.amu.wmi.service.StudentService;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentUserMapper studentUserMapper;

    private final StudentDAO studentDAO;

    private final StudyYearDAO studyYearDAO;

    private final RoleDAO roleDAO;

    @Autowired
    public StudentServiceImpl(StudentUserMapper studentUserMapper, StudentDAO studentDAO, StudyYearDAO studyYearDAO, RoleDAO roleDAO) {
        this.studentUserMapper = studentUserMapper;
        this.studentDAO = studentDAO;
        this.studyYearDAO = studyYearDAO;
        this.roleDAO = roleDAO;
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

    @Transactional
    @Override
    public StudentDTO create(StudentCreationRequestDTO dto, String studyYear) {
        validateData(dto.getIndexNumber(), studyYear);
        Student entity = studentUserMapper.createEntity(dto);

        StudyYear studyYearEntity = studyYearDAO.findByStudyYear(studyYear);
        entity.getUserData().setStudyYear(studyYearEntity);
        entity.getUserData().setRoles(Set.of(roleDAO.findByName(UserRole.STUDENT)));
        return studentUserMapper.mapToDto(studentDAO.save(entity));
    }

    private void validateData(String indexNumber, String studyYear) {
        List<Student> existingStudentForStudyYear = studentDAO.findAllByUserData_StudyYear_StudyYearAndUserData_IndexNumberIn(studyYear, List.of(indexNumber));
        if (!existingStudentForStudyYear.isEmpty()) {
            log.error("Duplicated data - student assigned to studyYear {} already exist in the database.", studyYear);
            throw new DuplicateKeyException("Duplicated student data");
        }
    }
}
