/* $Id$
 * 
 * This file is a part of jPapaBench providing a Java implementation 
 * of PapaBench project.
 * Copyright (C) 2010  Michal Malohlava <michal.malohlava_at_d3s.mff.cuni.cz>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */
package papabench.pj;

import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import papabench.core.autopilot.conf.PapaBenchAutopilotConf.AltitudeControlTaskConf;
import papabench.core.autopilot.conf.PapaBenchAutopilotConf.ClimbControlTaskConf;
import papabench.core.autopilot.conf.PapaBenchAutopilotConf.LinkFBWSendTaskConf;
import papabench.core.autopilot.conf.PapaBenchAutopilotConf.NavigationTaskConf;
import papabench.core.autopilot.conf.PapaBenchAutopilotConf.RadioControlTaskConf;
import papabench.core.autopilot.conf.PapaBenchAutopilotConf.ReportingTaskConf;
import papabench.core.autopilot.conf.PapaBenchAutopilotConf.StabilizationTaskConf;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.tasks.handlers.AltitudeControlTaskHandler;
import papabench.core.autopilot.tasks.handlers.ClimbControlTaskHandler;
import papabench.core.autopilot.tasks.handlers.LinkFBWSendTaskHandler;
import papabench.core.autopilot.tasks.handlers.NavigationTaskHandler;
import papabench.core.autopilot.tasks.handlers.RadioControlTaskHandler;
import papabench.core.autopilot.tasks.handlers.ReportingTaskHandler;
import papabench.core.autopilot.tasks.handlers.StabilizationTaskHandler;
import papabench.core.bus.SPIBusChannel;
import papabench.core.commons.data.FlightPlan;
import papabench.core.fbw.conf.PapaBenchFBWConf.CheckFailsafeTaskConf;
import papabench.core.fbw.conf.PapaBenchFBWConf.CheckMega128ValuesTaskConf;
import papabench.core.fbw.conf.PapaBenchFBWConf.SendDataToAutopilotTaskConf;
import papabench.core.fbw.conf.PapaBenchFBWConf.TestPPMTaskConf;
import papabench.core.fbw.modules.FBWModule;
import papabench.core.fbw.tasks.handlers.CheckFailsafeTaskHandler;
import papabench.core.fbw.tasks.handlers.CheckMega128ValuesTaskHandler;
import papabench.core.fbw.tasks.handlers.SendDataToAutopilotTaskHandler;
import papabench.core.fbw.tasks.handlers.TestPPMTaskHandler;
import papabench.core.simulator.conf.PapaBenchSimulatorConf.SimulatorFlightModelTaskConf;
import papabench.core.simulator.conf.PapaBenchSimulatorConf.SimulatorGPSTaskConf;
import papabench.core.simulator.conf.PapaBenchSimulatorConf.SimulatorIRTaskConf;
import papabench.core.simulator.model.FlightModel;
import papabench.core.simulator.tasks.handlers.SimulatorFlightModelTaskHandler;
import papabench.core.simulator.tasks.handlers.SimulatorGPSTaskHandler;
import papabench.core.simulator.tasks.handlers.SimulatorIRTaskHandler;
import papabench.pj.commons.tasks.PJPeriodicTask;

/**
 * Plain Java-based implementation of PapaBench.
 * 
 * It uses {@link ScheduledExecutorService} to execute periodic tasks.
 * 
 * It can be reimplemented with help of {@link Timer} class.
 * 
 * @author Michal Malohlava
 *
 */
public class PapaBenchPJImpl implements PJPapaBench {
	
	private AutopilotModule autopilotModule;
	private FBWModule fbwModule;	
	private FlightPlan flightPlan;
	
	// TODO move this to PapaBench core
	private static final int AUTOPILOT_TASKS_COUNT = 7;
	private static final int FBW_TASKS_COUNT = 4;
	private static final int SIMULATOR_TASKS_COUNT = 3;
	private static final int TOTAL_TASKS_COUNT = AUTOPILOT_TASKS_COUNT + FBW_TASKS_COUNT + SIMULATOR_TASKS_COUNT;
	
	private PJPeriodicTask[] autopilotTasks;
	private PJPeriodicTask[] fbwTasks;
	private PJPeriodicTask[] simulatorTasks;
	
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(TOTAL_TASKS_COUNT);

	public AutopilotModule getAutopilotModule() {
		return autopilotModule;
	}

	public FBWModule getFBWModule() {
		return fbwModule;
	}

	public void setFlightPlan(FlightPlan flightPlan) {
		this.flightPlan = flightPlan;		
	}

	public void init() {	
		// Allocate and initialize global objects: 
		//  - MC0 - autopilot
		autopilotModule = PapaBenchPJFactory.createAutopilotModule(this);
				
		//  - MC1 - FBW
		fbwModule = PapaBenchPJFactory.createFBWModule();		
		
		// Create & configure SPIBusChannel and connect both PapaBench modules
		SPIBusChannel spiBusChannel = PapaBenchPJFactory.createSPIBusChannel();
		spiBusChannel.init();
		autopilotModule.setSPIBus(spiBusChannel.getMasterEnd()); // = MC0: SPI master mode
		fbwModule.setSPIBus(spiBusChannel.getSlaveEnd()); // = MC1: SPI slave mode
		
		// setup flight plan
		assert(this.flightPlan != null);
		autopilotModule.getNavigator().setFlightPlan(this.flightPlan);
		
		// initialize both modules - if the modules are badly initialized the runtime exception is thrown
		autopilotModule.init();
		fbwModule.init();
		
		// Register interrupt handlers
		/*
		 * TODO
		 */
		
		// Register period threads
		createAutopilotTasks(autopilotModule);
		createFBWTasks(fbwModule);
		
		// Create a flight simulator
		FlightModel flightModel = PapaBenchPJFactory.createSimulator();
		flightModel.init();
		
		// Register simulator tasks
		createSimulatorTasks(flightModel, autopilotModule, fbwModule);
	}
	
