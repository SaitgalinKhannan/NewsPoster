FROM ubuntu:latest
LABEL authors="Khannan"
FROM openjdk:17
WORKDIR /app

COPY build/libs/NewsPoster-1.0-all.jar /app/NewsPoster-1.0-all.jar
COPY admins.json /app/admins.json
COPY config.json /app/config.json

CMD ["java", "-jar", "NewsPoster-1.0-all.jar config.json"]