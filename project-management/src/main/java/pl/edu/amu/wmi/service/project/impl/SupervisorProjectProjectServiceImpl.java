package pl.edu.amu.wmi.service.project.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.*;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.mapper.project.SupervisorProjectMapper;
import pl.edu.amu.wmi.model.project.SupervisorAvailabilityDTO;
import pl.edu.amu.wmi.service.project.SupervisorProjectService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        List<Supervisor> supervisorEntities = supervisorDAO.findAllByStudyYear(studyYear);
        List<SupervisorAvailabilityDTO> supervisorAvailabilityDTOS = new ArrayList<>();
        supervisorEntities
                .forEach(supervisor -> {
                    SupervisorAvailabilityDTO dto = supervisorProjectMapper.mapToAvailabilityDto(supervisor);
                    dto.setAssigned((int) supervisor.getProjects().stream()
                            .filter(p -> Objects.equals(AcceptanceStatus.ACCEPTED, p.getAcceptanceStatus()))
                            .count());
                    supervisorAvailabilityDTOS.add(dto);
                });
        return supervisorAvailabilityDTOS;
    }

    @Override
    @Transactional
    public List<SupervisorAvailabilityDTO> updateSupervisorsAvailability(String studyYear, List<SupervisorAvailabilityDTO> supervisorAvailabilityList) {
        List<Supervisor> supervisorEntities = new ArrayList<>();
        supervisorAvailabilityList.forEach(supervisorAvailabilityDTO -> {
            Supervisor entity = supervisorDAO.findByStudyYearAndUserData_IndexNumber(studyYear, supervisorAvailabilityDTO.getSupervisor().getIndexNumber());
            entity.setMaxNumberOfProjects(supervisorAvailabilityDTO.getMax());
            supervisorEntities.add(entity);
        });

        supervisorDAO.saveAll(supervisorEntities);

        return supervisorProjectMapper.mapToAvailabilityDtoList(supervisorEntities);
    }

}
