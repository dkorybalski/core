package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.CriteriaSection;
import pl.edu.amu.wmi.model.ProjectCriteriaSectionDTO;

import java.util.List;

@Mapper(componentModel = "spring", uses = { ProjectCriteriaGroupMapper.class, PointsMapper.class     })
public interface ProjectCriteriaSectionMapper {

    @Mapping(target = "gradeWeight", source = "criteriaSectionGradeWeight", qualifiedByName = "PointsToPercent")
    ProjectCriteriaSectionDTO mapToDto(CriteriaSection entity);

    List<ProjectCriteriaSectionDTO> mapToDtoList(List<CriteriaSection> entities);

}
