package pl.edu.amu.wmi.controller;

import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
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
                .when()
                .post(uri + "/data/students")
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