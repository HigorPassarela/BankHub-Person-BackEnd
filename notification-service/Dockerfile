FROM eclipse-temurin:21-jre-alpine

ENV TZ=America/Sao_Paulo

WORKDIR /app

COPY build/libs/*-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]