package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.entity.Criterion;
import pl.edu.amu.wmi.model.CriteriaGroupDTO;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CriterionMapper.class})
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

    @Mapping(target = "name", source = "key")
    @Mapping(target = "idFirstSemester", source = "idFirstSemester")
    @Mapping(target = "idSecondSemester", source = "idSecondSemester")
    @Mapping(target = "gradeWeightFirstSemester",source = "weightFirstSemester")
    @Mapping(target = "gradeWeightSecondSemester", source = "weightSecondSemester")
    @Mapping(target = "criteria", source = "criteria")
    CriteriaGroupDTO mapToDto(String key,
                              Long idFirstSemester,
                              Long idSecondSemester,
                              Double weightFirstSemester,
                              Double weightSecondSemester,
                              List<Criterion> criteria);
}
