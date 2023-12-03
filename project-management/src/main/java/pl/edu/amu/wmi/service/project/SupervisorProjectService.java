package pl.edu.amu.wmi.service.project;

import pl.edu.amu.wmi.model.project.SupervisorAvailabilityDTO;

import java.util.List;

public interface SupervisorProjectService {

    List<SupervisorAvailabilityDTO> getSupervisorsAvailability(String studyYear);

    boolean isSupervisorAvailable(String studyYear, String supervisorIndexNumber);

    List<SupervisorAvailabilityDTO> updateSupervisorsAvailability(String studyYear, List<SupervisorAvailabilityDTO> supervisorAvailabilityList);

}
