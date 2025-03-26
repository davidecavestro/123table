#!/usr/bin/env bash

# Run tests with coverage.
#
# Supports the following optional positional args:
# - path to the writeable build directory providing the jacoco agent jar 
# - path to the writeable target directory where reports are exported
# - path to the sources dir
# - path to the writeable directory where temporary classes are generated

echo "runnning tests"
export BUILDDIR=${1:-"/build"}
export TARGETDIR=${2:-"/target"}
export SOURCESDIR=${3:-"/app"}
export CLASSESDIR=${4:-"${BUILDDIR}/classes"}
set -x

cd ${SOURCESDIR}

JACOCO_PATH=$(printf '%s' ${BUILDDIR}/org.jacoco.agent-*-runtime.jar) \
AGENT_CFG="-javaagent:${JACOCO_PATH}=destfile=${BUILDDIR}/jacoco.exec,classdumpdir=${CLASSESDIR},includes=main:lib" \
JUNIT_CFG="\
-Djunit.platform.reporting.output.files=TEST-report.xml,TEST-report.json,TEST-report.html \
-Djunit.platform.reporting.open.xml.enabled=false \
-Djunit.platform.output.capture.stdout=true \
-Djunit.platform.output.capture.stderr=true \
-Djunit.platform.reporting.output.dir=${TARGETDIR}/reports" \
JAVA_OPTS="${AGENT_CFG} ${JUNIT_CFG}" \
groovy \
  -cp $(printf '%s:' ${DRIVERS_DIR}/*.jar)$(printf '%s:' ${BUILDDIR}/spock-*.jar) \
  ${SOURCESDIR}/libspec.groovy

ls -lha ${BUILDDIR}
ls -lha ${TARGETDIR}
ls -lha ${SOURCESDIR}
ls -lha ${CLASSESDIR}
# copy test reports to target dir
#cp -r ${SOURCESDIR}/spock ${TARGETDIR}/tests

# generate coverage report to target dir
java \
  -jar $(printf '%s' ${BUILDDIR}/org.jacoco.cli-*-nodeps.jar) \
  report \
    ${BUILDDIR}/jacoco.exec \
    --sourcefiles ${SOURCESDIR} \
    --classfiles ${CLASSESDIR} \
    --xml ${TARGETDIR}/coverage.xml \
    --html ${TARGETDIR}/coverage \
    --csv ${TARGETDIR}/coverage.csv

# generate html report for tests to target dir
java \
  -jar $(printf '%s' ${BUILDDIR}/open-test-reporting-cli-*.jar) \
  html-report \
  --output ${TARGETDIR}/reports/tests.html \
  ${TARGETDIR}/reports/junit-platform-events-*.xml	