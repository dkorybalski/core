package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.model.CriteriaGroupDTO;

@Mapper(componentModel = "spring")
public interface CriteriaGroupMapper {

    @Mapping(target = "gradeWeight", source = "dto.gradeWeightFirstSemester")
    @Mapping(target = "criteria", ignore = true)
    @Mapping(target = "id", expression = "java(isSaveMode ? null : dto.idFirstSemester())")
    CriteriaGroup mapToEntityForFirstSemester(CriteriaGroupDTO dto, boolean isSaveMode);

    @Mapping(target = "gradeWeight", source = "dto.gradeWeightSecondSemester")
    @Mapping(target = "criteria", ignore = true)
    @Mapping(target = "id", expression = "java(isSaveMode ? null : dto.idSecondSemester())")
    CriteriaGroup mapToEntityForSecondSemester(CriteriaGroupDTO dto, boolean isSaveMode);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "criteria", ignore = true)
    void update(@MappingTarget CriteriaGroup persistedEntity, CriteriaGroup updateEntity);

}
