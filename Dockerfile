FROM maven:3.6.3-openjdk-17-slim AS MAVEN_BUILD
COPY . /
RUN mvn clean package

FROM openjdk:21-slim-bookworm
EXPOSE 8080
COPY --from=MAVEN_BUILD pri-application/target/pri-application-*.jar /app.jar
ENTRYPOINT ["java", "-jar","/app.jar"]
