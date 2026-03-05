FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -pl sap-sdk-demo -am

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/sap-sdk-demo/target/sap-sdk-demo-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
