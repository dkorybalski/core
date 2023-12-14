package pl.edu.amu.wmi.mapper.committee;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import pl.edu.amu.wmi.entity.DefenseTimeSlot;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.model.committee.SupervisorDefenseAssignmentDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupervisorAvailabilityMapper {

    @Mapping(target = "supervisorId", source = "supervisor.id")
    @Mapping(target = "defenseSlotId", source = "defenseTimeSlot.id")
    @Mapping(target = "time", source = "defenseTimeSlot", qualifiedByName = "DefenseTimeSlotStartTimeToString")
    @Mapping(target = "projectId", source = "projectDefense.project.id")
    SupervisorDefenseAssignmentDTO mapToDto(SupervisorDefenseAssignment entity);

    List<SupervisorDefenseAssignmentDTO> mapToDtoList(List<SupervisorDefenseAssignment> entities);

    @Named("DefenseTimeSlotStartTimeToString")
    default String defenseTimeSlotStartDateToString (DefenseTimeSlot defenseTimeSlot) {
        return defenseTimeSlot.getStartTime().toString();
    }

    void update(@MappingTarget SupervisorDefenseAssignment entity, SupervisorDefenseAssignmentDTO dto);

}