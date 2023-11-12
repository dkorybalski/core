package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Criterion;
import pl.edu.amu.wmi.model.ProjectCriterionDTO;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface ProjectCriterionMapper {

    @Mapping(target = "category", source = "criterionCategory")
    ProjectCriterionDTO mapToDto(Criterion entity);

    Set<ProjectCriterionDTO> mapToDtoSet(Set<Criterion> entities);

}
