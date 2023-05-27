package pl.edu.amu.wmi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.mapper.SupervisorMapper;
import pl.edu.amu.wmi.model.SupervisorDTO;
import pl.edu.amu.wmi.service.SupervisorService;

import java.util.List;

@Service
public class SupervisorServiceImpl implements SupervisorService {

    private final SupervisorMapper supervisorMapper;

    private final SupervisorDAO supervisorDAO;

    @Autowired
    public SupervisorServiceImpl(SupervisorMapper supervisorMapper, SupervisorDAO supervisorDAO) {
        this.supervisorMapper = supervisorMapper;
        this.supervisorDAO = supervisorDAO;
    }

    @Override
    public List<SupervisorDTO> findAll() {
        return supervisorMapper.mapToDtoList(supervisorDAO.findAll());
    }

    @Override
    public Supervisor findById(Long id) {
        return supervisorDAO.findById(id).orElseThrow();
    }
}
