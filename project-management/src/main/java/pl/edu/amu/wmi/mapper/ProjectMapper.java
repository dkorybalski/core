package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.model.ProjectCreationRequestDTO;
import pl.edu.amu.wmi.model.ProjectCreationResponseDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "supervisor", ignore = true)
    @Mapping(target = "studyYear", ignore = true)
    Project mapToEntity(ProjectCreationRequestDTO dto);

    @Mapping(target = "studyYear", ignore = true)
    ProjectCreationRequestDTO mapToDto(Project project);

    ProjectCreationResponseDTO mapToResponseDto(Project project);

    List<ProjectCreationRequestDTO> mapToDtoList(List<Project> entityList);
}
