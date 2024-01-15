FROM openjdk:17-alpine as builder
WORKDIR app
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM openjdk:17-alpine
WORKDIR app
COPY --from=builder app/dependencies/ ./
COPY --from=builder app/spring-boot-loader/ ./
COPY --from=builder app/snapshot-dependencies/ ./
COPY --from=builder app/application/ ./
COPY src/main/resources/data/ ./src/main/resources/data/

ENTRYPOINT ["java", "-Dspring.profiles.active=${ACTIVE_SPRING_PROFILE}", "-Duser.timezone=Asia/Seoul", "org.springframework.boot.loader.launch.JarLauncher"]