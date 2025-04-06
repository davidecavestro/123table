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
#RUN curl -sSL -O https://jdbc.postgresql.org/download/postgresql-42.7.5.jar
#RUN curl -sSL -O https://download.oracle.com/otn-pub/otn_software/jdbc/237/ojdbc17.jar
#RUN curl -sSL 'https://go.microsoft.com/fwlink/?linkid=2310307' | tar xvvzf -
COPY --chown=1000:0 main.groovy /app
COPY --chown=1000:0 lib.groovy /app
COPY --chown=1000:0 opts.groovy /app

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

#RUN sh /build/runtests.sh

# RUN \
#   JACOCO_PATH=$(printf '%s' /build/org.jacoco.agent-*-runtime.jar) \
#   JAVA_OPTS="-javaagent:${JACOCO_PATH}=destfile=/build/jacoco.exec,classdumpdir=/build/classes,includes=main:lib" \
#   groovy \
#     -cp $(printf '%s:' ${DRIVERS_DIR}/*.jar) \
#     /app/libspec.groovy
# #RUN groovy -cp $(printf '%s:' ${DRIVERS_DIR}/*.jar) /app/libspec.groovy
# RUN \
#   java \ 
#     -jar $(printf '%s' /build/org.jacoco.cli-*-nodeps.jar) \
#     report \
#       /build/jacoco.exec \
#       --sourcefiles /app \
#       --classfiles /build/classes \
#       --xml /build/coverage.xml \
#       --html /build/coverage \
#       --csv /build/coverage.csv

# dist stage
#FROM groovy:${GROOVY_VERSION}-jdk21 as cold

# FROM ghcr.io/bell-sw/liberica-runtime-container:jdk-crac-glibc AS cold
FROM azul/zulu-openjdk:21-jdk-crac-latest AS cold
ARG GROOVY_VERSION
ENV GROOVY_VERSION=${GROOVY_VERSION}

COPY --from=build /opt/groovy /opt/groovy
COPY --from=build --chown=1000:0 /app/app.jar /app/app.jar
COPY --from=build --chown=1000:0 /drivers /drivers
COPY --from=build --chown=1000:0 /app/lib /app/lib

WORKDIR /app
#USER 0:0
#RUN groovyc *.groovy && \
#    jar cvf app.jar *.class

USER 1000:0

COPY --chown=1000:0 --chmod=750 123t /app/123t
ENV DRIVERS_DIR=/drivers
ENV PATH=$PATH:/opt/groovy/bin
#RUN ./123t --help

#RUN JAVA_OPTS=" -XX:CRaCCheckpointTo=/app/cr" groovy -cp $(printf '%s:' ${DRIVERS_DIR}/*.jar) /app/main.groovy -w || true

ENTRYPOINT [ "/app/123t" ]
#ENTRYPOINT [ "groovy", "-cp", "$(printf '%s:' ${DRIVERS_DIR}/*.jar)", "/app/main.groovy" ]
CMD [ "--help" ]

# FROM cold AS warm
# # ENV JAVA_OPTS=" -XX:CRaCCheckpointTo=cr"
# #USER 0:0

# #RUN echo "-h" | /app/123t -w

# RUN export CRAC_MODE=save && echo "-h" | /app/123t
# # ENV JAVA_OPTS=" -XX:CRaCRestoreFrom=/app/cr"
# ENV CRAC_MODE=restore

# #FROM warm AS cold