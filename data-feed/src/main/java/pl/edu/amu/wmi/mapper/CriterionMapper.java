package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Criterion;
import pl.edu.amu.wmi.model.CriterionDTO;

@Mapper(componentModel = "spring")
public interface CriterionMapper {

    @Mapping(target = "gradeWeight", source = "dto.gradeWeightFirstSemester")
    @Mapping(target = "scoringCriteria", ignore = true)
    @Mapping(target = "id", expression = "java(isNew ? null : dto.id())")
    Criterion mapToEntityForFirstSemester(CriterionDTO dto, boolean isNew);

    @Mapping(target = "gradeWeight", source = "dto.gradeWeightSecondSemester")
    @Mapping(target = "scoringCriteria", ignore = true)
    @Mapping(target = "id", expression = "java(isNew ? null : dto.id())")
    Criterion mapToEntityForSecondSemester(CriterionDTO dto, boolean isNew);

}
