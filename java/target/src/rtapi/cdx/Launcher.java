/**
 *  This file is part of miniCDx benchmark of oSCJ.
 *
 *   miniCDx is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   miniCDx is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with miniCDx.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *   Copyright 2009, 2010 
 *   @authors  Daniel Tang, Ales Plsek
 *
 *   See: http://sss.cs.purdue.edu/projects/oscj/
 */
package cdx;
import javax.safetycritical.CyclicExecutive;
import javax.safetycritical.JopSystem;
import javax.safetycritical.Safelet;

import cdx.cdx.Level0Safelet;

// ----- NOTE ----- Set the following values ----- NOTE -----
//
//	IM_SIZE to 20000 in Memory.java
//	STACK_SIZE = 512 in Const.java
//	RAM_LEN = 512 in Jopa.java
//	
public class Launcher {
    public static void main(final String[] args) {
    	
        Safelet<CyclicExecutive> safelet = new Level0Safelet();

        JopSystem js = new JopSystem();

        js.startCycle(safelet);
        
    }
}
