package pl.edu.amu.wmi.controller;

import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.Header;
import io.restassured.specification.MultiPartSpecification;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DataFeedControllerTest {

    @LocalServerPort
    private int port;

    private String uri;

    private static final Header STUDY_YEAR_HEADER = new Header("study-year", "2023#FullTime");
    private static final Header USER_INDEX_HEADER = new Header("user-index-number", "666");

    @PostConstruct
    public void init() {
        uri = "http://localhost:" + port + "/pri";
    }

    @Test
    void shouldCreateStudentsReturn200() {
        //given
        //when
        RestAssured
                .given()
                .multiPart(getMultiPart())
                .header(STUDY_YEAR_HEADER)
                .header(USER_INDEX_HEADER)
                .when()
                .post(uri + "/data/import/student")
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
                .multiPart(getMultiPart())
                .header(STUDY_YEAR_HEADER)
                .header(USER_INDEX_HEADER)
                .when()
                .get(uri + "/data/export/student")
                .then()
                .statusCode(200);
        //then
    }

    private MultiPartSpecification getMultiPart() {
        return new MultiPartSpecBuilder("content".getBytes()).
                fileName("data.csv").
                controlName("data").
                mimeType("text/plain").
                build();
    }

}