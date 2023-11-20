package pl.edu.amu.wmi.mapper.grade;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.CriteriaSection;
import pl.edu.amu.wmi.model.grade.CriteriaSectionDTO;

import java.util.List;

@Mapper(componentModel = "spring", uses = { ProjectCriteriaGroupMapper.class, PointsMapper.class     })
public interface ProjectCriteriaSectionMapper {

    @Mapping(target = "gradeWeight", source = "criteriaSectionGradeWeight", qualifiedByName = "PointsToPercent")
    CriteriaSectionDTO mapToDto(CriteriaSection entity);

    List<CriteriaSectionDTO> mapToDtoList(List<CriteriaSection> entities);

}
