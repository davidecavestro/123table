ARG GROOVY_VERSION=4.0.25
# build stage
FROM groovy:${GROOVY_VERSION}-jdk21 AS app-base

USER 0:0
RUN mkdir -p /app/lib /drivers /data
RUN chown -R 1000:0 /app /drivers /data
USER 1000:0

WORKDIR /drivers
COPY build/download.sh /drivers
ENV DRIVERS_DIR=/drivers

RUN sh download.sh org.postgresql:postgresql:42.7.5
RUN sh download.sh com.microsoft.sqlserver:mssql-jdbc:12.10.0.jre11
RUN sh download.sh net.sourceforge.csvjdbc:csvjdbc:1.0.46
RUN sh download.sh com.oracle.database.jdbc:ojdbc11:23.7.0.25.01
RUN sh download.sh com.h2database:h2:2.3.232
RUN sh download.sh org.xerial:sqlite-jdbc:3.49.1.0
RUN sh download.sh org.crac:crac:1.5.0 /app/lib


FROM app-base AS build
COPY --chown=1000:0 *.groovy /app
# COPY --chown=1000:0 lib.groovy /app
# COPY --chown=1000:0 opts.groovy /app

WORKDIR /app

RUN export CRAC_JAR=$(printf '%s' /app/lib/crac-*.jar) \
  prinenv CRAC_JAR && \
  jar -t -f ${CRAC_JAR} && \
  groovyc -cp ${CRAC_JAR} *.groovy && \
    jar cf app.jar *.class

# test stage
FROM app-base AS tests-base
ARG TESTUID=1000
ARG TESTGID=0

USER 0:0
RUN mkdir /build /target
RUN chown -R 1000:0 /build /target
USER 1000:0

RUN sh ${DRIVERS_DIR}/download.sh org.jacoco:org.jacoco.agent:0.8.12 /build runtime
RUN sh ${DRIVERS_DIR}/download.sh org.jacoco:org.jacoco.cli:0.8.12 /build nodeps
RUN sh ${DRIVERS_DIR}/download.sh org.opentest4j.reporting:open-test-reporting-cli:0.2.2 /build standalone

COPY libspec.groovy /app
COPY build/runtests.sh /build

USER 0:0
RUN chown -R ${TESTUID}:${TESTGID} /build /target /drivers /app
RUN chmod -R ug+w /build /target /drivers /app
USER ${TESTUID}:${TESTGID}

ENTRYPOINT [ "sh", "/build/runtests.sh" ]


FROM tests-base AS tests
COPY --from=build /app/*.groovy /app


FROM azul/zulu-openjdk:21-jre-crac-latest AS slim
ARG GROOVY_VERSION
ENV GROOVY_VERSION=${GROOVY_VERSION}

COPY --from=build /opt/groovy /opt/groovy
COPY --from=build --chown=1000:0 /app/app.jar /app/app.jar
COPY --from=build --chown=1000:0 /app/lib /app/lib

WORKDIR /app
USER 1000:0

COPY --chown=1000:0 --chmod=750 123t /app/123t
ENV DRIVERS_DIR=/drivers
ENV PATH=$PATH:/opt/groovy/bin

ENTRYPOINT [ "/app/123t" ]
CMD [ "--help" ]
LABEL org.opencontainers.image.description "Driver-less image for 123table, a containerized command line tool that makes it easy to load rows into a database table. "


FROM slim AS cold
COPY --from=build --chown=1000:0 /drivers /drivers
LABEL org.opencontainers.image.description "Image packaged with some JDBC drivers for 123table, a containerized command line tool that makes it easy to load rows into a database table. "


FROM slim AS fast-slim
RUN \
  export CRAC_MODE=save && \
  echo "-h" | /app/123t \
  || test $? -eq 137
ENV CRAC_MODE=restore
ENV SIDELOAD_DRIVERS=true
LABEL org.opencontainers.image.description "Pre-warmed driver-less image for 123table, a containerized command line tool that makes it easy to load rows into a database table. "


FROM cold AS fast
RUN \
  export CRAC_MODE=save && \
  echo "-h" | /app/123t \
  || test $? -eq 137
ENV CRAC_MODE=restore
ENV SIDELOAD_DRIVERS=false
LABEL org.opencontainers.image.description "Pre-warmed image with JDBC drivers for 123table, a containerized command line tool that makes it easy to load rows into a database table. "
