FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
RUN apt-get update && apt-get install -y maven && mvn dependency:go-offline -q
COPY src src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
