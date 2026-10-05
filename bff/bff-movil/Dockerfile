FROM maven:3.9-eclipse-temurin-21 AS construccion
WORKDIR /fuente
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl bff/bff-movil -am package -DskipTests
RUN java -Djarmode=tools -jar bff/bff-movil/target/bff-movil-0.0.1-SNAPSHOT.jar extract --layers --destination /extraido

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S banco && adduser -S banco -G banco
WORKDIR /app
COPY --from=construccion --chown=banco:banco /extraido/dependencies/ ./
COPY --from=construccion --chown=banco:banco /extraido/spring-boot-loader/ ./
COPY --from=construccion --chown=banco:banco /extraido/snapshot-dependencies/ ./
COPY --from=construccion --chown=banco:banco /extraido/application/ ./
USER banco
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8091
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -q -O /dev/null http://localhost:8091/actuator/health/liveness || exit 1
ENTRYPOINT ["java", "-jar", "bff-movil-0.0.1-SNAPSHOT.jar"]
