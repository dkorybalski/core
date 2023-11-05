package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.edu.amu.wmi.entity.CriteriaSection;
import pl.edu.amu.wmi.model.CriteriaSectionDTO;

@Mapper(componentModel = "spring")
public interface CriteriaSectionMapper {

    @Mapping(target = "criteriaSectionGradeWeight", source = "dto.criteriaSectionGradeWeightFirstSemester")
    @Mapping(target = "criteriaGroups", ignore = true)
    @Mapping(target = "semester", expression = "java(pl.edu.amu.wmi.enumerations.Semester.SEMESTER_I)")
    @Mapping(target = "id", expression = "java(isNew ? null : dto.id())")
    CriteriaSection mapToEntityForFirstSemester(CriteriaSectionDTO dto, boolean isNew);

    @Mapping(target = "criteriaSectionGradeWeight", source = "dto.criteriaSectionGradeWeightSecondSemester")
    @Mapping(target = "criteriaGroups", ignore = true)
    @Mapping(target = "semester", expression = "java(pl.edu.amu.wmi.enumerations.Semester.SEMESTER_II)")
    @Mapping(target = "id", expression = "java(isNew ? null : dto.id())")
    CriteriaSection mapToEntityForSecondSemester(CriteriaSectionDTO dto, boolean isNew);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    void update(@MappingTarget CriteriaSection persistedEntity, CriteriaSection updateEntity);
}
