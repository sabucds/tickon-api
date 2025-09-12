package com.tickon.e2e;

import io.restassured.RestAssured;
import java.io.File;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class EventUserJourneyE2ETest {

  @Container
  static DockerComposeContainer<?> environment =
      new DockerComposeContainer<>(new File("../../docker-compose.yml"))
          .withExposedService(
              "api-gateway",
              8080,
              Wait.forHttp("/actuator/health")
                  .forStatusCode(200)
                  .withStartupTimeout(Duration.ofMinutes(3)))
          .withExposedService(
              "eureka-server", 8761, Wait.forHttp("/actuator/health").forStatusCode(200));

  @BeforeAll
  static void setup() {
    Integer gatewayPort = environment.getServicePort("api-gateway", 8080);
    RestAssured.baseURI = "http://localhost:" + gatewayPort;
  }
}
