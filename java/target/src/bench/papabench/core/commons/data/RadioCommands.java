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
package papabench.core.commons.data;

import papabench.core.commons.conf.FBWMode;
import papabench.core.commons.conf.RadioConf;

/**
 * Radio commands which are specific for given radio.
 * 
 * @see RadioConf
 *
 * TODO this should be generated according to airplane configuration
 * - radio mc3030
 * 
 * @author Michal Malohlava
 *
 */
public interface RadioCommands {
	
	public static final byte RADIO_THROTTLE = 0;
	public static final byte RADIO_ROLL 	= 1;
	public static final byte RADIO_PITCH 	= 2;
	public static final byte RADIO_YAW 		= 3;
	public static final byte RADIO_MODE 	= 4;
	public static final byte RADIO_GAIN1 	= 5;
	public static final byte RADIO_GAIN2 	= 6;
	public static final byte RADIO_LLS   	= 7;
	public static final byte RADIO_CALIB 	= 8;
	
	int[] getChannels();
	
	int getThrottle();	
	int getRoll();
	int getPitch();
	int getYaw();
	FBWMode getMode();
	int getGain1();
	int getGain2();
	int getLLS();
	int getCalib();
	
	void setThrottle(int value);	
	void setRoll(int value);
	void setPitch(int value);
	void setYaw(int value);
	void setMode(FBWMode mode);
	void setGain1(int value);
	void setGain2(int value);
	void setLLS(int value);
	void setCalib(int value);
	
	boolean containsAveragedChannels();
	
	public RadioCommands clone();
	
	public void fillFrom(RadioCommands radioCommands);
}
