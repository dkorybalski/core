package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.RoleDAO;
import pl.edu.amu.wmi.dao.StudyYearDAO;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.entity.StudyYear;
import pl.edu.amu.wmi.entity.Supervisor;

import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.mapper.SupervisorUserMapper;
import pl.edu.amu.wmi.model.user.SupervisorCreationRequestDTO;
import pl.edu.amu.wmi.model.user.SupervisorDTO;
import pl.edu.amu.wmi.service.SupervisorService;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class SupervisorServiceImpl implements SupervisorService {

    private final SupervisorUserMapper supervisorUserMapper;

    private final SupervisorDAO supervisorDAO;

    private final StudyYearDAO studyYearDAO;

    private final RoleDAO roleDAO;

    @Autowired
    public SupervisorServiceImpl(SupervisorUserMapper supervisorUserMapper, SupervisorDAO supervisorDAO, StudyYearDAO studyYearDAO, RoleDAO roleDAO) {
        this.supervisorUserMapper = supervisorUserMapper;
        this.supervisorDAO = supervisorDAO;
        this.studyYearDAO = studyYearDAO;
        this.roleDAO = roleDAO;
    }

    @Transactional
    @Override
    public SupervisorDTO create(SupervisorCreationRequestDTO dto, String studyYear) {
        validateData(dto.getIndexNumber(), studyYear);
        Supervisor entity = supervisorUserMapper.createEntity(dto);

        StudyYear studyYearEntity = studyYearDAO.findByStudyYear(studyYear);
        entity.setStudyYear(studyYearEntity.getStudyYear());
        entity.getUserData().setRoles(Set.of(roleDAO.findByName(UserRole.SUPERVISOR)));

        return supervisorUserMapper.mapToDto(supervisorDAO.save(entity));
    }

    private void validateData(String indexNumber, String studyYear) {
        List<Supervisor> existingSupervisorForStudyYear = supervisorDAO.findAllByStudyYearAndUserData_IndexNumberIn(studyYear, List.of(indexNumber));
        if (!existingSupervisorForStudyYear.isEmpty()) {
            log.error("Duplicated data - supervisor assigned to studyYear {} already exist in the database.", studyYear);
            throw new DuplicateKeyException("Duplicated supervisor data");
        }
    }

    @Override
    public List<SupervisorDTO> findAll() {
        return supervisorUserMapper.mapToDtoList(supervisorDAO.findAll());
    }

    @Override
    public Supervisor findById(Long id) {
        return supervisorDAO.findById(id).orElseThrow();
    }
}
