FROM openjdk:11.0.1-jdk-slim-sid
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
ENV LOG_LEVEL=INFO

ARG BUILD_DATE
ARG VCS_REF
ARG VERSION

LABEL org.opencontainers.image.created=$BUILD_DATE \
org.opencontainers.image.authors="Dmitry Trosman <mailto:dmatik@gmail.com>" \
org.opencontainers.image.url="https://hub.docker.com/r/dmatik/oref-alerts" \
org.opencontainers.image.documentation="https://dmatik.github.io/docs/oref_alerts" \
org.opencontainers.image.source="https://github.com/dmatik/oref-alerts-proxy-ms" \
org.opencontainers.image.version=$VERSION \
org.opencontainers.image.revision=$VCS_REF \
org.opencontainers.image.vendor="https://www.oref.org.il/" \
org.opencontainers.image.licenses="Apache-2.0" \
org.opencontainers.image.ref.name=$VERSION \
org.opencontainers.image.title="dmatik/oref-alerts" \
org.opencontainers.image.description="Java Spring Boot MS to retrieve Israeli Pikud Ha-Oref so called Red Color alerts"