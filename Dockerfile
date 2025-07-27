FROM eclipse-temurin:21-jdk AS builder
WORKDIR /opt/app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
RUN chmod +x ./gradlew
COPY src ./src
RUN ./gradlew clean build -x test

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
EXPOSE 8080
COPY --from=builder /opt/app/build/libs/*.jar /opt/app/app.jar
ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
