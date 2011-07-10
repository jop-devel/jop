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
package papabench.core.commons.data.impl;

import papabench.core.autopilot.data.Position3D;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.modules.AutopilotStatus;
import papabench.core.autopilot.modules.Estimator;
import papabench.core.autopilot.modules.Navigator;
import papabench.core.commons.data.FlightPlan;
import papabench.core.commons.data.UTMPosition;
import papabench.core.utils.LogUtils;

/**
 * Abstract flight plan.
 * 
 * The concrete flight plan should be automatically generated from fligh-plan description and should inherit from
 * this class.
 * 
 * @author Michal Malohlava
 *
 */
public abstract class AbstractFlightPlan implements FlightPlan {
	
	/* injected dependencies */
	protected Estimator estimator;
	protected AutopilotStatus status;
	protected Navigator navigator;
	
	protected AutopilotModule autopilotModule;
	
	/* navigation flight plan */
	private NavigationBlock[] navigationBlocks;
	private Position3D[] waypoints;
	
	private NavigationBlock currentBlock = null;	
	private int lastWPNumber = 0;
	
	private int currentNumberOfBlocks = 0;
	private int currentNumberOfWaypoints = 0;
	
	public final void init() {
		if (estimator == null || status == null || navigator == null || autopilotModule == null) {
			throw new IllegalArgumentException("FlightPlan has wrong configuration!");
		}
		// let the user to setup navigation points
		this.waypoints = new Position3D[getNumberOfWaypoints()];
		initWaypoints();
		
		// allocated desired number of blocks
		this.navigationBlocks = new NavigationBlock[getNumberOfNavBlocks()];
		// let the user to setup navigation blocks and stages
		initNavigationBlocks();
		
		this.currentBlock = navigationBlocks[getStartNavBlockNumber()];
		this.currentBlock.init();
	}
	
	
	public void setEstimator(Estimator estimator) { this.estimator = estimator; }
	public void setAutopilotStatus(AutopilotStatus status) {this.status = status; }
	public void setNavigator(Navigator navigator) {this.navigator = navigator; }
	public void setAutopilotModule(AutopilotModule autopilotModule) { this.autopilotModule = autopilotModule; }
	
	/**
	 * Initialize waypoints.
	 * 
	 *  User should use method {@link #addWaypoint(Position3D)} to fill an internal array of waypoints.
	 */
	protected abstract void initWaypoints();
	
	/**
	 * Initialize navigation blocks.
	 * 
	 * User should use method {@link #addNavBlock(NavigationBlock)} to add navigation blocks. 
	 */
	protected abstract void initNavigationBlocks();
			
	protected Position3D WP(int number) {
		return this.waypoints[number];
	}
	
	protected float WPALT(int number) {
		return WP(number).z;
	}
	
	protected int getLastWPNumber() {
		return this.lastWPNumber;
	}
	
	/**
	 * Returns number of initial navigation block. 
	 * 
	 * Default is the first navigation block.
	 * 
	 * @return number of initial navigation block
	 */
	protected int getStartNavBlockNumber() { return 0; }
		
	protected abstract int getNumberOfNavBlocks();
	protected abstract int getNumberOfWaypoints();
	
	protected abstract UTMPosition getCenterUTMPosition();
	
	public final void execute() {		
		
		if (currentBlock != null) {
			currentBlock.execute();			
		}		
	}
	
	private void nextBlock() {
		int num = this.currentBlock.getBlockNumber();
		if (++num < navigationBlocks.length) {
			this.currentBlock = this.navigationBlocks[num];
			this.currentBlock.init();
		} else {
			// FIXME mission finished -> what to do?
			// we call mission finished
			missionFinished();
		}
	}
	
	private void gotoBlock(int i) {
		// assert 0 =< o < navigationBlocks.length
		this.currentBlock = this.navigationBlocks[i];
		this.currentBlock.init();
	}
	
	private void setLastWPNumber(int wp) {		 
		this.lastWPNumber = wp;
	}
	
	private void missionFinished() {
		//LogUtils.log(this, "Flightplan finished - mission termination requested");
		
		this.currentBlock = null;
		this.autopilotModule.missionFinished();		
	}
	
	
	/**
	 * Add navigation block.
	 * 
	 * IMPORTANT: Order of blocks define number of 
	 * @param block
	 * 
	 * @return the reference to the parameter block
	 */
	protected final NavigationBlock addNavBlock(NavigationBlock block) {
		navigationBlocks[currentNumberOfBlocks] = block;
		block.setFlightPlan(this);
		block.setBlockNumber(currentNumberOfBlocks);
		currentNumberOfBlocks++;
		
		return block;
	}
	
