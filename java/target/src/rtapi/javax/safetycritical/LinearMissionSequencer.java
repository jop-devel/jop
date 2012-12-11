/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2011, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package javax.safetycritical;

import javax.realtime.PriorityParameters;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import static javax.safetycritical.annotate.Level.SUPPORT;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;

/**
 * A LinearMissionSequencer is a MissionSequencer that serves the needs of a
 * common design pattern in which the sequence of Mission executions is known
 * prior to execution and all missions can be preallocated within an
 * outer-nested memory area. The parameter <SpecificMission> allows application
 * code to differentiate between LinearMissionSequencers that are designed for
 * use in Level 0 vs. other environments. For example, a
 * LinearMissionSequencer<CyclicExecutive> is known to only run missions that
 * are suitable for execution within a Level 0 run-time environment.
 * 
 * 
 * @param <SpecificMission>
 */
@SCJAllowed
public class LinearMissionSequencer<SpecificMission extends Mission> extends
		MissionSequencer<SpecificMission> {

	Mission single;
	Mission[] missions_;
	boolean repeat_;
	String name_;

	boolean served = false;

	int mission_id = 0;
	private Mission mission;

	/**
	 * Construct a LinearMissionSequencer object to oversee execution of the
	 * single mission m.
	 * 
	 * Throws IllegalArgumentException if any of the arguments equals null.
	 * 
	 * @param priority
	 *            The priority at which the MissionSequencer’s bound thread
	 *            executes.
	 * @param storage
	 *            The memory resources to be dedicated to execution of this
	 *            Mission Sequencer’s bound thread.
	 * @param repeat
	 *            When repeat is true, the specified mission shall be repeated
	 *            indefinitely.
	 * @param m
	 *            The single mission that runs under the oversight of this
	 *            LinearMissionSequencer.
	 */

	@SCJAllowed
	@SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
	public LinearMissionSequencer(PriorityParameters priority,
			StorageParameters storage, boolean repeat, SpecificMission m)
			throws IllegalArgumentException {
		this(priority, storage, repeat, m, "");
	}

	/**
	 * Construct a LinearMissionSequencer object to oversee execution of the
	 * single mission m.
	 * 
	 * @param priority
	 *            The priority at which the MissionSequencer’s bound thread
	 *            executes.
	 * @param storage
	 *            The memory resources to be dedicated to execution of this
	 *            Mission Sequencer’s bound thread.
	 * @param repeat
	 *            When repeat is true, the specified mission shall be repeated
	 *            indefinitely.
	 * @param m
	 *            The single mission that runs under the oversight of this
	 *            LinearMissionSequencer.
	 * @param name
	 *            The name by which this LinearMissionSequencer will be
	 *            identified in traces for use in debug or in toString.
	 */
	@SCJAllowed
	@SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
	public LinearMissionSequencer(PriorityParameters priority,
			StorageParameters storage, boolean repeat, SpecificMission m,
			String name) throws IllegalArgumentException {
		super(priority, storage, name);

		if ((priority == null) | (storage == null) | (m == null)) {
			throw new IllegalArgumentException();
		}

		single = m;
		repeat_ = repeat;
		name_ = name;
	}

	/**
	 * Construct a LinearMissionSequencer object to oversee execution of the
	 * sequence of missions represented by the missions parameter. The
	 * LinearMission- Sequencer runs the sequence of missions identified in its
	 * missions array exactly once, from low to high index position within the
	 * array. The constructor allocates a copy of its missions array argument
	 * within the current scope.
	 * 
	 * @param priority
	 *            The priority at which the MissionSequencer’s bound thread
	 *            executes.
	 * @param storage
	 *            The memory resources to be dedicated to execution of this
	 *            MissionSequencer’s bound thread.
	 * @param missions
	 *            An array representing the sequence of missions to be executed
	 *            under the oversight of this LinearMissionSequencer. It is
	 *            required that the elements of the missions array reside in a
	 *            scope that encloses the scope of this. The missions array
	 *            itself may reside in a more inner-nested temporary scope.
	 * @param repeat
	 *            When repeat is true, the specified list of missions shall be
	 *            repeated indefinitely.
	 */
	@SCJAllowed
	@SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
	public LinearMissionSequencer(PriorityParameters priority,
			StorageParameters storage, SpecificMission[] missions,
			boolean repeat) throws IllegalArgumentException {
		this(priority, storage, missions, repeat, "");
	}

	/**
	 * Construct a LinearMissionSequencer object to oversee execution of the
	 * sequence of missions represented by the missions parameter. The
	 * LinearMission- Sequencer runs the sequence of missions identified in its
	 * missions array exactly once, from low to high index position within the
	 * array. The constructor allocates a copy of its missions array argument
	 * within the current scope.
	 * 
	 * @param priority
	 *            The priority at which the MissionSequencer’s bound thread
	 *            executes.
	 * @param storage
	 *            The memory resources to be dedicated to execution of this
	 *            MissionSequencer’s bound thread.
	 * @param missions
	 *            An array representing the sequence of missions to be executed
	 *            under the oversight of this LinearMissionSequencer. It is
	 *            required that the elements of the missions array reside in a
	 *            scope that encloses the scope of this. The missions array
	 *            itself may reside in a more inner-nested temporary scope.
	 * @param repeat
	 *            When repeat is true, the specified list of missions shall be
	 *            repeated indefinitely.
	 * @name The name by which this LinearMissionSequencer will be identified in
	 *       traces for use in debug or in toString.
	 */
	@SCJAllowed
	@SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
	public LinearMissionSequencer(PriorityParameters priority,
			StorageParameters storage, SpecificMission[] missions,
			boolean repeat, String name) throws IllegalArgumentException {

		super(priority, storage, name);

		if ((priority == null) | (storage == null) | (missions == null)) {
			throw new IllegalArgumentException();
		}

		missions_ = new Mission[missions.length];
		System.arraycopy(missions, 0, missions_, 0, missions.length);
		repeat_ = repeat;
		name_ = name;
	}

	@SCJAllowed(SUPPORT)
	@SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
	@Override
	protected SpecificMission getNextMission() {

		// For an array of missions
		if (missions_ != null) {
			if (mission_id < missions_.length) {
				mission = missions_[mission_id];
				mission_id++;
				if ((repeat_) & (mission_id >= missions_.length)) {
					mission_id = 0;
				}
			} else {
				// No more missions, termination request??
				mission = null;
				requestSequenceTermination();
			}

			// For a single mission
		} else {
			if (repeat_) {
				mission = single;
			} else {
				if (!served) {
					mission = single;
					served = true;
				} else {
					mission = null;
					requestSequenceTermination();
				}
			}
		}

		return (SpecificMission) mission;
	}
}
