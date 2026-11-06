package mx.edu.utvt.schoolmanagement;

import java.net.URI;
import java.net.http.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StudentApiIntegrationTest {
    @Autowired
    Environment environment;
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        URI uri = URI.create("http://localhost:" + environment.getProperty("local.server.port") + path);
        var builder = HttpRequest.newBuilder(uri).header("Content-Type", "application/json");
        builder.method(method,
                body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void exposesOpenApiAndSwaggerUi() throws Exception {
        var spec = request("GET", "/v3/api-docs", null);
        assertEquals(200, spec.statusCode());
        assertTrue(spec.body().contains("UTVT School Management API"));
        assertTrue(spec.body().contains("/api/v1/students/{id}/status"));
        assertTrue(spec.body().contains("StudentResponse"));
        assertEquals(200, request("GET", "/swagger-ui/index.html", null).statusCode());
    }

    @Test
    void enforcesHttpValidationAndErrorContracts() throws Exception {
        String path = "/api/v1/students";
        assertEquals(400, request("POST", path, "{}").statusCode());
        assertEquals(400, request("GET", path + "?size=101", null).statusCode());
        var missing = request("GET", path + "/999999", null);
        assertEquals(404, missing.statusCode());
        assertTrue(missing.headers().firstValue("Content-Type").orElse("").contains("application/problem+json"));
        String payload = """
                {"studentNumber":"API-001","firstName":"Ana","paternalSurname":"Lopez","email":"ana@utvtol.edu.mx"}
                """;
        var created = request("POST", path, payload);
        assertEquals(201, created.statusCode());
        String location = URI.create(created.headers().firstValue("Location").orElseThrow()).getPath();
        assertEquals(200, request("GET", location, null).statusCode());
        assertEquals(409, request("POST", path, payload).statusCode());
        var deactivated = request("PATCH", location + "/status", "{\"active\":false}");
        assertEquals(200, deactivated.statusCode());
        assertTrue(deactivated.body().contains("\"active\":false"));
    }
}
