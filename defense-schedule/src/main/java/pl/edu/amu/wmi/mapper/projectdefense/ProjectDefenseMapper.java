package pl.edu.amu.wmi.mapper.projectdefense;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.edu.amu.wmi.entity.DefenseTimeSlot;
import pl.edu.amu.wmi.entity.ProjectDefense;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectDefenseMapper {

    @Mapping(target = "projectDefenseId", source = "entity.id")
    @Mapping(target = "projectId", source = "entity.project.id")
    @Mapping(target = "time", source = "entity.defenseTimeslot", qualifiedByName = "DefenseTimeSlotToString")
    @Mapping(target = "projectName", source = "entity.project.name")
    @Mapping(target = "committee", source = "entity.supervisorDefenseAssignments", qualifiedByName = "SupervisorDefenseAssignmentsToSupervisorNames")
    @Mapping(target = "chairperson", source = "entity.chairpersonDefenseAssignment", qualifiedByName = "ChairpersonToChairpersonName")
    @Mapping(target = "classroom", source = "entity.classroom")
    @Mapping(target = "editable", ignore = true)
    ProjectDefenseDTO mapToDto(ProjectDefense entity);

    List<ProjectDefenseDTO> mapToDTOs(List<ProjectDefense> entities);

    @Named("DefenseTimeSlotToString")
    default String defenseTimeSlotToString (DefenseTimeSlot defenseTimeSlot) {
        return defenseTimeSlot.getStartTime().toString() + " - " + defenseTimeSlot.getEndTime().toString();
    }

    @Named("SupervisorDefenseAssignmentsToSupervisorNames")
    default List<String> supervisorDefenseAssignmentsToSupervisorNames(List<SupervisorDefenseAssignment> supervisorDefenseAssignments) {
        return supervisorDefenseAssignments.stream()
                .map(supervisorDefenseAssignment -> supervisorDefenseAssignment.getSupervisor().getFullName())
                .toList();
    }

    @Named("ChairpersonToChairpersonName")
    default String chairpersonToChairpersonName(SupervisorDefenseAssignment chairperson) {
        return chairperson.getSupervisor().getFullName();
    }
}
