#!/bin/bash
#
# Run regression test suite
#
LOG_DIR=$1
if [ -z ${LOG_DIR} ]; then
    echo "Usage: run LOG_DIR"
    exit 1
fi

make init
for f in `ls testsuite/designs/*.run`; do
    bash ${f} >> ${LOG_DIR}/`basename ${f}`.log 2>&1
done
for f in `ls testsuite/tests/*.run`; do
    bash ${f} >> ${LOG_DIR}/report.txt 2>&1
done
