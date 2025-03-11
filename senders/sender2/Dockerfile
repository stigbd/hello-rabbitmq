FROM eclipse-temurin:21
RUN mkdir /opt/app
COPY ./target/rabbitmq-sender-1.0-SNAPSHOT.jar /opt/app
CMD ["java", "-jar", "/opt/app/rabbitmq-sender-1.0-SNAPSHOT.jar"]
