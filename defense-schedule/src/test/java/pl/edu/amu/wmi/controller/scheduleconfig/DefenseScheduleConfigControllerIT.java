package pl.edu.amu.wmi.controller.scheduleconfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.edu.amu.wmi.enumerations.DefensePhase;
import pl.edu.amu.wmi.model.scheduleconfig.DateRangeDTO;
import pl.edu.amu.wmi.model.scheduleconfig.DefensePhaseDTO;
import pl.edu.amu.wmi.model.scheduleconfig.DefenseScheduleConfigDTO;
import pl.edu.amu.wmi.model.scheduleconfig.TimeRangeDTO;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DefenseScheduleConfigControllerIT {

    private static final String STUDY_YEAR_HEADER_NAME = "study-year";
    private static final String STUDY_YEAR_PART_TIME_HEADER_VALUE = "PART_TIME#2023";
    private static final String STUDY_YEAR_FULL_TIME_HEADER_VALUE = "FULL_TIME#2023";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createDefenseScheduleConfigReturns201() throws Exception {
        //given
        DefenseScheduleConfigDTO defenseScheduleConfigDTO = createDefenseScheduleConfigDTO();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        //when
        final ResultActions resultActions = mockMvc.perform(post("/schedule/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(defenseScheduleConfigDTO))
                .header(STUDY_YEAR_HEADER_NAME, STUDY_YEAR_PART_TIME_HEADER_VALUE));
        //then
        resultActions.andExpect(status().isCreated()).andDo(print());
    }

    @Test
    void getCurrentDefensePhaseReturnsCorrectPhase() throws Exception {
        //given
        DefensePhaseDTO expectedPhase = new DefensePhaseDTO(DefensePhase.DEFENSE_PROJECT_REGISTRATION.getPhaseName());
        //when
        ResultActions resultActions = mockMvc.perform(get("/schedule/config/phase")
                        .header(STUDY_YEAR_HEADER_NAME, STUDY_YEAR_FULL_TIME_HEADER_VALUE));
        //then
        resultActions.andExpect(status().isOk()).andDo(print());
        DefensePhaseDTO phase = extractPhaseFromResponse(resultActions);
        assertThat(phase).isEqualTo(expectedPhase);
    }

    private DefensePhaseDTO extractPhaseFromResponse(ResultActions resultActions) throws JsonProcessingException, UnsupportedEncodingException {
        String json = resultActions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, DefensePhaseDTO.class);
    }

    private DefenseScheduleConfigDTO createDefenseScheduleConfigDTO() {
        DefenseScheduleConfigDTO defenseScheduleConfigDTO = new DefenseScheduleConfigDTO();
        defenseScheduleConfigDTO.setSlotDuration(30);

        DateRangeDTO dateRangeDTO = new DateRangeDTO();
        dateRangeDTO.setStart(LocalDate.of(2024, 1, 8));
        dateRangeDTO.setEnd(LocalDate.of(2024, 1, 9));
        defenseScheduleConfigDTO.setDateRange(dateRangeDTO);

        TimeRangeDTO timeRangeDTO = new TimeRangeDTO();
        timeRangeDTO.setStart(LocalTime.of(8, 0));
        timeRangeDTO.setEnd(LocalTime.of(12, 0));
        defenseScheduleConfigDTO.setTimeRange(timeRangeDTO);

        return defenseScheduleConfigDTO;
    }

}
