FROM azul/zulu-openjdk:17.0.5
RUN mkdir /app
WORKDIR /app
COPY app.jar /app/
CMD ["java", "-jar", "/app/app.jar"]
EXPOSE 8080
