ARG BASE_IMAGE=senzing/senzingapi-runtime:3.6.0
ARG BASE_BUILDER_IMAGE=senzing/base-image-debian:1.0.19

# -----------------------------------------------------------------------------
# Stage: builder
# -----------------------------------------------------------------------------

FROM ${BASE_BUILDER_IMAGE} as builder

ENV REFRESHED_AT=2023-06-29

LABEL Name="senzing/connector-neo4j-builder" \
      Maintainer="support@senzing.com" \
      Version="0.2.2"

# Set environment variables.

ENV SENZING_ROOT=/opt/senzing
ENV PYTHONPATH=${SENZING_ROOT}/g2/python
ENV LD_LIBRARY_PATH=${SENZING_ROOT}/g2/lib:${SENZING_ROOT}/g2/lib/debian

# Install java-17
# This is a requirement for neo4j java client 5.0 and higher.
RUN wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - \
 && echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list \
 && apt-get update \
 && apt-get -y install temurin-17-jdk

# Build "connector-neo4j.jar"

COPY . /connector-neo4j
WORKDIR /connector-neo4j

RUN export CONNECTOR_NEO4J_JAR_VERSION=$(mvn "help:evaluate" -Dexpression=project.version -q -DforceStdout) \
 && make package \
 && cp /connector-neo4j/target/neo4j-connector-${CONNECTOR_NEO4J_JAR_VERSION}.jar "/neo4j-connector.jar" \
 && cp -r /connector-neo4j/target/libs "/libs"

# -----------------------------------------------------------------------------
# Stage: Final
# -----------------------------------------------------------------------------

FROM ${BASE_IMAGE}

ENV REFRESHED_AT=2023-06-29

LABEL Name="senzing/connector-neo4j" \
      Maintainer="support@senzing.com" \
      Version="0.2.2"

HEALTHCHECK CMD ["/app/healthcheck.sh"]

# Run as "root" for system installation.

USER root

# Install packages via apt.

RUN apt-get update \
 && apt-get -y install \
      software-properties-common \
 && rm -rf /var/lib/apt/lists/*

# Service exposed on port 8080.

EXPOSE 8080

# Copy files from builder step.

COPY --from=builder "/neo4j-connector.jar" "/app/neo4j-connector.jar"
COPY --from=builder "/libs" "/app/libs"

# Make non-root container.

USER 1001

# Runtime execution.

WORKDIR /app
ENTRYPOINT ["java", "-jar", "neo4j-connector.jar"]
