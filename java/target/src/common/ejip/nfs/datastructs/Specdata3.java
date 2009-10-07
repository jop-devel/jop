/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Daniel Reichhard (daniel.reichhard@gmail.com)

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
package ejip.nfs.datastructs;

import ejip.nfs.Xdr;

/**
 * describes a device file
 */
public class Specdata3 {
	/**
	 * usually major device number
	 */
	public int specdata1;
	/**
	 * usually minor device number
	 */
	public int specdata2;

	public String toString() {
		return "SpecData1:\t" + specdata1 + "\n" + 
			"SpecData2:\t" + specdata2;
	}
	
	public void loadFields(StringBuffer sb) {
		specdata1 = Xdr.getNextInt(sb);
		specdata2 = Xdr.getNextInt(sb);
	}
	
	public void appendToStringBuffer(StringBuffer sb) {
		Xdr.append(sb, specdata1);
		Xdr.append(sb, specdata2);
	}
}
