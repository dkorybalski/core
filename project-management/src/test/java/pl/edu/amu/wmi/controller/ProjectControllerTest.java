package pl.edu.amu.wmi.controller;

import io.restassured.RestAssured;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import pl.edu.amu.wmi.model.ProjectDetailsDTO;
import pl.edu.amu.wmi.service.ProjectService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectControllerTest {

    @Autowired
    ProjectService projectService;

    @LocalServerPort
    private int port;

    private String uri;

    @PostConstruct
    public void init() {
        uri = "http://localhost:" + port + "/pri";
    }

    @Test
    void shouldGetProjectsReturnsAllEntriesAndStatus200() {
        //given
        int expectedNumberOfProjects = 0;
        //when
        List<ProjectDetailsDTO> projects = RestAssured.get(uri + "/project/")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList(".", ProjectDetailsDTO.class);
        //then
        assertThat(projects).hasSize(expectedNumberOfProjects);
    }

    @Test
    void shouldCreateNewProjectReturnsProjectDataAndStatus201() {
    }
}