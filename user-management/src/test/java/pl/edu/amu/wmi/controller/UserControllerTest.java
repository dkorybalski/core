package pl.edu.amu.wmi.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import pl.edu.amu.wmi.model.user.StudentCreationRequestDTO;
import pl.edu.amu.wmi.model.user.StudentDTO;
import pl.edu.amu.wmi.model.user.SupervisorCreationRequestDTO;
import pl.edu.amu.wmi.model.user.SupervisorDTO;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {

    @LocalServerPort
    private int port;

    private static final Header STUDY_YEAR_HEADER = new Header("study-year", "PART_TIME#2023");
    private String uri;

    @PostConstruct
    public void init() {
        uri = "http://localhost:" + port + "/pri";
    }

    @Nested
    class SupervisorUserTest {

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
    class StudentUserTest {

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
            int expectedNumberOfResults = 1;
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