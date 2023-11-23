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
import java.util.Optional;
import java.util.function.Predicate;

import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.ACCEPTED;
import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.CONFIRMED;

@Mapper(componentModel = "spring", uses = { SupervisorProjectMapper.class, ExternalLinkMapper.class, PointsMapper.class })
public interface ProjectMapper {

    Integer NUMBER_OF_EVALUATION_CARDS_IN_SINGLE_SEMESTER = 3;

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
        Semester semester = determineTheMostRecentSemester(entity.getEvaluationCards());
        Optional<EvaluationCard> theMostRecentEvaluationCard = findTheMostRecentEvaluationCard(entity.getEvaluationCards(), semester);
        return theMostRecentEvaluationCard.map(evaluationCard -> !evaluationCard.isDisqualified()).orElse(false);
    }

    default Semester determineTheMostRecentSemester(List<EvaluationCard> evaluationCards) {
        return evaluationCards.size() <= NUMBER_OF_EVALUATION_CARDS_IN_SINGLE_SEMESTER ? Semester.FIRST : Semester.SECOND;
    }

    private String getPointsForSemester(Project entity, Semester semester) {
        Optional<EvaluationCard> theMostRecentEvaluationCard = findTheMostRecentEvaluationCard(entity.getEvaluationCards(), semester);
        return theMostRecentEvaluationCard.map(evaluationCard -> pointsToOverallPercent(evaluationCard.getTotalPoints())).orElse("0%");
    }

    private Optional<EvaluationCard> findTheMostRecentEvaluationCard(List<EvaluationCard> evaluationCards, Semester semester) {
        Predicate<EvaluationCard> isSearchedSemester = evaluationCard -> Objects.equals(semester, evaluationCard.getSemester());
        Predicate<EvaluationCard> isDefensePhase = evaluationCard -> Objects.equals(EvaluationPhase.DEFENSE_PHASE, evaluationCard.getEvaluationPhase());
        Predicate<EvaluationCard> isRetakePhase = evaluationCard -> Objects.equals(EvaluationPhase.RETAKE_PHASE, evaluationCard.getEvaluationPhase());
        Predicate<EvaluationCard> isActiveStatus = evaluationCard -> Objects.equals(EvaluationStatus.ACTIVE, evaluationCard.getEvaluationStatus());
        Predicate<EvaluationCard> isPublishedStatus = evaluationCard -> Objects.equals(EvaluationStatus.PUBLISHED, evaluationCard.getEvaluationStatus());

        return evaluationCards.stream()
                .filter(isSearchedSemester.and(isActiveStatus))
                .findFirst()
                .or(() -> evaluationCards.stream()
                        .filter(isSearchedSemester.and(isRetakePhase).and(isPublishedStatus))
                        .findFirst()
                        .or(() -> evaluationCards.stream()
                                .filter(isSearchedSemester.and(isDefensePhase).and(isPublishedStatus))
                                .findFirst()));
    }

    private String pointsToOverallPercent(Double points) {
        if (Objects.isNull(points))
            return "0.0%";
        Double pointsOverall = points * 100 / 4;
        return String.format("%.2f", pointsOverall) + "%";
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
