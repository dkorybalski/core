package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.*;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.mapper.SupervisorProjectMapper;
import pl.edu.amu.wmi.model.SupervisorAvailabilityDTO;
import pl.edu.amu.wmi.service.SupervisorService;

import java.util.List;

@Slf4j
@Service
public class SupervisorServiceImpl implements SupervisorService {

    private final SupervisorDAO supervisorDAO;

    private final SupervisorProjectMapper supervisorProjectMapper;

    @Autowired
    public SupervisorServiceImpl(SupervisorDAO supervisorDAO, SupervisorProjectMapper supervisorProjectMapper) {
        this.supervisorDAO = supervisorDAO;
        this.supervisorProjectMapper = supervisorProjectMapper;
    }

    @Override
    public List<SupervisorAvailabilityDTO> getSupervisorsAvailability(String studyYear) {
        List<Supervisor> supervisorEntities = supervisorDAO.findAllByUserData_StudyYear_StudyYear(studyYear);
        return supervisorProjectMapper.mapToAvailabilityDtoList(supervisorEntities);
    }

}
