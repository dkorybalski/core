package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.EvaluationCardTemplate;
import pl.edu.amu.wmi.model.EvaluationCriteriaDTO;

@Mapper(componentModel = "spring")
public interface EvaluationCriteriaMapper {

    @Mapping(target = "criteriaSections", expression = "java(new java.util.ArrayList())")
    EvaluationCriteriaDTO mapToDto(EvaluationCardTemplate entity);

}
