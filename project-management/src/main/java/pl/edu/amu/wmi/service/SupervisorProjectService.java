package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.model.SupervisorAvailabilityDTO;

import java.util.List;

public interface SupervisorProjectService {

    List<SupervisorAvailabilityDTO> getSupervisorsAvailability(String studyYear);

    List<SupervisorAvailabilityDTO> updateSupervisorsAvailability(String studyYear, List<SupervisorAvailabilityDTO> supervisorAvailabilityList);

}
