FROM alpine/java:21-jdk

WORKDIR /app

COPY --chown=app:app ./build/libs/app.jar ./app.jar

CMD ["java", "-jar", "--add-opens=java.base/java.lang=ALL-UNNAMED", "app.jar"]