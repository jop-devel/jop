# Needs a lot (1.2G) of memory for DFA analysis
#
TARGET_METHODS="debie.telecommand.TelecommandExecutionTask.tcInterruptService
                debie.telecommand.TelecommandExecutionTask.tmInterruptService 
                debie.particles.AcquisitionTask.handleHitTrigger
                debie.telecommand.TelecommandExecutionTask.handleTelecommand 
                debie.particles.AcquisitionTask.handleAcquisition
                debie.health.HealthMonitoringTask.handleHealthMonitor"

if [ -z "${LOG_DIR}" ] ; then
	echo "Error: \${LOG_DIR} not set" >&2
	exit 1
fi

make java_app P1=bench P2=debie/harness P3=Harness 2>&1 | tee ${LOG_DIR}/debie.build.log
rm -f ${LOG_DIR}/debie.wca.log
for t in ${TARGET_METHODS} ; do
	echo "* Method: ${t}" | tee -a ${LOG_DIR}/debie.wca.log
	make wcet P1=bench P2=debie/harness P3=Harness WCET_METHOD="${t}" USE_DFA=yes \
		WCET_OPTIONS="--dfa-cache-dir java/target/wcet/dfa-cache" 2>&1 | tee -a ${LOG_DIR}/debie.wca.log
done
grep -e '* Method: ' -e 'wcet:' -e 'wcet.time:' -e 'wcet.always-miss:' -e 'wcet.min-cache-cost' ${LOG_DIR}/debie.wca.log > ${LOG_DIR}/debie.results 
grep -e '* Method: ' -e 'ERROR \[' ${LOG_DIR}/debie.wca.log > ${LOG_DIR}/debie.errors 
