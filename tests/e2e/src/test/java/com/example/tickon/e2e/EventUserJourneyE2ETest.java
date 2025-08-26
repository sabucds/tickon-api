package com.example.tickon.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;

@Testcontainers
class EventUserJourneyE2ETest {

    @Container
    static DockerComposeContainer<?> environment = new DockerComposeContainer<>(
            new File("../../docker-compose.yml"))
            .withExposedService("api-gateway", 8080,
                Wait.forHttp("/actuator/health")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(3)))
            .withExposedService("eureka-server", 8761,
                Wait.forHttp("/actuator/health")
                    .forStatusCode(200));

    @BeforeAll
    static void setup() {
        Integer gatewayPort = environment.getServicePort("api-gateway", 8080);
        RestAssured.baseURI = "http://localhost:" + gatewayPort;
    }

    @Test
    void userCanCreateEventAndRetrieveIt() {
        // Wait for services to register with Eureka
        await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    given()
                        .when()
                        .get("/actuator/health")
                        .then()
                        .statusCode(200);
                });

        // Create a new event
        String eventId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "name": "Summer Music Festival",
                        "description": "Annual summer festival",
                        "dateTime": "2024-07-15T18:00:00",
                        "venue": "Central Park",
                        "capacity": 5000,
                        "price": 75.0
                    }
                    """)
                .when()
                .post("/api/events")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Retrieve the created event
        given()
                .when()
                .get("/api/events/" + eventId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Summer Music Festival"))
                .body("venue", equalTo("Central Park"));

        // List all events
        given()
                .when()
                .get("/api/events")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].name", equalTo("Summer Music Festival"));
    }
}