	protected void createAutopilotTasks(AutopilotModule autopilotModule) {
		autopilotTasks = new PJPeriodicTask[AUTOPILOT_TASKS_COUNT];
		autopilotTasks[0] = new PJPeriodicTask(new AltitudeControlTaskHandler(autopilotModule), AltitudeControlTaskConf.PRIORITY, AltitudeControlTaskConf.RELEASE_MS, AltitudeControlTaskConf.PERIOD_MS);
		autopilotTasks[1] = new PJPeriodicTask(new ClimbControlTaskHandler(autopilotModule), ClimbControlTaskConf.PRIORITY, ClimbControlTaskConf.RELEASE_MS, ClimbControlTaskConf.PERIOD_MS);
		autopilotTasks[2] = new PJPeriodicTask(new LinkFBWSendTaskHandler(autopilotModule), LinkFBWSendTaskConf.PRIORITY, LinkFBWSendTaskConf.RELEASE_MS, LinkFBWSendTaskConf.PERIOD_MS);
		autopilotTasks[3] = new PJPeriodicTask(new NavigationTaskHandler(autopilotModule), NavigationTaskConf.PRIORITY, NavigationTaskConf.RELEASE_MS, NavigationTaskConf.PERIOD_MS);
		autopilotTasks[4] = new PJPeriodicTask(new RadioControlTaskHandler(autopilotModule), RadioControlTaskConf.PRIORITY, RadioControlTaskConf.RELEASE_MS, RadioControlTaskConf.PERIOD_MS);
		autopilotTasks[5] = new PJPeriodicTask(new ReportingTaskHandler(autopilotModule), ReportingTaskConf.PRIORITY, ReportingTaskConf.RELEASE_MS, ReportingTaskConf.PERIOD_MS);
		
		// StabilizationTask allocates messages which are sent to FBW unit -> allocate them in scope memory
		autopilotTasks[6] = new PJPeriodicTask(new StabilizationTaskHandler(autopilotModule), StabilizationTaskConf.PRIORITY, StabilizationTaskConf.RELEASE_MS, StabilizationTaskConf.PERIOD_MS);		
	}
	
	protected void createFBWTasks(FBWModule fbwModule) {
		fbwTasks = new PJPeriodicTask[FBW_TASKS_COUNT];
		fbwTasks[0] = new PJPeriodicTask(new CheckFailsafeTaskHandler(fbwModule), CheckFailsafeTaskConf.PRIORITY, CheckFailsafeTaskConf.RELEASE_MS, CheckFailsafeTaskConf.PERIOD_MS);
		fbwTasks[1] = new PJPeriodicTask(new CheckMega128ValuesTaskHandler(fbwModule), CheckMega128ValuesTaskConf.PRIORITY, CheckMega128ValuesTaskConf.RELEASE_MS, CheckMega128ValuesTaskConf.PERIOD_MS);
		fbwTasks[2] = new PJPeriodicTask(new SendDataToAutopilotTaskHandler(fbwModule), SendDataToAutopilotTaskConf.PRIORITY, SendDataToAutopilotTaskConf.RELEASE_MS, SendDataToAutopilotTaskConf.PERIOD_MS);
		fbwTasks[3] = new PJPeriodicTask(new TestPPMTaskHandler(fbwModule), TestPPMTaskConf.PRIORITY, TestPPMTaskConf.RELEASE_MS, TestPPMTaskConf.PERIOD_MS);
	}
	
	protected void createSimulatorTasks(FlightModel flightModel, AutopilotModule autopilotModule, FBWModule fbwModule) {
		simulatorTasks = new PJPeriodicTask[SIMULATOR_TASKS_COUNT];
		simulatorTasks[0] = new PJPeriodicTask(new SimulatorFlightModelTaskHandler(flightModel,autopilotModule,fbwModule), SimulatorFlightModelTaskConf.PRIORITY, SimulatorFlightModelTaskConf.RELEASE_MS, SimulatorFlightModelTaskConf.PERIOD_MS);
		simulatorTasks[1] = new PJPeriodicTask(new SimulatorGPSTaskHandler(flightModel,autopilotModule), SimulatorGPSTaskConf.PRIORITY, SimulatorGPSTaskConf.RELEASE_MS, SimulatorGPSTaskConf.PERIOD_MS);
		simulatorTasks[2] = new PJPeriodicTask(new SimulatorIRTaskHandler(flightModel,autopilotModule), SimulatorIRTaskConf.PRIORITY, SimulatorIRTaskConf.RELEASE_MS, SimulatorIRTaskConf.PERIOD_MS);		
	}

	public void start() {
		// FIXME put here rendez-vous for all tasks		
		for (int i = 0; i < SIMULATOR_TASKS_COUNT; i++) {
			start(simulatorTasks[i]);
			
		}
		for (int i = 0; i < FBW_TASKS_COUNT; i++) {
			start(fbwTasks[i]);
		}		
		for (int i = 0; i < AUTOPILOT_TASKS_COUNT; i++) {
			start(autopilotTasks[i]);
		}		
	}
	
	protected void start(PJPeriodicTask pjPeriodicTask) {
		executorService.scheduleAtFixedRate(pjPeriodicTask.getTaskHandler(), pjPeriodicTask.getReleaseMs(), pjPeriodicTask.getPeriodMs(), TimeUnit.MILLISECONDS);		
	}

	@Override
	public void shutdown() {
		if (executorService!=null && !executorService.isShutdown()) {			
			executorService.shutdown();			
		} else {
			throw new IllegalStateException("Executor service cannot be shutdown!");
		}
	}
}
