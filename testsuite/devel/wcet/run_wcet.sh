# Run standard WCET tests and compare them to measurements

if [ -z "${LOGDIR}" ] ; then
    echo "LOGDIR not set. Exit." >&2
    exit 1
fi

function make_sim()
{
    KEY=$1
    EXPC=$2
    shift 2
    LOG="${LOGDIR}/${P3}.log"

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
    LOG="${LOGDIR}/${P3}_${KEY}.log"

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

# Show help
echo ''
echo '# Regression Tests for WCET analysis on JOP'
echo '# Formats:'
echo '# moet_${METHOD} ${ACTUAL} ${EXPECTED} ${DIFF}'
echo '# wcet_${METHOD} ${ACTUAL} ${EXPECTED} ${DIFF} ${RATIO}'
echo ''
echo '# DIFF ... difference between ACTUAL and EXPECTED'
echo '# RATIO ... the error ratio of the WCET relative to the MOET'
echo ''

# Start Lift
export P1=test;export P2=wcet;export P3=StartLift
echo "[Lift] LOG=${LOGDIR}/${P3}*.log" >&2
make_sim jsim 5007
make_java
make_wcet cs-0-dfa-no 7621 CALLSTRING_LENGTH=0 USE_DFA=no
make_wcet cs-0-dfa-yes 7621 CALLSTRING_LENGTH=0 USE_DFA=yes
make_wcet cs-1-dfa-yes 7621 CALLSTRING_LENGTH=1 USE_DFA=yes


# Start Kfl
export P1=test;export P2=wcet;export P3=StartKfl
echo "[test] LOG=${LOGDIR}/${P3}*.log" >&2
make_sim jsim 10040
make_java
make_wcet cs-0-dfa-no 37407 CALLSTRING_LENGTH=0 USE_DFA=no
make_wcet cs-0-dfa-yes 22269 CALLSTRING_LENGTH=0 USE_DFA=yes
make_wcet cs-1-dfa-yes 20299 CALLSTRING_LENGTH=1 USE_DFA=yes


# Microbenchmarks
# SuperGraph1
export P1=test;export P2=wcet/devel;export P3=SuperGraph1
echo "[test] LOG=${LOGDIR}/${P3}*.log" >&2
make_sim jsim 2079873
make_java
make_wcet cs-0-dfa-yes 67370795 CALLSTRING_LENGTH=0 USE_DFA=yes
make_wcet cs-1-dfa-yes  2524044 CALLSTRING_LENGTH=1 USE_DFA=yes
make_wcet cs-2-dfa-yes  2079945 CALLSTRING_LENGTH=2 USE_DFA=yes

# Load on Return
export P1=test;export P2=wcet/devel;export P3=LoadOnReturn
echo "[test] LOG=${LOGDIR}/${P3}*.log" >&2
make_sim jsim 5000
make_java
make_wcet cs-0-dfa-yes 5000 CALLSTRING_LENGTH=0 USE_DFA=yes
make_wcet cs-1-dfa-yes 5000 CALLSTRING_LENGTH=1 USE_DFA=yes

