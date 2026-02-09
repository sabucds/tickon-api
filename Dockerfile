FROM eclipse-temurin:21-jdk-jammy as base
WORKDIR /build
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/

FROM base as test
WORKDIR /build
# Copy all module structure for Maven multi-module build
COPY pom.xml .
COPY java-formatter.xml .
COPY common/ common/
COPY services/ services/
COPY tests/ tests/
# Copy root src if it exists
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw test

FROM base as deps
ARG SERVICE_PATH
WORKDIR /build
# Copy all module structure for Maven multi-module build
COPY pom.xml .
COPY java-formatter.xml .
COPY common/pom.xml common/
COPY services/eureka-server/pom.xml services/eureka-server/
COPY services/event-service/pom.xml services/event-service/
COPY services/identity-service/pom.xml services/identity-service/
COPY services/api-gateway/pom.xml services/api-gateway/
COPY tests/e2e/pom.xml tests/e2e/
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -DskipTests

FROM deps AS package
ARG SERVICE_PATH
WORKDIR /build
COPY ${SERVICE_PATH}/src ${SERVICE_PATH}/src/
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -pl ${SERVICE_PATH} -am -DskipTests && \
    mv ${SERVICE_PATH}/target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout -pl ${SERVICE_PATH})-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout -pl ${SERVICE_PATH}).jar ${SERVICE_PATH}/target/app.jar

FROM package AS extract
ARG SERVICE_PATH
WORKDIR /build
RUN java -Djarmode=layertools -jar ${SERVICE_PATH}/target/app.jar extract --destination ${SERVICE_PATH}/target/extracted

FROM extract AS development
ARG SERVICE_PATH
WORKDIR /build
RUN cp -r /build/${SERVICE_PATH}/target/extracted/dependencies/. ./
RUN cp -r /build/${SERVICE_PATH}/target/extracted/spring-boot-loader/. ./
RUN cp -r /build/${SERVICE_PATH}/target/extracted/snapshot-dependencies/. ./
RUN cp -r /build/${SERVICE_PATH}/target/extracted/application/. ./
CMD [ "java", "-Dspring.profiles.active=postgres", "org.springframework.boot.loader.launch.JarLauncher" ]

FROM eclipse-temurin:21-jre-jammy AS final
ARG UID=10001
ARG SERVICE_PATH
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser
COPY --from=extract build/${SERVICE_PATH}/target/extracted/dependencies/ ./
COPY --from=extract build/${SERVICE_PATH}/target/extracted/spring-boot-loader/ ./
COPY --from=extract build/${SERVICE_PATH}/target/extracted/snapshot-dependencies/ ./
COPY --from=extract build/${SERVICE_PATH}/target/extracted/application/ ./
EXPOSE 8080
ENTRYPOINT [ "java", "-Dspring.profiles.active=postgres", "org.springframework.boot.loader.launch.JarLauncher" ]
