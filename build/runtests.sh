#!/usr/bin/env bash

# Run tests with coverage.
#
# Supports the following optional positional args:
# - path to the writeable build directory providing the jacoco agent jar 
# - path to the writeable target directory where reports are exported
# - path to the sources dir
# - path to the writeable directory where temporary classes are generated

export BUILDDIR=${1:-"/build"}
export TARGETDIR=${2:-"/target"}
export SOURCESDIR=${3:-"/app"}
export CLASSESDIR=${4:-"${BUILDDIR}/classes"}

JACOCO_PATH=$(printf '%s' ${BUILDDIR}/org.jacoco.agent-*-runtime.jar) \
JAVA_OPTS="-javaagent:${JACOCO_PATH}=destfile=${BUILDDIR}/jacoco.exec,classdumpdir=${CLASSESDIR},includes=main:lib" \
groovy \
  -cp $(printf '%s:' ${DRIVERS_DIR}/*.jar) \
  ${SOURCESDIR}/libspec.groovy

# generate coverage report
java \
  -jar $(printf '%s' ${BUILDDIR}/org.jacoco.cli-*-nodeps.jar) \
  report \
    ${BUILDDIR}/jacoco.exec \
    --sourcefiles ${SOURCESDIR} \
    --classfiles ${CLASSESDIR} \
    --xml ${TARGETDIR}/coverage.xml \
    --html ${TARGETDIR}/coverage \
    --csv ${TARGETDIR}/coverage.csv
