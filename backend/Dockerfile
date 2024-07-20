FROM maven:3.9.8-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2  mvn clean package dependency:copy-dependencies -DincludeScope=runtime

FROM tomcat:10.1-jdk21-temurin-jammy
RUN addgroup app-group && adduser --ingroup app-group app-user
USER app-user:app-group
ARG WAR_FILE=currency-converter.war
WORKDIR /usr/local/tomcat/webapps/
COPY --from=build /app/target/${WAR_FILE} /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]
