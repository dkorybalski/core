package pl.edu.amu.wmi.mapper.project;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.edu.amu.wmi.entity.EvaluationCard;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.mapper.externallink.ExternalLinkMapper;
import pl.edu.amu.wmi.mapper.grade.PointsMapper;
import pl.edu.amu.wmi.model.project.ProjectDTO;
import pl.edu.amu.wmi.model.project.ProjectDetailsDTO;

import java.util.List;
import java.util.Objects;

import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.ACCEPTED;
import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.CONFIRMED;

@Mapper(componentModel = "spring", uses = { SupervisorProjectMapper.class, ExternalLinkMapper.class, PointsMapper.class })
public interface ProjectMapper {

    @Mapping(target = "supervisor", ignore = true)
    Project mapToEntity(ProjectDetailsDTO dto);

    @Mapping(target = "accepted", source = "acceptanceStatus", qualifiedByName = "AcceptedToBoolean")
    @Mapping(target = "confirmed", source = "acceptanceStatus", qualifiedByName = "ConfirmedToBoolean")
    ProjectDetailsDTO mapToProjectDetailsDto(Project project);

    @Mapping(target = "accepted", source = "acceptanceStatus", qualifiedByName = "AcceptedToBoolean")
    @Mapping(target = "confirmed", source = "acceptanceStatus", qualifiedByName = "ConfirmedToBoolean")
    @Mapping(target = "externalLinks", ignore = true)
    ProjectDetailsDTO mapToProjectDetailsWithRestrictionsDto(Project project);


    @Mapping(target = "supervisor", source = "entity.supervisor")
    @Mapping(target = "accepted", source = "acceptanceStatus", qualifiedByName = "AcceptedToBoolean")
    @Mapping(target = "pointsFirstSemester", source = "entity", qualifiedByName = "GetPointsForFirstSemester")
    @Mapping(target = "pointsSecondSemester", source = "entity", qualifiedByName = "GetPointsForSecondSemester")
    @Mapping(target = "criteriaMet", source = "entity", qualifiedByName = "GetCriteriaMet")
    @Named("mapWithoutRestrictions")
        // TODO: 11/22/2023 add manual mapping for disqualified
    ProjectDTO mapToProjectDto(Project entity);

    @Named("GetPointsForFirstSemester")
    default String mapPointsFirstSemester(Project entity) {
        return getPointsForSemester(entity, Semester.FIRST);
    }

    @Named("GetPointsForSecondSemester")
    default String mapPointsSecondSemester(Project entity) {
        return getPointsForSemester(entity, Semester.SECOND);
    }

    @Named("GetCriteriaMet")
    default boolean getCriteriaMet(Project entity) {
        // TODO: 11/23/2023 remove hardcoded values | add correct logic
        return entity.getEvaluationCards().stream()
                .filter(evaluationCard -> Objects.equals(Semester.FIRST, evaluationCard.getSemester()))
                .filter(evaluationCard -> Objects.equals(EvaluationPhase.SEMESTER_PHASE, evaluationCard.getEvaluationPhase()))
                .filter(evaluationCard -> Objects.equals(EvaluationStatus.ACTIVE, evaluationCard.getEvaluationStatus()))
                .map(evaluationCard -> !evaluationCard.isDisqualified())
                .findFirst()
                .orElse(false);
    }

    private String getPointsForSemester(Project entity, Semester semester) {
        // TODO: 11/23/2023 implement logic to take the most recent active or published result
        Double points = entity.getEvaluationCards().stream()
                .filter(evaluationCard -> Objects.equals(semester, evaluationCard.getSemester()))
                .filter(evaluationCard -> Objects.equals(EvaluationPhase.SEMESTER_PHASE, evaluationCard.getEvaluationPhase()))
                .filter(evaluationCard -> Objects.equals(EvaluationStatus.ACTIVE, evaluationCard.getEvaluationStatus()))
                .map(EvaluationCard::getTotalPoints)
                .findFirst()
                .orElse(0.0);
        return String.format("%.2f", (points * 100 / 4)) + "%";

    }

    @Named("AcceptedToBoolean")
    default boolean mapAccepted(AcceptanceStatus status) {
        return status.equals(ACCEPTED);
    }

    @Named("ConfirmedToBoolean")
    default boolean mapConfirmed(AcceptanceStatus status) {
        return status.equals(CONFIRMED) || status.equals(ACCEPTED);
    }

    @Named("DisqualifiedToCriteriaMet")
    default boolean mapCriteriaMet(boolean isDisqualified) {
        return !isDisqualified;
    }

    @Mapping(target = "supervisor", source = "entity.supervisor")
    @Mapping(target = "accepted", source = "acceptanceStatus", qualifiedByName = "AcceptedToBoolean")
    @Mapping(target = "pointsFirstSemester", ignore = true)
    @Mapping(target = "pointsSecondSemester", ignore = true)
    @Mapping(target = "criteriaMet", ignore = true)
    @Mapping(target = "externalLinks", ignore = true)
    ProjectDTO mapToProjectDtoWithRestrictions(Project entity);

    @IterableMapping(qualifiedByName = "mapWithoutRestrictions")
    List<ProjectDTO> mapToDTOs(List<Project> projectEntityList);
}
