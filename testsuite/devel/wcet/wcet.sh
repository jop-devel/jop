# Utilities for running WCET tests

function assert_set()
{
    for v in $@ ; do
        eval val=\$$v
        if [ -z "${val}" ] ; then
            echo "${v} not set. Exit." >&2
            exit 1
        fi
    done
}

assert_set LOGDIR

function show_help()
{
    echo ''
    echo '# Regression Tests for WCET analysis on JOP'
    echo '# Formats:'
    echo '# moet_${METHOD} ${ACTUAL} ${EXPECTED} ${DIFF}'
    echo '# wcet_${METHOD} ${ACTUAL} ${EXPECTED} ${DIFF} ${RATIO}'
    echo ''
    echo '# DIFF ... difference between ACTUAL and EXPECTED'
    echo '# RATIO ... the error ratio of the WCET relative to the MOET'
    echo ''
}

function logfile()
{
    assert_set LOGDIR P3
    if [ -z "${1}" ] ; then
        echo ${LOGDIR}/${P3}.log
    else
        echo ${LOGDIR}/${P3}_${1}.log
    fi
}

# make_sim key expected-value jsim-args...
#
# @print    moet_${METHOD} ${ACTUAL} ${EXPECTED} ${DIFF}
# @uses     LOGDIR,P1,P2,P3
# @exports  SIMVAL
function make_sim()
{
    KEY=$1
    EXPC=$2
    shift 2

    assert_set LOGDIR P1 P2 P3
    LOG=$(logfile ${KEY})

    make jop_config MEASURE=true >/dev/null 2>&1
    make jsim P1=${P1} P2=${P2} P3=${P3} $@ >"${LOG}" 2>&1
    SIMVAL=$(cat "${LOG}" | perl -n -e'/max:.*?(\d+)/ && print $1')
    echo moet_${KEY} "${SIMVAL}" "${EXPC}" $((SIMVAL-EXPC))
    export SIMVAL
}

function make_java()
{
    make MEASURE=false jop_config  >/dev/null 2>&1
    make java_app P1=${P1} P2=${P2} P3=${P3} $@ >/dev/null 2>&1
}

function make_wcet()
{
    KEY=$1
    EXPC=$2
    shift 2

    assert_set LOGDIR P1 P2 P3
    LOG=$(logfile ${KEY})

    make wcet P1=${P1} P2=${P2} P3=${P3} $@ >"${LOG}" 2>&1
    WCETVAL=$(cat "${LOG}" | perl -n -e'/wcet:.*?(\d+)/ && print $1')
    if [ -z "${WCETVAL}" ] ; then
        echo "WCET calculation failed" >&2
        tail "${LOG}" >&2
    else
        DIFFEXP=$((WCETVAL-EXPC))
        DIFFSIM=$((WCETVAL-SIMVAL))
        if [ ${DIFFSIM} -lt 0 ] ; then
            echo "UNDERESTIMATION: ${WCETVAL} < ${SIMVAL}" >&2
        fi
        RATIO=$(bc <<< "scale=2; ${DIFFSIM} / ${SIMVAL} + 1.0")
        echo wcet_${KEY} "${WCETVAL}" "${EXPC}" ${DIFFEXP} ${RATIO}
    fi
}
