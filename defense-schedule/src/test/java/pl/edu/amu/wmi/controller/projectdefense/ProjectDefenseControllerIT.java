package pl.edu.amu.wmi.controller.projectdefense;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseDTO;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefensePatchDTO;
import pl.edu.amu.wmi.model.projectdefense.ProjectNameDTO;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectDefenseControllerIT {

    private static final String STUDY_YEAR_HEADER_NAME = "study-year";
    private static final String STUDY_YEAR_FULL_TIME_HEADER_VALUE = "FULL_TIME#2023";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUserDetails(value = "Coordinator 1", userDetailsServiceBeanName = "testUserDetailsServiceDefenseSchedule")
    void getProjectDefensesReturnsResults() throws Exception {
        //given
        int expectedNumberOfResults = 4; //based on data.sql file
        //when
        ResultActions resultActions = mockMvc.perform(get("/schedule/defense")
                .header(STUDY_YEAR_HEADER_NAME, STUDY_YEAR_FULL_TIME_HEADER_VALUE));
        //then
        resultActions.andExpect(status().isOk()).andDo(print());
        List<ProjectDefenseDTO> resultList = extractProjectDefensesFromResponse(resultActions);
        assertThat(resultList)
                .isNotNull()
                .hasSize(expectedNumberOfResults);
    }

    @Test
    void getProjectNamesReturnResults() throws Exception {
        //given - based on data from file data.sql
        int expectedNumberOfProjectNames = 2;
        List<String> expectedProjectNames = List.of("TC: Project Test 1", "TS: Project Test 2");
        //when
        ResultActions resultActions = mockMvc.perform(get("/schedule/defense/projects")
                .header(STUDY_YEAR_HEADER_NAME, STUDY_YEAR_FULL_TIME_HEADER_VALUE));
        //then
        resultActions.andExpect(status().isOk()).andDo(print());
        List<ProjectNameDTO> projectNames = extractProjectNamesFromResponse(resultActions);
        assertThat(projectNames)
                .isNotNull()
                .hasSize(expectedNumberOfProjectNames);

        projectNames.forEach(projectName ->
                assertThat(expectedProjectNames).contains(projectName.getName()));
    }

    @Test
    @WithUserDetails(value = "Student 1", userDetailsServiceBeanName = "testUserDetailsServiceDefenseSchedule")
    void assignProjectToProjectDefenseAssignProjectCorrectly() throws Exception {
        //given based on data from file data.sql
        Long projectDefenseId = 1001L;
        String projectId = "1001";
        ProjectDefensePatchDTO projectDefensePatchDTO = new ProjectDefensePatchDTO(projectId);
        ObjectMapper objectMapper = new ObjectMapper();
        //when
        ResultActions resultActions = mockMvc.perform(patch("/schedule/defense/{projectDefenseId}", projectDefenseId)
                .header(STUDY_YEAR_HEADER_NAME, STUDY_YEAR_FULL_TIME_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDefensePatchDTO)));
        //then
        resultActions.andExpect(status().isOk()).andDo(print());
        List<ProjectDefenseDTO> projectDefenses = extractProjectDefensesFromResponse(resultActions);
        assertThat(projectDefenses).isNotNull();
        boolean isProjectAssignedCorrectly = projectDefenses.stream()
                .anyMatch(projectDefense -> Objects.equals(projectDefense.getProjectId(), String.valueOf(projectId)) &&
                        Objects.equals(Long.valueOf(projectDefense.getProjectDefenseId()), projectDefenseId));
        assertTrue(isProjectAssignedCorrectly);
    }

    private List<ProjectNameDTO> extractProjectNamesFromResponse(ResultActions resultActions) throws UnsupportedEncodingException, JsonProcessingException {
        String json = resultActions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<List<ProjectNameDTO>>() {
        });
    }

    private List<ProjectDefenseDTO> extractProjectDefensesFromResponse(ResultActions resultActions) throws UnsupportedEncodingException, JsonProcessingException {
        String json = resultActions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<List<ProjectDefenseDTO>>() {
        });
    }
}
