package pl.edu.amu.wmi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.edu.amu.wmi.model.project.ProjectDTO;
import pl.edu.amu.wmi.model.project.ProjectDetailsDTO;
import pl.edu.amu.wmi.model.project.SupervisorAvailabilityDTO;
import pl.edu.amu.wmi.service.project.ProjectService;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.edu.amu.wmi.utils.Constants.STUDY_YEAR_FULL_TIME_2023;
import static pl.edu.amu.wmi.utils.Constants.STUDY_YEAR_HEADER;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration(classes = {TestConfig.class})
class ProjectControllerIT {

    @Autowired
    ProjectService projectService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUserDetails(value = "Coordinator 1", userDetailsServiceBeanName = "testUserDetailsService")
    void getProjectsShouldReturnResultsAndStatus200() throws Exception {
        //given
        int expectedNumberOfProjects = 1;
        //when
        final ResultActions resultActions = mockMvc.perform(get("/project").header(STUDY_YEAR_HEADER, STUDY_YEAR_FULL_TIME_2023));
        //then
        resultActions.andExpect(status().isOk()).andDo(print());
        List<ProjectDTO> projects = extractProjectResultListFromResponse(resultActions);
        assertThat(projects).hasSize(expectedNumberOfProjects);
    }

    @Test
    @WithUserDetails(value = "Coordinator 1", userDetailsServiceBeanName = "testUserDetailsService")
    void getProjectByIdShouldReturnResultAndStatus200() throws Exception {
        //given
        Long projectId = 1L;
        //when
        final ResultActions resultActions = mockMvc.perform(get("/project/{id}", projectId).header(STUDY_YEAR_HEADER, STUDY_YEAR_FULL_TIME_2023));
        //then
        resultActions.andExpect(status().isOk()).andDo(print());
        ProjectDetailsDTO project = extractProjectDetailsDtoFromResponse(resultActions);

        assertThat(project).isNotNull();
        assertThat(project.getId()).isEqualTo(projectId.toString());
    }

    @Test
    @WithUserDetails(value = "Coordinator 1", userDetailsServiceBeanName = "testUserDetailsService")
    void getSupervisorAvailabilityShouldReturnResultsAndStatus200() throws Exception {
        //given
        int expectedNumberOfResults = 3;
        //when
        final ResultActions resultActions = mockMvc.perform(get("/project/supervisor/availability").header(STUDY_YEAR_HEADER, STUDY_YEAR_FULL_TIME_2023));
        //then
        resultActions.andExpect(status().isOk()).andDo(print());
        List<SupervisorAvailabilityDTO> supervisorAvailabilities = extractSupervisorAvailabilityListFromResponse(resultActions);
        assertThat(supervisorAvailabilities).hasSize(expectedNumberOfResults);
    }

    private List<SupervisorAvailabilityDTO> extractSupervisorAvailabilityListFromResponse(ResultActions resultActions) throws UnsupportedEncodingException, JsonProcessingException {
        String json = resultActions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<List<SupervisorAvailabilityDTO>>() {});
    }

    private ProjectDetailsDTO extractProjectDetailsDtoFromResponse(ResultActions resultActions) throws UnsupportedEncodingException, JsonProcessingException {
        String json = resultActions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, ProjectDetailsDTO.class);
    }

    private List<ProjectDTO> extractProjectResultListFromResponse(ResultActions resultActions) throws UnsupportedEncodingException, JsonProcessingException {
        String json = resultActions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<List<ProjectDTO>>() {});
    }

}
