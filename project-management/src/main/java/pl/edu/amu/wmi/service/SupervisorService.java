package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.model.SupervisorAvailabilityDTO;

import java.util.List;

public interface SupervisorService {

    List<SupervisorAvailabilityDTO> getSupervisorsAvailability(String studyYear);

}
