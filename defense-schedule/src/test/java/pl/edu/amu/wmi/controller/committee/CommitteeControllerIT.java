package pl.edu.amu.wmi.controller.committee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;
import pl.edu.amu.wmi.model.committee.ChairpersonAssignmentDTO;
import pl.edu.amu.wmi.model.committee.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.model.committee.SupervisorStatisticsDTO;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommitteeControllerIT {

    private static final String STUDY_YEAR_HEADER_NAME = "study-year";
    private static final String STUDY_YEAR_FULL_TIME_HEADER_VALUE = "FULL_TIME#2023";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUserDetails(value = "Coordinator 1", userDetailsServiceBeanName = "testUserDetailsServiceDefenseSchedule")
    void getSupervisorsAvailabilityReturnsAggregatedSupervisorAvailability() throws Exception {
        //given - based on data from file data.sql
        int expectedNumberOfDayKeys = 2;
        String[] expectedDayKeys = {"08.01.2024 | Mon", "09.01.2024 | Tue"};
        int expectedNumberOfSupervisors = 3;
        String[] expectedSupervisorIds = {"10", "20", "30"};
        int expectedNumberOfSupervisorAssignments = 8;
        //when
        ResultActions resultActions = mockMvc.perform(get("/schedule/committee/supervisor")
                .header(STUDY_YEAR_HEADER_NAME, STUDY_YEAR_FULL_TIME_HEADER_VALUE));
        //then
        resultActions.andExpect(status().isOk()).andDo(print());
        Map<String, Map<String, Map<String, SupervisorDefenseAssignmentDTO>>> result = extractAggregatedSupervisorAvailabilityFromResponse(resultActions);

        assertThat(result).isNotNull()
                .hasSize(expectedNumberOfDayKeys)
                .containsKeys(expectedDayKeys);

        result.entrySet().forEach(mapOfDays -> {
                    assertThat(mapOfDays.getValue())
                            .isNotNull()
                            .hasSize(expectedNumberOfSupervisors)
                            .containsKeys(expectedSupervisorIds);
                    mapOfDays.getValue().forEach((supervisor, supervisorAssignments) -> {
                        assertThat(supervisorAssignments)
                                .isNotNull()
                                .hasSize(expectedNumberOfSupervisorAssignments);
                    });
                }
        );
    }

    @Test
    @WithUserDetails(value = "Coordinator 1", userDetailsServiceBeanName = "testUserDetailsServiceDefenseSchedule")
    void getChairpersonAssignmentsReturnsChairpersonAssignments() throws Exception {
        //given - based on data from file data.sql
        int expectedNumberOfDayKeys = 2;
        String[] expectedDayKeys = {"08.01.2024 | Mon", "09.01.2024 | Tue"};
        //when
        ResultActions resultActions = mockMvc.perform(get("/schedule/committee/chairperson")
                .header(STUDY_YEAR_HEADER_NAME, STUDY_YEAR_FULL_TIME_HEADER_VALUE));
        //then
        resultActions.andExpect(status().isOk()).andDo(print());
        Map<String, Map<CommitteeIdentifier, ChairpersonAssignmentDTO>> chairpersonAssignments = extractChairpersonAssignmentsFromResponse(resultActions);
        assertThat(chairpersonAssignments)
                .isNotNull()
                .hasSize(expectedNumberOfDayKeys)
                .containsKeys(expectedDayKeys);
    }

    @Test
    @WithUserDetails(value = "Coordinator 1", userDetailsServiceBeanName = "testUserDetailsServiceDefenseSchedule")
    void getSupervisorStatisticsReturnsStatistics() throws Exception {
        //given - based on data from file data.sql
        int expectedNumberOfResults = 3;
        //when
        ResultActions resultActions = mockMvc.perform(get("/schedule/committee/statistics")
                .header(STUDY_YEAR_HEADER_NAME, STUDY_YEAR_FULL_TIME_HEADER_VALUE));
        //then
        resultActions.andExpect(status().isOk()).andDo(print());
        List<SupervisorStatisticsDTO> supervisorStatistics = extractSupervisorStatisticsFromResponse(resultActions);
        assertThat(supervisorStatistics)
                .isNotNull()
                .hasSize(expectedNumberOfResults);
    }

    private Map<String, Map<CommitteeIdentifier, ChairpersonAssignmentDTO>> extractChairpersonAssignmentsFromResponse(ResultActions resultActions) throws UnsupportedEncodingException, JsonProcessingException {
        String json = resultActions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<Map<String, Map<CommitteeIdentifier, ChairpersonAssignmentDTO>>>() {
        });
    }

    private List<SupervisorStatisticsDTO> extractSupervisorStatisticsFromResponse(ResultActions resultActions) throws UnsupportedEncodingException, JsonProcessingException {
        String json = resultActions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<List<SupervisorStatisticsDTO>>() {
        });
    }

    private Map<String, Map<String, Map<String, SupervisorDefenseAssignmentDTO>>> extractAggregatedSupervisorAvailabilityFromResponse
            (ResultActions resultActions) throws UnsupportedEncodingException, JsonProcessingException {
        String json = resultActions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<Map<String, Map<String, Map<String, SupervisorDefenseAssignmentDTO>>>>() {
        });
    }
}
