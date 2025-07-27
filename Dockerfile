FROM openjdk:21-jdk-slim

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

COPY src src

RUN chmod +x ./gradlew

RUN ./gradlew build -x test

CMD ["java", "-jar", "build/libs/crudjava-0.0.1-SNAPSHOT.jar"]
