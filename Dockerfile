FROM maven:3-openjdk-11-slim AS j-chess-builder
RUN mkdir /j-chess-server
COPY pom.xml /j-chess-server/pom.xml
COPY src /j-chess-server/src
WORKDIR /j-chess-server
RUN mvn clean compile assembly:single

FROM openjdk:11-jdk-slim
COPY --from=j-chess-builder /j-chess-server/target/j-chess-server-jar-with-dependencies.jar /app.jar
CMD ["java", "-jar", "/app.jar"]
EXPOSE 5123