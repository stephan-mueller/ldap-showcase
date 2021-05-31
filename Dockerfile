FROM openliberty/open-liberty:21.0.0.5-kernel-slim-java11-openj9-ubi

COPY --chown=1001:0 src/main/liberty/config /config

RUN features.sh

COPY --chown=1001:0 target/*.war /config/apps

RUN configure.sh