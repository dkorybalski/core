package pl.edu.amu.wmi.controller;

import io.restassured.RestAssured;
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
    void shouldTestControllerReturn200() {
        //given
        //when
        RestAssured.get(uri + "/data/")
                .then()
                .statusCode(200);
        //then
    }

}