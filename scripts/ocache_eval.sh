# O$ evaluation
TIMING_OPTS="--jop.jop-rws 2 --jop.jop-wws 3"
TIMING_OPTS="--jop.jop-ocache-words-per-line 16 ${TIMING_OPTS}"
TIMING_OPTS="--jop.jop-ocache-fill 1 ${TIMING_OPTS}"
TIMING_OPTS="--jop.jop-ocache-hit-cycles 5 ${TIMING_OPTS}"
TIMING_OPTS="--jop.jop-ocache-load-field-cycles 10 ${TIMING_OPTS}"
TIMING_OPTS="--jop.jop-ocache-load-block-cycles 10 ${TIMING_OPTS}"
# varying:
# jop.jop-object-cache false|true
# jop.jop-ocache-associativity no|0|4|8|16|64

# run evaluation for one benchmark
# TIMING_OPTS ... Options for the WCET tool configuring the timing
# MAKE_OPTS   ... additional options for make
# WCET_OPTS   ... additional options for the WCET analysis
function run_eval() {
make P1=$1 P2=$2 P3=$3 ${MAKE_OPTS} java_app >&2
for N in no 0 4 8 16 64; do
  if [ "$N" == "no" ]; then
    OPTS="${TIMING_OPTS} --jop.jop-object-cache false"
  else
    OPTS="${TIMING_OPTS} --jop.jop-object-cache true --jop.jop-ocache-associativity ${N}"
  fi
  R=$(make P1=$1 P2=$2 P3=$3 wcet \
    WCET_METHOD=$4 USE_DFA=yes CALLSTRING_LENGTH=1 ${MAKE_OPTS} \
    WCET_OPTIONS="${OPTS} ${WCET_OPTS}" 2>&1 | \
    tee /dev/stderr | grep '^wcet:') 
  echo "$1.$2.$3 ; ${N} ; $(echo ${R} | cut -d' ' -f 2) ; ${R} ;"
done  
}

# lift
run_eval test wcet StartLift measure
# updip
MAKE_OPTS="USE_SCOPES=true"
run_eval test wcet StartBenchUdpIp measure
# ejip (without TCP)
patch -p1 < scripts/ejip.patch 
run_eval test wcet StartEjipCmp measure
git checkout java/target/src/common/ejip/Ejip.java
