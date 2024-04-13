FROM amazoncorretto:11-alpine-jdk
COPY statistics-service/target/*.jar statistics-service.jar
ENTRYPOINT ["java","-jar","/statistics-service.jar"]