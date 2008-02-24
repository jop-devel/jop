/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

/*
 * Created on 06.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package iface;

/**
 * @author admin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestIf implements IFA, IFB {

	/* (non-Javadoc)
	 * @see iface.IFA#vf()
	 */
	public void vf() {
		System.out.println("TestIf vf");

	}

	/* (non-Javadoc)
	 * @see iface.IFA#ifunc()
	 */
	public int ifunc() {
		// TODO Auto-generated method stub
		return 1;
	}

	/* (non-Javadoc)
	 * @see iface.IFB#vfb()
	 */
	public void vfb() {
		System.out.println("TestIf vfb");
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see iface.IFB#sfb()
	 */
	public String sfb() {
		// TODO Auto-generated method stub
		return "Hello";
	}

	public static void main(String[] args) {
		
		IFA oa = new ClIfA();
		IFA ota = new TestIf();
		IFB ob = new TestIf();
	
		int i = 123;
		System.out.println("oa:");
		int j = 124;
		System.out.println(oa.ifunc());
		oa.vf();
		System.out.println("ota:");
		System.out.println(ota.ifunc());
		ota.vf();
		System.out.println("ob:");
		System.out.println(ob.sfb());
		ob.vfb();
		
		
	}
}
