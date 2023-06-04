package pl.edu.amu.wmi.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import pl.edu.amu.wmi.model.user.SupervisorCreationRequestDTO;
import pl.edu.amu.wmi.model.user.SupervisorDTO;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

    // TODO: 6/4/2023 fix spring security
    @Disabled
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
        assertThat(createdSupervisor.getName()).isEqualTo(creationRequest.getName() + " " + creationRequest.getSurname());
        assertThat(createdSupervisor.getEmail()).isEqualTo(creationRequest.getEmail());
        assertThat(createdSupervisor.getIndexNumber()).isEqualTo(creationRequest.getIndexNumber());
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