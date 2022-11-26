FROM openjdk:17-alpine3.14
COPY ./build/libs/review-bot-0.0.1-SNAPSHOT.jar .
CMD ["java", "-jar", "-Xms128m", "-Xmx300m", "review-bot-0.0.1-SNAPSHOT.jar"]