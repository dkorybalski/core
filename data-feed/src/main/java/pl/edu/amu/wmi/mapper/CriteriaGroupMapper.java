package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.model.CriteriaGroupDTO;

@Mapper(componentModel = "spring")
public interface CriteriaGroupMapper {

    @Mapping(target = "criteriaGroupGradeWeight", source = "dto.criteriaGroupGradeWeightFirstSemester")
    @Mapping(target = "criteria", ignore = true)
    @Mapping(target = "semester", expression = "java(pl.edu.amu.wmi.enumerations.Semester.SEMESTER_I)")
    @Mapping(target = "id", expression = "java(isNew ? null : dto.id())")
    CriteriaGroup mapToEntityForFirstSemester(CriteriaGroupDTO dto, boolean isNew);

    @Mapping(target = "criteriaGroupGradeWeight", source = "dto.criteriaGroupGradeWeightSecondSemester")
    @Mapping(target = "criteria", ignore = true)
    @Mapping(target = "semester", expression = "java(pl.edu.amu.wmi.enumerations.Semester.SEMESTER_II)")
    @Mapping(target = "id", expression = "java(isNew ? null : dto.id())")
    CriteriaGroup mapToEntityForSecondSemester(CriteriaGroupDTO dto, boolean isNew);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    void update(@MappingTarget CriteriaGroup persistedEntity, CriteriaGroup updateEntity);
}
