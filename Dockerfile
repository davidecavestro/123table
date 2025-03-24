# build stage
FROM groovy:4 AS build

USER 0:0
RUN mkdir /app /build /drivers /data

WORKDIR /drivers
COPY download.sh /drivers
ENV DRIVERS_DIR=/drivers

RUN sh download.sh org.postgresql:postgresql:42.7.5
RUN sh download.sh com.microsoft.sqlserver:mssql-jdbc:12.10.0.jre11
RUN sh download.sh net.sourceforge.csvjdbc:csvjdbc:1.0.46
RUN sh download.sh com.oracle.database.jdbc:ojdbc11:23.7.0.25.01

#RUN curl -sSL -O https://jdbc.postgresql.org/download/postgresql-42.7.5.jar
#RUN curl -sSL -O https://download.oracle.com/otn-pub/otn_software/jdbc/237/ojdbc17.jar
#RUN curl -sSL 'https://go.microsoft.com/fwlink/?linkid=2310307' | tar xvvzf -
WORKDIR /app
COPY main.groovy /app
COPY lib.groovy /app
RUN chown -R 1000:0 /app /build /drivers /data

USER 1000:0

ENTRYPOINT [ "groovy", "-cp", "$(printf '%s:' /${DRIVERS_DIR}/*.jar)", "/app/main.groovy" ]
CMD [ "--help" ]


# test stage
FROM build AS tests

COPY libspec.groovy /app
RUN sh ${DRIVERS_DIR}/download.sh org.jacoco:org.jacoco.agent:0.8.12 /build runtime
RUN sh ${DRIVERS_DIR}/download.sh org.jacoco:org.jacoco.cli:0.8.12 /build nodeps
#RUN sh ${DRIVERS_DIR}/download.sh org.jacoco:org.jacoco.core:0.8.12 /build

RUN \
  JACOCO_PATH=$(printf '%s' /build/org.jacoco.agent-*-runtime.jar) \
  JAVA_OPTS="-javaagent:${JACOCO_PATH}=destfile=/build/jacoco.exec,classdumpdir=/build/classes,includes=main:lib" \
  groovy \
    -cp $(printf '%s:' ${DRIVERS_DIR}/*.jar) \
    /app/libspec.groovy
#RUN groovy -cp $(printf '%s:' ${DRIVERS_DIR}/*.jar) /app/libspec.groovy
RUN \
  java \ 
    -jar $(printf '%s' /build/org.jacoco.cli-*-nodeps.jar) \
    report \
      /build/jacoco.exec \
      --sourcefiles /app \
      --classfiles /build/classes \
      --xml /build/coverage.xml \
      --html /build/coverage \
      --csv /build/coverage.csv

# dist stage
#FROM build AS dist