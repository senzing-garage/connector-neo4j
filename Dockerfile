ARG BASE_IMAGE=senzing/senzing-base:1.6.3
ARG BASE_BUILDER_IMAGE=senzing/base-image-debian:1.0.3

# -----------------------------------------------------------------------------
# Stage: builder
# -----------------------------------------------------------------------------

FROM ${BASE_BUILDER_IMAGE} as builder

# Set Shell to use for RUN commands in builder step.

ENV REFRESHED_AT=2021-12-07

LABEL Name="senzing/connector-neo4j-builder" \
      Maintainer="support@senzing.com" \
      Version="1.0.0"

# Build arguments.

ARG SENZING_G2_JAR_RELATIVE_PATHNAME=unknown
ARG SENZING_G2_JAR_VERSION=unknown

# Set environment variables.

ENV SENZING_ROOT=/opt/senzing
ENV SENZING_G2_DIR=${SENZING_ROOT}/g2
ENV PYTHONPATH=${SENZING_ROOT}/g2/python
ENV LD_LIBRARY_PATH=${SENZING_ROOT}/g2/lib:${SENZING_ROOT}/g2/lib/debian

# Copy Repo files to Builder step.

COPY . /connector-neo4j

# Run the "make" command to create the artifacts.

WORKDIR /connector-neo4j

RUN export CONNECTOR_NEO4J_JAR_VERSION=$(mvn "help:evaluate" -Dexpression=project.version -q -DforceStdout) \
 && make \
     SENZING_G2_JAR_PATHNAME=/connector-neo4j/${SENZING_G2_JAR_RELATIVE_PATHNAME} \
     SENZING_G2_JAR_VERSION=${SENZING_G2_JAR_VERSION} \
     package \
 && cp /connector-neo4j/target/neo4j-connector-${CONNECTOR_NEO4J_JAR_VERSION}.jar "/neo4j-connector.jar" \
 && cp -r /connector-neo4j/target/libs "/libs" \
 && cp -r /connector-neo4j/target/conf "/conf"

# -----------------------------------------------------------------------------
# Stage: Final
# -----------------------------------------------------------------------------

FROM ${BASE_IMAGE}

ENV REFRESHED_AT=2021-12-07

LABEL Name="senzing/connector-neo4j" \
      Maintainer="support@senzing.com" \
      Version="1.8.1"

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
COPY --from=builder "/conf" "/app/conf"

# Make non-root container.

USER 1001

# Runtime execution.

WORKDIR /app
ENTRYPOINT ["java", "-jar", "neo4j-connector.jar"]
