package pl.edu.amu.wmi.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class DiplomasRestClient {
    private final RestTemplate restTemplate;

    public ResponseEntity<String> forwardRequest(HttpServletRequest request) {
        try {
            URI url = createForwardUrl(request);
            System.out.println(url);
            return sendRequest(restTemplate, url, HttpMethod.valueOf(request.getMethod()),
                getRequestBodyFromRequest(request));
        } catch (HttpStatusCodeException ex) {
            return mapToResponseEntity(ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            return mapToResponseEntity();
        }
    }

    private static String getRequestBodyFromRequest(HttpServletRequest request) throws IOException {
        return request.getReader()
            .lines()
            .collect(Collectors.joining());
    }

    private ResponseEntity<String> mapToResponseEntity(HttpStatusCodeException ex) {
        return ResponseEntity.status(ex.getStatusCode())
            .body(ex.getResponseBodyAsString());
    }

    private ResponseEntity<String> mapToResponseEntity() {
        return ResponseEntity.status(500)
            .build();
    }

    private static URI createForwardUrl(HttpServletRequest request) throws URISyntaxException {
        final String originalUrl = request.getRequestURL().toString();
        return UriComponentsBuilder.newInstance()
            .uri(new URI("http://localhost:3300" +  originalUrl.substring(originalUrl.indexOf("/pri"))))
            .build(true)
            .toUri();
    }

    private <T> ResponseEntity<String> sendRequest(RestTemplate restTemplate, URI url, HttpMethod requestType, T data) {
        HttpEntity<T> entity = new HttpEntity<>(data);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, requestType, entity, String.class);
        return ResponseEntity.status(responseEntity.getStatusCode())
            .body(responseEntity.getBody());
    }
}