package pl.edu.amu.wmi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.model.user.*;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {TestConfig.class})
class UserControllerIT {

    @LocalServerPort
    private int port;

    private static final Header STUDY_YEAR_HEADER = new Header("study-year", "FULL_TIME#2023");
    private String uri;

    @PostConstruct
    public void init() {
        uri = "http://localhost:" + port + "/pri";
    }

    @Nested
    @AutoConfigureMockMvc
    @ContextConfiguration(classes = {TestConfig.class})
    class UserIT {

        private static final String STUDY_YEAR_HEADER_NAME = "study-year";
        private static final String STUDY_YEAR_HEADER_VALUE = "FULL_TIME#2023";

        @Autowired
        private MockMvc mockMvc;

        @Test
        @WithUserDetails(value = "Coordinator 1", userDetailsServiceBeanName = "testUserDetailsServiceUserManagement")
        void getUserWithCoordinatorRoleReturns200() throws Exception {
            //given
            String expectedUserIndexNumber = "Coordinator 1";
            //when
            final ResultActions resultActions = mockMvc.perform(get("/user").header(STUDY_YEAR_HEADER_NAME, STUDY_YEAR_HEADER_VALUE));
            //then
            resultActions.andExpect(status().isOk()).andDo(print());
            UserDTO userDTO = extractUserDTOFromResponse(resultActions);
            assertThat(userDTO).isNotNull();
            assertThat(userDTO.getIndexNumber()).isEqualTo(expectedUserIndexNumber);
            assertThat(userDTO.getRole()).isEqualTo(UserRole.COORDINATOR.toString());
        }

        @Test
        @WithUserDetails(value = "Supervisor 1", userDetailsServiceBeanName = "testUserDetailsServiceUserManagement")
        void getUserWithSupervisorRoleReturns200() throws Exception {
            //given
            String expectedUserIndexNumber = "Supervisor 1";
            //when
            final ResultActions resultActions = mockMvc.perform(get("/user").header(STUDY_YEAR_HEADER_NAME, STUDY_YEAR_HEADER_VALUE));
            //then
            resultActions.andExpect(status().isOk()).andDo(print());
            UserDTO userDTO = extractUserDTOFromResponse(resultActions);
            assertThat(userDTO).isNotNull();
            assertThat(userDTO.getIndexNumber()).isEqualTo(expectedUserIndexNumber);
            assertThat(userDTO.getRole()).isEqualTo(UserRole.SUPERVISOR.toString());
        }

        @Test
        @WithUserDetails(value = "Student 1", userDetailsServiceBeanName = "testUserDetailsServiceUserManagement")
        void getUserWithStudentRoleReturns200() throws Exception {
            //given
            String expectedUserIndexNumber = "Student 1";
            //when
            final ResultActions resultActions = mockMvc.perform(get("/user").header(STUDY_YEAR_HEADER_NAME, STUDY_YEAR_HEADER_VALUE));
            //then
            resultActions.andExpect(status().isOk()).andDo(print());
            UserDTO userDTO = extractUserDTOFromResponse(resultActions);
            assertThat(userDTO).isNotNull();
            assertThat(userDTO.getIndexNumber()).isEqualTo(expectedUserIndexNumber);
            assertThat(userDTO.getRole()).isEqualTo(UserRole.STUDENT.toString());
        }

        private UserDTO extractUserDTOFromResponse(ResultActions resultActions) throws JsonProcessingException, UnsupportedEncodingException {
            String json = resultActions.andReturn().getResponse().getContentAsString();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, UserDTO.class);
        }
    }

    @Nested
    class SupervisorUserIT {

        @Test
        void createSupervisorReturn200() {
            //given
            String notExistingIndexNumber = "123456";
            SupervisorCreationRequestDTO creationRequest = createSupervisorCreationRequest(notExistingIndexNumber);
            //when
            Response response = RestAssured
                    .given()
                    .header(STUDY_YEAR_HEADER)
                    .contentType(ContentType.JSON)
                    .body(creationRequest)
                    .when()
                    .post(uri + "/user/supervisor");
            //then
            assertThat(response.getStatusCode()).isEqualTo(200);
            SupervisorDTO createdSupervisor = response.as(SupervisorDTO.class);
            assertThat(createdSupervisor.getName()).isEqualTo(creationRequest.getName() + " " + creationRequest.getSurname());
            assertThat(createdSupervisor.getEmail()).isEqualTo(creationRequest.getEmail());
            assertThat(createdSupervisor.getIndexNumber()).isEqualTo(creationRequest.getIndexNumber());
        }

        @Test
        void getSupervisorsReturns200() {
            //given
            int expectedNumberOfResults = 3;
            //when
            Response response = RestAssured
                    .given()
                    .header(STUDY_YEAR_HEADER)
                    .contentType(ContentType.JSON)
                    .when()
                    .get(uri + "/user/supervisor");
            //then
            assertThat(response.getStatusCode()).isEqualTo(200);
            SupervisorDTO[] supervisors = response.as(SupervisorDTO[].class);
            assertEquals(supervisors.length, expectedNumberOfResults);
        }

        private SupervisorCreationRequestDTO createSupervisorCreationRequest(String indexNumber) {
            return SupervisorCreationRequestDTO.builder()
                    .name("Jan")
                    .surname("Kowalski")
                    .email("jankow@amu.edu.pl")
                    .indexNumber(indexNumber)
                    .groupNumber(1)
                    .build();
        }
    }

    @Nested
    class StudentUserIT {

        @Test
        void createStudentReturn200() {
            //given
            String notExistingIndexNumber = "456789";
            StudentCreationRequestDTO creationRequest = createStudentCreationRequest(notExistingIndexNumber);
            //when
            Response response = RestAssured
                    .given()
                    .header(STUDY_YEAR_HEADER)
                    .contentType(ContentType.JSON)
                    .body(creationRequest)
                    .when()
                    .post(uri + "/user/student");
            //then
            assertThat(response.getStatusCode()).isEqualTo(200);
            StudentDTO createdStudent = response.as(StudentDTO.class);
            assertThat(createdStudent.getName()).isEqualTo(creationRequest.getName() + " " + creationRequest.getSurname());
            assertThat(createdStudent.getEmail()).isEqualTo(creationRequest.getEmail());
            assertThat(createdStudent.getIndexNumber()).isEqualTo(creationRequest.getIndexNumber());
        }

        @Test
        void getStudentReturn200AndResultList() {
            //given
            int expectedNumberOfResults = 6;
            //when
            Response response = RestAssured
                    .given()
                    .header(STUDY_YEAR_HEADER)
                    .contentType(ContentType.JSON)
                    .when()
                    .get(uri + "/user/student");
            //then
            assertThat(response.getStatusCode()).isEqualTo(200);
            StudentDTO[] students = response.as(StudentDTO[].class);
            assertEquals(students.length, expectedNumberOfResults);
        }

        private StudentCreationRequestDTO createStudentCreationRequest(String indexNumber) {
            return StudentCreationRequestDTO.builder()
                    .name("Anna")
                    .surname("Nowak")
                    .email("annnow@st.amu.edu.pl")
                    .indexNumber(indexNumber)
                    .build();
        }
    }

}
