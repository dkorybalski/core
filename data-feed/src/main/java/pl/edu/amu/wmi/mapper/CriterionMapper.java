package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.edu.amu.wmi.entity.Criterion;
import pl.edu.amu.wmi.model.CriterionDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CriterionMapper {

    @Mapping(target = "criterionCategory", source = "dto.category")
    @Mapping(target = "disqualifying", source = "dto.isDisqualifying")
    @Mapping(target = "id", expression = "java(isNew ? null : dto.id())")
    Criterion mapToEntity(CriterionDTO dto, boolean isNew);

    default List<Criterion> mapToEntitiesList(List<CriterionDTO> criterionDTOS, boolean isNew) {
        return criterionDTOS.stream()
                .map(criterion -> mapToEntity(criterion, isNew))
                .toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    void update(@MappingTarget Criterion persistedEntity, Criterion criterion);
}
