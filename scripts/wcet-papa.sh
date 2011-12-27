#! /bin/sh

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
	make jop_config wcet -e P1=bench P2=papabench/jop P3=PapaBenchJopApplication USE_SCOPES=true MEASURE=false WCET_METHOD=$f
done

# ant -Dp1=bench -Dp2=papabench/jop -Dp3=PapaBenchJopApplication -DUSE_SCOPES=true -DMEASURE=true jop-config japp > papa.log
