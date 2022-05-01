FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine
COPY /build/libs/nosto-exchange.jar /nosto-exchange.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/nosto-exchange.jar"]