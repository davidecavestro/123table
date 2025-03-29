# build stage
FROM groovy:4.0-jdk21 AS app-base

USER 0:0
RUN mkdir /app /drivers /data
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


FROM app-base AS build
#RUN curl -sSL -O https://jdbc.postgresql.org/download/postgresql-42.7.5.jar
#RUN curl -sSL -O https://download.oracle.com/otn-pub/otn_software/jdbc/237/ojdbc17.jar
#RUN curl -sSL 'https://go.microsoft.com/fwlink/?linkid=2310307' | tar xvvzf -
COPY --chown=1000:0 main.groovy /app
COPY --chown=1000:0 lib.groovy /app
COPY --chown=1000:0 opts.groovy /app


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
FROM groovy:4

COPY --from=build /app /app
COPY --from=build /drivers /drivers

USER 1000:0
WORKDIR /app

COPY --chmod=750 123t /app/123t
ENV DRIVERS_DIR=/drivers

ENTRYPOINT [ "/app/123t" ]
#ENTRYPOINT [ "groovy", "-cp", "$(printf '%s:' ${DRIVERS_DIR}/*.jar)", "/app/main.groovy" ]
CMD [ "--help" ]
