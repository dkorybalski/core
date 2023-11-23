package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.edu.amu.wmi.entity.CriteriaSection;
import pl.edu.amu.wmi.model.CriteriaSectionDTO;

@Mapper(componentModel = "spring")
public interface CriteriaSectionMapper {

    @Mapping(target = "criteriaSectionGradeWeight", source = "dto.criteriaSectionGradeWeightFirstSemester")
    @Mapping(target = "defenseSection", source = "dto.isDefenseSection")
    @Mapping(target = "criteriaGroups", ignore = true)
    @Mapping(target = "semester", expression = "java(pl.edu.amu.wmi.enumerations.Semester.FIRST)")
    @Mapping(target = "id", expression = "java(isSaveMode ? null : dto.idFirstSemester())")
    CriteriaSection mapToEntityForFirstSemester(CriteriaSectionDTO dto, boolean isSaveMode);

    @Mapping(target = "criteriaSectionGradeWeight", source = "dto.criteriaSectionGradeWeightSecondSemester")
    @Mapping(target = "defenseSection", source = "dto.isDefenseSection")
    @Mapping(target = "criteriaGroups", ignore = true)
    @Mapping(target = "semester", expression = "java(pl.edu.amu.wmi.enumerations.Semester.SECOND)")
    @Mapping(target = "id", expression = "java(isSaveMode ? null : dto.idSecondSemester())")
    CriteriaSection mapToEntityForSecondSemester(CriteriaSectionDTO dto, boolean isSaveMode);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "criteriaGroups", ignore = true)
    void update(@MappingTarget CriteriaSection persistedEntity, CriteriaSection updateEntity);
}
