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

//import java.util.Arrays;

import papabench.core.commons.conf.FBWMode;
import papabench.core.commons.conf.RadioConf;
import papabench.core.commons.data.RadioCommands;

/**
 * Radio commands
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class RadioCommandsImpl implements RadioCommands {
	
	private boolean containsAveragedChannels = false;
	
	private int[] channels = new int[RadioConf.RADIO_CTL_NB];
	
	public RadioCommandsImpl() {	
	}
	
	/**
	 * Copy constructor.
	 */
	protected RadioCommandsImpl(RadioCommandsImpl radioCommands) {
		for (int i = 0 ; i < channels.length; i++) {
			// @WCA loop = papabench.core.commons.conf.RadioConf.RADIO_CTL_NB
			this.channels[i] = radioCommands.channels[i];			
		}
		this.containsAveragedChannels = radioCommands.containsAveragedChannels;
	}
	
	public boolean containsAveragedChannels() {		
		return containsAveragedChannels;
	}

	public int getCalib() {		
		return channels[RADIO_CALIB];
	}

	public int[] getChannels() {
		return channels;
	}

	public int getGain1() {
		return channels[RADIO_GAIN1];
	}

	public int getGain2() {
		return channels[RADIO_GAIN2];
	}

	public int getLLS() {
		return channels[RADIO_LLS];
	}

	public FBWMode getMode() {
		return FBWMode.valueOf(channels[RADIO_MODE]);
	}

	public int getPitch() {
		return channels[RADIO_PITCH];
	}

	public int getRoll() {
		return channels[RADIO_ROLL];
	}

	public int getThrottle() {
		return channels[RADIO_THROTTLE];
	}

	public int getYaw() {
		return channels[RADIO_YAW];
	}

	public void setCalib(int value) {
		channels[RADIO_CALIB] = value;		
	}

	public void setGain1(int value) {
		channels[RADIO_GAIN1] = value;
	}

	public void setGain2(int value) {
		channels[RADIO_GAIN2] = value;
	}

	public void setLLS(int value) {
		channels[RADIO_LLS] = value;	
	}

	public void setMode(FBWMode mode) {
		channels[RADIO_MODE] = mode.getValue();		
	}

	public void setPitch(int value) {
		channels[RADIO_PITCH] = value;
	}

	public void setRoll(int value) {
		channels[RADIO_ROLL] = value;
	}

	public void setThrottle(int value) {
		channels[RADIO_THROTTLE] = value;
	}

	public void setYaw(int value) {
		channels[RADIO_YAW] = value;
	}
	
	public RadioCommands clone() {		
		return new RadioCommandsImpl(this);
	}
	
	public void fillFrom(RadioCommands radioCommands) {
//		assert(this.channels.length == radioCommands.getChannels().length);
		
		int[] channels = radioCommands.getChannels();
		for (int i = 0; i < channels.length; i++) {
			// @WCA loop = papabench.core.commons.conf.RadioConf.RADIO_CTL_NB
			this.channels[i] = channels[i];									
		}
		
		this.containsAveragedChannels = radioCommands.containsAveragedChannels(); 
	}
	
	@Override
	public String toString() {		
		return "RadioCommands: " + "missing Arrays"; // Arrays.toString(this.channels);
	}
}
