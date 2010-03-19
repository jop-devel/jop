/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>
    
  Copyright (C) 2010, Thomas Hassler, Lukas Marx

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


/**
 * @author Thomas Hassler	e0425918@student.tuwien.ac.at
 * @author Lukas Marx	lukas.marx@gmail.com
 * @version 1.0
 */

package ttpa.protocol;

/** 
 * Rosefile
 * active section describes which of the sections is active
 * section 2 and 3 each contain a sequence of rounds (rodls) that are executed
 */
public class Rosefile 
{
	
	/** active section of the rose file */
	private RoseSect activeSection;
	
	/** section 2 of the rose file */
	private RoseSect section2;
	
	/** section 3 of the rose file */
	private RoseSect section3;
	
	
	/**
	 * @param myActiveSection active section
	 * @param mySection2 section 2
	 * @param mySection3 section 3
	 */
	public Rosefile(RoseSect myActiveSection, RoseSect mySection2, RoseSect mySection3) 
	{
		this.activeSection = myActiveSection;
		this.section2 = mySection2;
		this.section3 = mySection3;
	}

	/**
	 * @return active_section of the rosefile
	 */
	public RoseSect getActiveSection() 
	{
		return activeSection;
	}
	
	/**
	 * @return section2 of the rosefile
	 */
	public RoseSect getSection2() 
	{
		return section2;
	}

	/**
	 * @return section3 of the rosefile
	 */
	public RoseSect getSection3() 
	{
		return section3;
	}
}
