package pl.edu.amu.wmi.mapper.projectdefense;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseDTO;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseSummaryDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Mapping(target = "time", source = "entity.defenseTimeslot", qualifiedByName = "DefenseTimeSlotToString")
    @Mapping(target = "projectName", source = "entity.project.name")
    @Mapping(target = "committee", source = "entity.supervisorDefenseAssignments", qualifiedByName = "SupervisorDefenseAssignmentsToSupervisorNames")
    @Mapping(target = "chairperson", source = "entity.chairpersonDefenseAssignment", qualifiedByName = "ChairpersonToChairpersonName")
    @Mapping(target = "classroom", source = "entity.classroom")
    @Mapping(target = "students", source = "entity.project", qualifiedByName = "StudentsToStudentNames")
    @Mapping(target = "supervisor", source = "entity.project", qualifiedByName = "SupervisorToSupervisorName")
    ProjectDefenseSummaryDTO mapToSummaryDto(ProjectDefense entity);

    List<ProjectDefenseSummaryDTO> mapToSummaryDTOs(List<ProjectDefense> entities);

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

    @Named("StudentsToStudentNames")
    default List<String> studentsToStudentNames(Project project) {
        if (Objects.nonNull(project)) {
            return project.getStudents().stream()
                    .map(Student::getFullName)
                    .toList();
        } else {
            return new ArrayList<>();
        }
    }

    @Named("SupervisorToSupervisorName")
    default String supervisorToSupervisorName(Project project) {
        return Objects.nonNull(project) ? project.getSupervisor().getFullName() : null;
    }
}
