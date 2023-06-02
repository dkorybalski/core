package pl.edu.amu.wmi.controller;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import pl.edu.amu.wmi.model.ProjectDetailsDTO;
import pl.edu.amu.wmi.service.ProjectService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectControllerTest {

    @Autowired
    ProjectService projectService;

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
    void getProjectWithCorrectHeadersShouldReturnResultsAndStatus200() {
        int expectedNumberOfProjects = 0;

        List<ProjectDetailsDTO> projects = RestAssured
                .given()
                .header(STUDY_YEAR_HEADER)
                .header(USER_INDEX_HEADER)
                .when()
                .get(uri + "/project")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList(".", ProjectDetailsDTO.class);

        assertThat(projects).hasSize(expectedNumberOfProjects);
    }

    @Test
    void getProjectWithoutHeadersShouldReturnBadRequestErrorAndStatus400() {
        String expectedError = "Bad Request";

        String actualError = RestAssured
                .given()
                .when()
                .get(uri + "/project")
                .then()
                .statusCode(400)
                .extract().body().jsonPath().getString("error");

        assertThat(actualError).isEqualTo(expectedError);
    }


    @Test
    void shouldCreateNewProjectReturnsProjectDataAndStatus201() {
    }
}