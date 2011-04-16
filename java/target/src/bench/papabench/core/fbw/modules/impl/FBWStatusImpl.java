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
package papabench.core.fbw.modules.impl;

import papabench.core.commons.conf.FBWMode;
import papabench.core.fbw.modules.FBWStatus;

/**
 * Fly-by-wire status information.
 *  
 * @author Michal Malohlava
 *
 */
public class FBWStatusImpl implements FBWStatus {
	
	protected FBWMode fbwMode;
	protected boolean isMega128OK = true;
	protected boolean isRadioOK = true;
	protected boolean isRadioReallyLost = false;

	public FBWMode getFBWMode() {
		return this.fbwMode;
	}

	public boolean isMega128OK() {
		return this.isMega128OK;
	}

	public boolean isRadioOK() {
		return this.isRadioOK;
	}

	public boolean isRadioReallyLost() {
		return this.isRadioReallyLost;
	}

	public void setFBWMode(FBWMode mode) {
		this.fbwMode = mode;		
	}

	public void setMega128OK(boolean value) {
		this.isMega128OK = value;
	}

	public void setRadioOK(boolean value) {
		this.isRadioOK = value;
	}

	public void setRadioReallyLost(boolean value) {
		this.isRadioReallyLost = value;
	}

}
