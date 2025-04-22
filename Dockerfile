FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y \
    postgis \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/*
RUN which pgsql2shp || echo "pgsql2shp not found"
ADD target/chorvoq-gis-backend-0.0.1-SNAPSHOT.jar app.jar
ENV TZ=Asia/Tashkent
EXPOSE 8080
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]