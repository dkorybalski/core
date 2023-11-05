package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.model.CriteriaGroupDTO;

@Mapper(componentModel = "spring")
public interface CriteriaGroupMapper {

    @Mapping(target = "gradeWeight", source = "dto.gradeWeightFirstSemester")
    @Mapping(target = "criteria", ignore = true)
    @Mapping(target = "id", expression = "java(isNew ? null : dto.id())")
    CriteriaGroup mapToEntityForFirstSemester(CriteriaGroupDTO dto, boolean isNew);

    @Mapping(target = "gradeWeight", source = "dto.gradeWeightSecondSemester")
    @Mapping(target = "criteria", ignore = true)
    @Mapping(target = "id", expression = "java(isNew ? null : dto.id())")
    CriteriaGroup mapToEntityForSecondSemester(CriteriaGroupDTO dto, boolean isNew);

}