	protected final void addWaypoint(Position3D point) {
		this.waypoints[currentNumberOfWaypoints++] = point;
	}
	
	
	/**
	 * Simple implementation of navigation block.
	 * 
	 * The user should instantiate this class and overide block logic method.
	 * 
	 * @author Michal Malohlava
	 *
	 */
	public class NavigationBlock {
		private int blockNumber;		
		private AbstractFlightPlan flightPlan;
		
		private NavigationStage[] navigationStages;		
		private int currentNumberOfStages = 0;
		
		private NavigationStage currentStage = null;
		
		public NavigationBlock(int numberOfStages) {
			this.navigationStages = new NavigationStage[numberOfStages];
		}
		
		protected final void execute() {			
			preCall();
			
			//LogUtils.log(this, "Block " + blockNumber + " Stage " + this.currentStage.stageNumber + " executed");
			this.currentStage.execute();
			
//			if (!skipPostCall) {
//				postCall();
//				skipPostCall = false;
//			}
		}
		
		protected void init() {
			this.currentStage = this.navigationStages[0];
			this.currentStage.init();
		}
		
		/**
		 * This method is call before the stage code is executed.
		 * 
		 * User can override this method.
		 */
		protected void preCall() {}
		/**
		 * This method is call after execution stage code (stage can skip this code).
		 * 
		 * User can override this method.
		 */
		protected void postCall() {}
		
		/**
		 * Add new stage into this block.
		 * 
		 * @param stage stage of this block
		 * 
		 * @return reference to this block to allow chaining.
		 */
		public final NavigationBlock addNavStage(NavigationStage stage) {
			this.navigationStages[currentNumberOfStages] = stage;
			stage.setNavigationBlock(this);
			stage.setStageNumber(currentNumberOfStages);
			currentNumberOfStages++;
			
			return this;
		}
		
		/**
		 * Called by flight plan to setup a navigation block number.
		 * Block number depends on addition of navigation block into the flight plan.
		 *  
		 * @param number navigation block number
		 */
		private void setBlockNumber(int number) {
			this.blockNumber = number;
		}
		
		private int getBlockNumber() {
			return this.blockNumber;
		}
		
		private void setFlightPlan(AbstractFlightPlan flightPlan) {
			this.flightPlan = flightPlan;
		}
		
		/**
		 * Called by the current stage to go to next stage.
		 */
		private void nextStage() {
			int num = this.currentStage.getStageNumber();
			if (++num < this.navigationStages.length) {				
				this.currentStage = this.navigationStages[num];
				this.currentStage.init();
			} else {
				nextBlock();
			}
		}
		
		private void nextStageFrom(int wp) {
			this.flightPlan.setLastWPNumber(wp);
			nextStage();
		}
		
		private void gotoStage(int stage) {
			this.currentStage = this.navigationStages[stage];
			this.currentStage.init();
		}
		
		protected final void nextBlock() {
			this.flightPlan.nextBlock();
		}
		
		protected final void gotoBlock(int i) {
			this.flightPlan.gotoBlock(i);						
		}
	}
	
	/**
	 * Simple implementation of block navigation stage.
	 * 
	 * The user should instantiate this class with own logic.
	 * 
	 * @author Michal Malohlava
	 *
	 */
	public abstract class NavigationStage extends NavigatorCommands {
		private int stageNumber;
		
		private NavigationBlock block;
		private Position3D lastPosition = new Position3D(0, 0, 0);
		
		void setNavigationBlock(NavigationBlock block) {
			this.block = block;			
		}
				
		protected abstract void execute();
		
		protected void init() {
			lastPosition.x = estimator.getPosition().x;
			lastPosition.y = estimator.getPosition().y;
			lastPosition.z = estimator.getPosition().z;			
		}
		
		protected final void nextStage() {
			block.nextStage();
		}
		
		protected final void nextStageFrom(int wp) {
			block.nextStageFrom(wp);
		}
		
		private void setStageNumber(int number) {
			this.stageNumber = number;
		}
		
		protected int getStageNumber() {
			return this.stageNumber;
		}
		
		protected final void gotoStage(int stage) {
			block.gotoStage(stage);
		}
		
		protected final void missionFinished() {
			AbstractFlightPlan.this.missionFinished();
		}
		
		@Override
		protected Estimator estimator() { return estimator;	}
		@Override
		protected Navigator navigator() { return navigator; }
		@Override
		protected AutopilotStatus status() {return status; }
		@Override
		protected Position3D WP(int n) {return AbstractFlightPlan.this.WP(n); }
		@Override
		protected int getLastWPNumber() {			
			return AbstractFlightPlan.this.getLastWPNumber();
		}
		@Override
		protected final Position3D getLastPosition() {
			return this.lastPosition;			
		}
	}
}
