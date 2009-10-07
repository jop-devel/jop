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

package jvm;

public class Except extends TestCase {
	
	public String toString() {
		return "Except";
	}
	
	public boolean test() {

		boolean ok = true;
		
		ok = ok && throw1();
		ok = ok && throw2();
		ok = ok && throw3();
		ok = ok && throw4();
		ok = ok && throw5();
		ok = ok && throw6();
		ok = ok && throw7();
		ok = ok && throw8();
		ok = ok && throw9();

		return ok;
	}

	// check basic throw
	private boolean throw1() {
		boolean retval = false;
		Exception e = new Exception("ok");
		try {
			throw e;
		} catch (Exception exc) {
			retval = exc.getMessage().equals("ok");
		}
		return retval;
	}

	// check throw with cast
	private boolean throw2() {
		boolean retval = false;
		Exception e = new Exception("ok");
		try {
			throw e;
		} catch (Throwable exc) {
			retval = exc.getMessage().equals("ok");
		}
		return retval;
	}

	// check 1-level recursive throw
	private boolean throw3() {
		boolean retval = false;
		try {
			throw3a();
		} catch (Exception exc) {
			retval = exc.getMessage().equals("ok");
		}
		return retval;
	}

	private void throw3a() throws Exception {
		throw new Exception("ok");
	}

	// check 2-level recursive throw
	private boolean throw4() {
		boolean retval = false;
		try {
			throw4a();
		} catch (Exception exc) {
			retval = exc.getMessage().equals("ok");
		}
		return retval;
	}

	private void throw4a() throws Exception {
		throw4b();
	}

	private void throw4b() throws Exception {
		throw new Exception("ok");
	}

	// check 2-level recursive throw with cast
	private boolean throw5() {
		boolean retval = false;
		try {
			throw5a();
		} catch (Throwable exc) {
			retval = exc.getMessage().equals("ok");
		}
		return retval;
	}

	private void throw5a() throws Exception {
		throw5b();
	}

	private void throw5b() throws Exception {
		throw new Exception("ok");
	}

	// check finally
	private boolean throw6() {
		boolean retval = false;
		try {
		} catch (Exception exc) {
		} finally {
			retval = true;
		}
		return retval;
	}

	// check finally
	private boolean throw7() {
		boolean retval = false;
		try {
			throw new Exception("ok");
		} catch (Exception exc) {
		} finally {
			retval = true;
		}
		return retval;
	}

	// check deep finally
	static boolean throw8_retval;

	private boolean throw8() {
		throw8_retval = false;
		try {
			throw8a();
		} catch (Exception exc) {
			throw8_retval &= exc.getMessage().equals("ok");
		}
		return throw8_retval;
	}

	private void throw8a() throws Exception {
		try {
			throw new Exception("ok");
		} finally {
			throw8_retval = true;
		}
	}

	// check synchronization
	static Exception throw9_exc;

	private boolean throw9() {
		boolean retval = false;
		throw9_exc = new Exception("ok");
		for (int i = 0; i < 100; i++) {
			try {
				throw9a();
			} catch (Exception exc) {
				retval |= !exc.getMessage().equals("ok");
			}
		}
		return !retval;
	}

	private void throw9a() throws Exception {
		throw9b();
	}

	private synchronized void throw9b() throws Exception {
		throw9c();
	}

	private void throw9c() throws Exception {
		throw9d();
	}

	private synchronized void throw9d() throws Exception {
		throw9e();
	}

	private void throw9e() throws Exception {
		throw9f();
	}

	private synchronized void throw9f() throws Exception {
		throw9g();
	}

	private void throw9g() throws Exception {
		throw9h();
	}

	private synchronized void throw9h() throws Exception {
		throw throw9_exc;
	}
	
}
