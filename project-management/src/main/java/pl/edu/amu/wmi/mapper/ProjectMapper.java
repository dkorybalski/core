package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.model.ProjectDetailsDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "supervisor", ignore = true)
    Project mapToEntity(ProjectDetailsDTO dto);

    ProjectDetailsDTO mapToDto(Project project);

    List<ProjectDetailsDTO> mapToDtoList(List<Project> entityList);
}
