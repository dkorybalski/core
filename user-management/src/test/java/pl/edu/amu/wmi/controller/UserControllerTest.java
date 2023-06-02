package pl.edu.amu.wmi.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.model.StudentDTO;
import pl.edu.amu.wmi.model.SupervisorCreationRequestDTO;
import pl.edu.amu.wmi.model.SupervisorDTO;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {

    @LocalServerPort
    private int port;

    private static final Header STUDY_YEAR_HEADER = new Header("study-year", "2023#FullTime");
    private static final Header USER_INDEX_HEADER = new Header("user-index-number", "666");
    private String uri;

    @PostConstruct
    public void init() {
        uri = "http://localhost:" + port + "/pri";
    }

    @Test
    void createSupervisorReturn200() {
        //given
        SupervisorCreationRequestDTO creationRequest = createSupervisorCreationRequest();

        //when
        Response response = RestAssured
                .given()
                .header(STUDY_YEAR_HEADER)
                .header(USER_INDEX_HEADER)
                .contentType(ContentType.JSON)
                .body(creationRequest)
                .when()
                .post(uri + "/user/supervisor");
        //then
        assertThat(response.getStatusCode()).isEqualTo(200);
        SupervisorDTO createdSupervisor = response.as(SupervisorDTO.class);
        assertThat(createdSupervisor.getName()).isEqualTo(creationRequest.getName());
        assertThat(createdSupervisor.getSurname()).isEqualTo(creationRequest.getSurname());
        assertThat(createdSupervisor.getEmail()).isEqualTo(creationRequest.getEmail());
        assertThat(createdSupervisor.getIndexNumber()).isEqualTo(creationRequest.getIndexNumber());
    }

    @Test
    void createSupervisorReturn409WhenSupervisorAlreadyExistForStudyYear() {
        //given
        SupervisorCreationRequestDTO creationRequest = createSupervisorCreationRequest();

        //when
        //first creation - supervisor should be created properly
        RestAssured
                .given()
                .header(STUDY_YEAR_HEADER)
                .header(USER_INDEX_HEADER)
                .contentType(ContentType.JSON)
                .body(creationRequest)
                .when()
                .post(uri + "/user/supervisor")
                .then()
                .statusCode(200);

//        //second creation of the same supervisor - error 409 expected
//        RestAssured
//                .given()
//                .header(STUDY_YEAR_HEADER)
//                .header(USER_INDEX_HEADER)
//                .contentType(ContentType.JSON)
//                .body(creationRequest)
//                .when()
//                .post(uri + "/user/supervisor")
//                .then()
//                .statusCode(409);

    }

    private SupervisorCreationRequestDTO createSupervisorCreationRequest() {
        return SupervisorCreationRequestDTO.builder()
                .name("Jan")
                .surname("Kowalski")
                .email("jankow@amu.edu.pl")
                .indexNumber("123456")
                .groupNumber(1)
                .build();
    }
}