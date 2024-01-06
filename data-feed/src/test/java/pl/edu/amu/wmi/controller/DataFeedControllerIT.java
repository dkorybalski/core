package pl.edu.amu.wmi.controller;

import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.Header;
import io.restassured.specification.MultiPartSpecification;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DataFeedControllerIT {

    @LocalServerPort
    private int port;

    private String uri;

    private static final Header STUDY_YEAR_HEADER = new Header("study-year", "PART_TIME#2023");
    private static final Header USER_INDEX_HEADER = new Header("index-number", "666");

    @PostConstruct
    public void init() {
        uri = "http://localhost:" + port + "/pri";
    }

    @Test
    void shouldCreateStudentsReturn200() throws IOException {
        //given
        String csvPath = "src/test/resources/students.csv";
        //when
        RestAssured
                .given()
                .multiPart(getMultiPart(csvPath))
                .header(STUDY_YEAR_HEADER)
                .header(USER_INDEX_HEADER)
                .when()
                .post(uri + "/data/import/student")
                .then()
                .statusCode(200);
        //then
    }

    @Test
    void shouldCreateSupervisorsReturn200() throws IOException {
        //given
        String csvPath = "src/test/resources/supervisors.csv";
        //when
        RestAssured
                .given()
                .multiPart(getMultiPart(csvPath))
                .header(STUDY_YEAR_HEADER)
                .header(USER_INDEX_HEADER)
                .when()
                .post(uri + "/data/import/supervisor")
                .then()
                .statusCode(200);
        //then
    }

    @Test
    void shouldDataExportStudentsDataReturn200() {
        //given
        //when
        RestAssured
                .given()
                .header(STUDY_YEAR_HEADER)
                .header(USER_INDEX_HEADER)
                .when()
                .get(uri + "/data/export/student")
                .then()
                .statusCode(200);
        //then
    }

    @Test
    void shouldExportCriteriaReturn200() {
        //given
        //when
        RestAssured
                .given()
                .header(STUDY_YEAR_HEADER)
                .header(USER_INDEX_HEADER)
                .when()
                .get(uri + "/data/export/criteria")
                .then()
                .statusCode(200);
        //then
    }

    @Test
    void shouldImportCriteriaReturn200() throws IOException {
        //given
        //when
        RestAssured
                .given()
                .multiPart(getCriteriaJson())
                .header(STUDY_YEAR_HEADER)
                .header(USER_INDEX_HEADER)
                .when()
                .post(uri + "/data/import/criteria")
                .then()
                .statusCode(200);
        //then
    }

    private MultiPartSpecification getCriteriaJson() throws IOException {
        Path criteriaJsonPath = Paths.get("src/test/resources/criteria.json");
        byte[] jsonContent = Files.readAllBytes(criteriaJsonPath);
        return new MultiPartSpecBuilder(jsonContent)
                .fileName("data.json")
                .controlName("data")
                .mimeType("json")
                .build();
    }

    private MultiPartSpecification getMultiPart(String path) throws IOException {
        Path studentsCsvPath = Paths.get(path);
        byte[] csvContent = Files.readAllBytes(studentsCsvPath);
        return new MultiPartSpecBuilder(csvContent)
                .fileName("data.csv")
                .controlName("data")
                .mimeType("text/plain")
                .build();
    }

}
