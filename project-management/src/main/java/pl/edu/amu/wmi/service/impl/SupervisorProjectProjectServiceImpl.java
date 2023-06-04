package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.*;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.mapper.SupervisorProjectMapper;
import pl.edu.amu.wmi.model.SupervisorAvailabilityDTO;
import pl.edu.amu.wmi.service.SupervisorProjectService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SupervisorProjectProjectServiceImpl implements SupervisorProjectService {

    private final SupervisorDAO supervisorDAO;

    private final SupervisorProjectMapper supervisorProjectMapper;

    @Autowired
    public SupervisorProjectProjectServiceImpl(SupervisorDAO supervisorDAO, SupervisorProjectMapper supervisorProjectMapper) {
        this.supervisorDAO = supervisorDAO;
        this.supervisorProjectMapper = supervisorProjectMapper;
    }

    @Override
    public List<SupervisorAvailabilityDTO> getSupervisorsAvailability(String studyYear) {
        List<Supervisor> supervisorEntities = supervisorDAO.findAllByUserData_StudyYear_StudyYear(studyYear);
        return supervisorProjectMapper.mapToAvailabilityDtoList(supervisorEntities);
    }

    @Override
    @Transactional
    public List<SupervisorAvailabilityDTO> updateSupervisorsAvailability(String studyYear, List<SupervisorAvailabilityDTO> supervisorAvailabilityList) {
        List<Supervisor> supervisorEntities = new ArrayList<>();
        supervisorAvailabilityList.forEach(supervisorAvailabilityDTO -> {
            Supervisor entity = supervisorDAO.findByUserData_StudyYear_StudyYearAndUserData_IndexNumber(studyYear, supervisorAvailabilityDTO.getSupervisor().getIndexNumber());
            entity.setMaxNumberOfProjects(supervisorAvailabilityDTO.getMax());
            supervisorEntities.add(entity);
        });

        supervisorDAO.saveAll(supervisorEntities);

        return supervisorProjectMapper.mapToAvailabilityDtoList(supervisorEntities);
    }

}
