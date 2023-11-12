package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.model.ProjectCriteriaGroupDTO;

import java.util.List;

@Mapper(componentModel = "spring", uses = { ProjectCriterionMapper.class, PointsMapper.class })
public interface ProjectCriteriaGroupMapper {

    @Mapping(target = "gradeWeight", source = "gradeWeight", qualifiedByName = "PointsToPercent")
    @Mapping(target = "selectedCriterion", ignore = true)
    ProjectCriteriaGroupDTO mapToDto(CriteriaGroup entity);

    List<ProjectCriteriaGroupDTO> mapToDtoList(List<CriteriaGroup> entities);

}


