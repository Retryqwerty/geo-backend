# Build
FROM gradle:8.8-jdk21 AS build
WORKDIR /src
COPY . .
RUN gradle bootJar --no-daemon

# Run
FROM eclipse-temurin:21-jre
ENV JAVA_OPTS="-XX:+UseContainerSupport"
WORKDIR /app
COPY --from=build /src/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["bash","-lc","java $JAVA_OPTS -jar app.jar"]
