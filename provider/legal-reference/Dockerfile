# Use the official AdoptOpenJDK for a base image.
# https://hub.docker.com/_/openjdk
FROM openjdk:8-slim
WORKDIR /app
COPY target/legal-reference-0.0.1-SNAPSHOT-spring-boot.jar legal.jar
# Run the web service on container startup.
CMD java -Djava.security.egd=file:/dev/./urandom -Dserver.port=8080 -jar /app/legal.jar
