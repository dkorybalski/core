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
    @Mapping(target = "id", expression = "java(isSaveMode ? null : dto.id())")
    Criterion mapToEntity(CriterionDTO dto, boolean isSaveMode);

    default List<Criterion> mapToEntitiesList(List<CriterionDTO> criterionDTOS, boolean isSaveMode) {
        return criterionDTOS.stream()
                .map(criterion -> mapToEntity(criterion, isSaveMode))
                .toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    void update(@MappingTarget Criterion persistedEntity, Criterion criterion);

    @Mapping(target = "category", source = "criterionCategory")
    @Mapping(target = "isDisqualifying", source = "entity.disqualifying")
    CriterionDTO mapToDto(Criterion entity);
}
