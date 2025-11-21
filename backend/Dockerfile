FROM eclipse-temurin:21-jdk AS build
LABEL authors="arnotserunyan"
WORKDIR /app

COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew
RUN ./gradlew clean bootJar --no-daemon

# 2) Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# copy the jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# app listens on 8080
EXPOSE 8080

# OpenAI key comes from env
ENV OPENAI_API_KEY=""

ENTRYPOINT ["java", "-jar", "app.jar"]
