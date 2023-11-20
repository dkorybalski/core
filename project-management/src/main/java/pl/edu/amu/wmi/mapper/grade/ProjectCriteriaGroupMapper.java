package pl.edu.amu.wmi.mapper.grade;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.model.grade.CriteriaGroupDTO;
import pl.edu.amu.wmi.model.grade.CriterionDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", uses = { PointsMapper.class })
public interface ProjectCriteriaGroupMapper {

    @Mapping(target = "gradeWeight", source = "gradeWeight", qualifiedByName = "PointsToPercent")
    @Mapping(target = "selectedCriterion", ignore = true)
    @Mapping(target = "criteria", ignore = true)
    CriteriaGroupDTO mapToDto(CriteriaGroup entity);

    List<CriteriaGroupDTO> mapToDtoList(List<CriteriaGroup> entities);

    @AfterMapping
    default void mapCriteria(CriteriaGroup entity, @MappingTarget CriteriaGroupDTO dto) {
        Map<String, CriterionDTO> criteria = new HashMap<>();
        entity.getCriteria().forEach(criterion -> criteria.put(criterion.getCriterionCategory().name(),
                new CriterionDTO(criterion.getDescription(), criterion.isDisqualifying())));
        dto.setCriteria(criteria);
    }

}


