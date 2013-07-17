# O$ evaluation
# what to run
RUN_JBE=yes
RUN_PAPA=yes
RUN_DEBIE=yes

# Options
TIMING_OPTS="--jop.jop-rws 1 --jop.jop-wws 2"
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
for N in old 0 4 8 16 64 1024; do
  if [ "$N" == "old" ]; then
    OPTS="${TIMING_OPTS} --jop.jop-object-cache false"
  else
    OPTS="${TIMING_OPTS} --jop.jop-object-cache true --jop.jop-ocache-associativity ${N}"
  fi
  R=$(make P1=$1 P2=$2 P3=$3 wcet \
    WCET_METHOD=$4 USE_DFA=yes CALLSTRING_LENGTH=${CALLSTRING_LENGTH} ${MAKE_OPTS} \
    WCET_OPTIONS="${OPTS} ${WCET_OPTS}" 2>&1 | \
    tee /dev/stderr | grep '^wcet:') 
  echo "$1.$2.$3.$4 ; ${N} ; $(echo ${R} | cut -d' ' -f 2) ; ${R} ;"
done  
}

MAKE_OPTS="USE_SCOPES=true"

if [ "${RUN_JBE}" == yes ] ; then
CALLSTRING_LENGTH=1
# lift
run_eval test wcet StartLift measure

# updip
run_eval test wcet StartBenchUdpIp measure

# ejip (without TCP)
patch -p1 < scripts/ejip.patch 
run_eval test wcet StartEjipCmp measure
git checkout java/target/src/common/ejip/Ejip.java
fi

# papabench
if [ "${RUN_PAPA}" == yes ] ; then
CALLSTRING_LENGTH=0
for f in \
papabench.core.fbw.tasks.handlers.CheckMega128ValuesTaskHandler.run \
papabench.core.autopilot.tasks.handlers.NavigationTaskHandler.courseComputation \
papabench.core.autopilot.tasks.handlers.NavigationTaskHandler.update \
papabench.core.autopilot.tasks.handlers.RadioControlTaskHandler.run \
papabench.core.autopilot.tasks.handlers.NavigationTaskHandler.run \
papabench.core.autopilot.tasks.handlers.AltitudeControlTaskHandler.run \
papabench.core.autopilot.tasks.handlers.ClimbControlTaskHandler.run \
papabench.core.autopilot.tasks.handlers.StabilizationTaskHandler.run \
papabench.core.autopilot.tasks.handlers.LinkFBWSendTaskHandler.run \
papabench.core.autopilot.tasks.handlers.ReportingTaskHandler.run \
papabench.core.fbw.tasks.handlers.SendDataToAutopilotTaskHandler.run \
papabench.core.fbw.tasks.handlers.TestPPMTaskHandler.run \
papabench.core.fbw.tasks.handlers.CheckFailsafeTaskHandler.run \
#papabench.core.simulator.tasks.handlers.SimulatorFlightModelTaskHandler.run \
#papabench.core.simulator.tasks.handlers.SimulatorGPSTaskHandler.run \
#papabench.core.simulator.tasks.handlers.SimulatorIRTaskHandler.run
do
	run_eval bench papabench/jop PapaBenchJopApplication $f
done
fi

# debie
if [ "${RUN_DEBIE}" == yes ] ; then
CALLSTRING_LENGTH=0
TARGET_METHODS="debie.telecommand.TelecommandExecutionTask.tcInterruptService
                debie.telecommand.TelecommandExecutionTask.tmInterruptService 
                debie.particles.AcquisitionTask.handleHitTrigger
                debie.telecommand.TelecommandExecutionTask.handleTelecommand 
                debie.particles.AcquisitionTask.handleAcquisition"
#                debie.health.HealthMonitoringTask.handleHealthMonitor
for M in $TARGET_METHODS ; do
  run_eval bench debie/harness Harness $M
done
fi