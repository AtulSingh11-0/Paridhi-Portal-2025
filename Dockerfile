FROM eclipse-temurin:17-jdk-focal AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# create template.env instead of copying actual .env with secrets
COPY .env.example .env.example

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]