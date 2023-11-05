package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.edu.amu.wmi.entity.ScoringCriteria;
import pl.edu.amu.wmi.model.ScoringCriteriaDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScoringCriteriaMapper {

    @Mapping(target = "criterionCategory", source = "dto.category")
    @Mapping(target = "disqualifying", source = "dto.isDisqualifying")
    @Mapping(target = "id", expression = "java(isNew ? null : dto.id())")
    ScoringCriteria mapToEntity(ScoringCriteriaDTO dto, boolean isNew);

    default List<ScoringCriteria> mapToEntitiesList(List<ScoringCriteriaDTO> scoringCriteriaDTOS, boolean isNew) {
        return scoringCriteriaDTOS.stream()
                .map(scoringCriterion -> mapToEntity(scoringCriterion, isNew))
                .toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    void update(@MappingTarget ScoringCriteria persistedEntity, ScoringCriteria scoringCriterion);
}
