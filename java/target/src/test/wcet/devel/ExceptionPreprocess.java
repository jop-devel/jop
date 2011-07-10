/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2011, Martin Schoeberl (martin@jopdesign.com)

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

package wcet.devel;

public class ExceptionPreprocess {
	
	public static void main(String [] args) {
		ExceptionPreprocess e = new ExceptionPreprocess();
		try {
			e.throwA();
		} catch (Exception exc) {
			exc.printStackTrace();
			System.out.println("FAIL");
			return;
		}
		System.out.println("PASS");
	}

	static Exception throw_exc;

	private void throwA() throws Exception {
		throw_exc = new Exception("ok");
		try {
			throwB();
		} catch (Exception exc) {
			if (exc != throw_exc)
				throw exc;
		}
	}

	private synchronized void throwB() throws Exception {
		throw throw_exc;
	}

}
