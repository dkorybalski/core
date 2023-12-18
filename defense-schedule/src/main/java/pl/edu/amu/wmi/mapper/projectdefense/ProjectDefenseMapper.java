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

import static pl.edu.amu.wmi.util.CommonDateUtils.commonDateFormatter;

@Mapper(componentModel = "spring")
public interface ProjectDefenseMapper {

    @Mapping(target = "projectDefenseId", source = "entity.id")
    @Mapping(target = "projectId", source = "entity.project", qualifiedByName = "ProjectToProjectId")
    @Mapping(target = "date", source = "entity.defenseTimeslot", qualifiedByName = "DefenseTimeSlotDateToString")
    @Mapping(target = "time", source = "entity.defenseTimeslot", qualifiedByName = "DefenseTimeSlotStartTimeToString")
    @Mapping(target = "projectName", source = "entity.project.name")
    @Mapping(target = "committee", source = "entity.supervisorDefenseAssignments", qualifiedByName = "SupervisorDefenseAssignmentsToSupervisorsInitials")
    @Mapping(target = "students", source = "entity.project", qualifiedByName = "StudentsToStudentsNames")
    @Mapping(target = "chairperson", source = "entity.chairpersonDefenseAssignment", qualifiedByName = "ChairpersonToChairpersonInitials")
    @Mapping(target = "classroom", source = "entity.classroom")
    @Mapping(target = "editable", ignore = true)
    ProjectDefenseDTO mapToDto(ProjectDefense entity);

    List<ProjectDefenseDTO> mapToDTOs(List<ProjectDefense> entities);

    @Mapping(target = "time", source = "entity.defenseTimeslot", qualifiedByName = "DefenseTimeSlotStartTimeToString")
    @Mapping(target = "projectName", source = "entity.project.name")
    @Mapping(target = "committee", source = "entity.supervisorDefenseAssignments", qualifiedByName = "SupervisorDefenseAssignmentsToSupervisorsInitials")
    @Mapping(target = "chairperson", source = "entity.chairpersonDefenseAssignment", qualifiedByName = "ChairpersonToChairpersonInitials")
    @Mapping(target = "classroom", source = "entity.classroom")
    @Mapping(target = "students", source = "entity.project", qualifiedByName = "StudentsToStudentNames")
    @Mapping(target = "supervisor", source = "entity.project", qualifiedByName = "SupervisorToSupervisorInitials")
    ProjectDefenseSummaryDTO mapToSummaryDto(ProjectDefense entity);

    List<ProjectDefenseSummaryDTO> mapToSummaryDTOs(List<ProjectDefense> entities);

    @Named("DefenseTimeSlotStartTimeToString")
    default String defenseTimeSlotStartDateToString (DefenseTimeSlot defenseTimeSlot) {
        return defenseTimeSlot.getStartTime().toString();
    }

    @Named("DefenseTimeSlotDateToString")
    default String defenseTimeSlotDateToString (DefenseTimeSlot defenseTimeSlot) {
        return defenseTimeSlot.getDate().format(commonDateFormatter());
    }

    @Named("SupervisorDefenseAssignmentsToSupervisorsInitials")
    default List<String> supervisorDefenseAssignmentsToSupervisorsInitials(List<SupervisorDefenseAssignment> supervisorDefenseAssignments) {
        return supervisorDefenseAssignments.stream()
                .map(supervisorDefenseAssignment -> supervisorDefenseAssignment.getSupervisor().getInitials())
                .toList();
    }

    @Named("StudentsToStudentsNames")
    default String studentsToStudentsNames(Project project) {
        if (Objects.nonNull(project)) {
            return project.getStudentsBasicData();
        } else {
            return null;
        }
    }

    @Named("ProjectToProjectId")
    default String projectToProjectId(Project project) {
        if (Objects.nonNull(project)) {
            return project.getId().toString();
        } else {
            return null;
        }
    }

    @Named("ChairpersonToChairpersonInitials")
    default String chairpersonToChairpersonInitials(SupervisorDefenseAssignment chairperson) {
        return chairperson.getSupervisor().getInitials();
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

    @Named("SupervisorToSupervisorInitials")
    default String supervisorToSupervisorInitials(Project project) {
        return Objects.nonNull(project) ? project.getSupervisor().getInitials() : null;
    }
}
