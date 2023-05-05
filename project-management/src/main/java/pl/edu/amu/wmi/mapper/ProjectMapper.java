package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.model.ProjectDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    Project map(ProjectDTO dto);

    ProjectDTO map(Project entity);

    List<ProjectDTO> map(List<Project> entityList);
}
