FROM adoptopenjdk/openjdk11:jre-11.0.11_9-alpine
COPY /build/libs/nosto-exchange.jar /nosto-exchange.jar
ENV JAVA_TOOL_OPTIONS "-Xmx300m -Xss512k -XX:CICompilerCount=2 -XX:+UseContainerSupport -Dfile.encoding=UTF-8"
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/nosto-exchange.jar"]
