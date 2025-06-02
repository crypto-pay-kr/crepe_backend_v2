FROM openjdk:17-jdk-slim

RUN cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo "Asia/Seoul" > /etc/timezone

COPY $ROOT_DIRECTORY/gradlew .
COPY $ROOT_DIRECTORY/gradle gradle
COPY $ROOT_DIRECTORY/build.gradle .
COPY $ROOT_DIRECTORY/settings.gradle .
COPY $ROOT_DIRECTORY/src src

RUN chmod +x ./gradlew
RUN ./gradlew tasks
RUN ./gradlew bootjar

RUN cp ./build/libs/*.jar app.jar
RUN chmod +x ./app.jar

ENTRYPOINT ["java", "-Xms512m", "-Xmx1g", "-XX:+UseG1GC", "-XX:+UseContainerSupport", "-jar", "app.jar"]
