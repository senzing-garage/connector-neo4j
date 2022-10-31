ARG BASE_IMAGE=senzing/senzingapi-runtime:3.3.2
ARG BASE_BUILDER_IMAGE=senzing/base-image-debian:1.0.10

# -----------------------------------------------------------------------------
# Stage: builder
# -----------------------------------------------------------------------------

FROM ${BASE_BUILDER_IMAGE} as builder

ENV REFRESHED_AT=2022-10-27

LABEL Name="senzing/connector-neo4j-builder" \
      Maintainer="support@senzing.com" \
      Version="0.2.1"

# Set environment variables.

ENV SENZING_ROOT=/opt/senzing
ENV SENZING_G2_DIR=${SENZING_ROOT}/g2
ENV PYTHONPATH=${SENZING_ROOT}/g2/python
ENV LD_LIBRARY_PATH=${SENZING_ROOT}/g2/lib:${SENZING_ROOT}/g2/lib/debian

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

ENV REFRESHED_AT=2022-10-27

LABEL Name="senzing/connector-neo4j" \
      Maintainer="support@senzing.com" \
      Version="0.2.1"

HEALTHCHECK CMD ["/app/healthcheck.sh"]

# Run as "root" for system installation.

USER root

# Install packages via apt.

RUN apt update \
 && apt -y install \
      software-properties-common \
 && rm -rf /var/lib/apt/lists/*

# Install Java-11.

RUN wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | apt-key add - \
 && add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/ \
 && apt update \
 && apt install -y adoptopenjdk-11-hotspot \
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
