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
package papabench.jop;

//import java.util.Timer;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;

import joprt.RtThread;
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
import papabench.jop.commons.tasks.PJPeriodicTask;

/**
 * JOP based implementation of PapaBench.
 * 
 * It uses {@link ScheduledExecutorService} to execute periodic tasks.
 * 
 * It can be reimplemented with help of {@link Timer} class.
 * 
 * @author Michal Malohlava
 *
 */
public class PapaBenchJopImpl implements JopPapaBench {
	
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
	
//	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(TOTAL_TASKS_COUNT);

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
		System.out.println("init");
		// Allocate and initialize global objects: 
		//  - MC0 - autopilot
		autopilotModule = PapaBenchJopFactory.createAutopilotModule(this);
				
		//  - MC1 - FBW
		fbwModule = PapaBenchJopFactory.createFBWModule();		
		
		// Create & configure SPIBusChannel and connect both PapaBench modules
		SPIBusChannel spiBusChannel = PapaBenchJopFactory.createSPIBusChannel();
		spiBusChannel.init();
		autopilotModule.setSPIBus(spiBusChannel.getMasterEnd()); // = MC0: SPI master mode
		fbwModule.setSPIBus(spiBusChannel.getSlaveEnd()); // = MC1: SPI slave mode
		
		// setup flight plan
		// no assert in JOP
		// assert(this.flightPlan != null);
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
		FlightModel flightModel = PapaBenchJopFactory.createSimulator();
		flightModel.init();
		
		// Register simulator tasks
		createSimulatorTasks(flightModel, autopilotModule, fbwModule);
	}
	
	protected void createAutopilotTasks(AutopilotModule autopilotModule) {
		autopilotTasks = new PJPeriodicTask[AUTOPILOT_TASKS_COUNT];
		autopilotTasks[0] = new PJPeriodicTask(new AltitudeControlTaskHandler(autopilotModule), AltitudeControlTaskConf.PRIORITY, AltitudeControlTaskConf.RELEASE_MS, AltitudeControlTaskConf.PERIOD_MS, AltitudeControlTaskConf.NAME);
		autopilotTasks[1] = new PJPeriodicTask(new ClimbControlTaskHandler(autopilotModule), ClimbControlTaskConf.PRIORITY, ClimbControlTaskConf.RELEASE_MS, ClimbControlTaskConf.PERIOD_MS, ClimbControlTaskConf.NAME);
		autopilotTasks[2] = new PJPeriodicTask(new LinkFBWSendTaskHandler(autopilotModule), LinkFBWSendTaskConf.PRIORITY, LinkFBWSendTaskConf.RELEASE_MS, LinkFBWSendTaskConf.PERIOD_MS, LinkFBWSendTaskConf.NAME);

		autopilotTasks[3] = new PJPeriodicTask(new NavigationTaskHandler(autopilotModule), NavigationTaskConf.PRIORITY, NavigationTaskConf.RELEASE_MS, NavigationTaskConf.PERIOD_MS, NavigationTaskConf.NAME);
		autopilotTasks[3].setScope(1024);

		autopilotTasks[4] = new PJPeriodicTask(new RadioControlTaskHandler(autopilotModule), RadioControlTaskConf.PRIORITY, RadioControlTaskConf.RELEASE_MS, RadioControlTaskConf.PERIOD_MS, RadioControlTaskConf.NAME);
		autopilotTasks[4].setScope(128);

		autopilotTasks[5] = new PJPeriodicTask(new ReportingTaskHandler(autopilotModule), ReportingTaskConf.PRIORITY, ReportingTaskConf.RELEASE_MS, ReportingTaskConf.PERIOD_MS, ReportingTaskConf.NAME);
		
		// StabilizationTask allocates messages which are sent to FBW unit -> allocate them in scope memory
		autopilotTasks[6] = new PJPeriodicTask(new StabilizationTaskHandler(autopilotModule), StabilizationTaskConf.PRIORITY, StabilizationTaskConf.RELEASE_MS, StabilizationTaskConf.PERIOD_MS, StabilizationTaskConf.NAME);
		autopilotTasks[6].setScope(128);
	}
	
	protected void createFBWTasks(FBWModule fbwModule) {
		fbwTasks = new PJPeriodicTask[FBW_TASKS_COUNT];
		fbwTasks[0] = new PJPeriodicTask(new CheckFailsafeTaskHandler(fbwModule), CheckFailsafeTaskConf.PRIORITY, CheckFailsafeTaskConf.RELEASE_MS, CheckFailsafeTaskConf.PERIOD_MS, CheckFailsafeTaskConf.NAME);

		fbwTasks[1] = new PJPeriodicTask(new CheckMega128ValuesTaskHandler(fbwModule), CheckMega128ValuesTaskConf.PRIORITY, CheckMega128ValuesTaskConf.RELEASE_MS, CheckMega128ValuesTaskConf.PERIOD_MS, CheckMega128ValuesTaskConf.NAME);
		fbwTasks[1].setScope(128);

		fbwTasks[2] = new PJPeriodicTask(new SendDataToAutopilotTaskHandler(fbwModule), SendDataToAutopilotTaskConf.PRIORITY, SendDataToAutopilotTaskConf.RELEASE_MS, SendDataToAutopilotTaskConf.PERIOD_MS, SendDataToAutopilotTaskConf.NAME);
		fbwTasks[2].setScope(128);

		fbwTasks[3] = new PJPeriodicTask(new TestPPMTaskHandler(fbwModule), TestPPMTaskConf.PRIORITY, TestPPMTaskConf.RELEASE_MS, TestPPMTaskConf.PERIOD_MS, TestPPMTaskConf.NAME);
	}
	
	protected void createSimulatorTasks(FlightModel flightModel, AutopilotModule autopilotModule, FBWModule fbwModule) {
		simulatorTasks = new PJPeriodicTask[SIMULATOR_TASKS_COUNT];

		simulatorTasks[0] = new PJPeriodicTask(new SimulatorFlightModelTaskHandler(flightModel,autopilotModule,fbwModule), SimulatorFlightModelTaskConf.PRIORITY, SimulatorFlightModelTaskConf.RELEASE_MS, SimulatorFlightModelTaskConf.PERIOD_MS, SimulatorFlightModelTaskConf.NAME);
		simulatorTasks[0].setScope(128);

		simulatorTasks[1] = new PJPeriodicTask(new SimulatorGPSTaskHandler(flightModel,autopilotModule), SimulatorGPSTaskConf.PRIORITY, SimulatorGPSTaskConf.RELEASE_MS, SimulatorGPSTaskConf.PERIOD_MS, SimulatorGPSTaskConf.NAME);
		simulatorTasks[1].setScope(128);

		simulatorTasks[2] = new PJPeriodicTask(new SimulatorIRTaskHandler(flightModel,autopilotModule), SimulatorIRTaskConf.PRIORITY, SimulatorIRTaskConf.RELEASE_MS, SimulatorIRTaskConf.PERIOD_MS, SimulatorIRTaskConf.NAME);
		simulatorTasks[2].setScope(128);
	}

	public void start() {
		System.out.println("start");
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
		RtThread.startMission();
	}
	
	protected void start(PJPeriodicTask pjPeriodicTask) {
		// this is a noop on JOP
	}

	@Override
	public void shutdown() {
		// there is no shutdown in a JOP RT thread system
		// System.out.println("Shutdown request");
//		if (executorService!=null && !executorService.isShutdown()) {			
//			executorService.shutdown();			
//		} else {
//			throw new IllegalStateException("Executor service cannot be shutdown!");
//		}
	}
}
