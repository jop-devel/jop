/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Martin Schoeberl (martin@jopdesign.com)

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

package javax.safetycritical.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/*
 This file is part of JOP, the Java Optimized Processor
 see <http://www.jopdesign.com/>

 Copyright (C) 2011, Martin Schoeberl (martin@jopdesign.com)

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

import javax.safetycritical.annotate.SCJAllowed;

@SCJAllowed
public class SimplePrintStream extends OutputStream {
	
	PrintStream ps;
	/**
	 * Create a filter class.
	 * However, we know that our console: OutputStream is
	 * already a PrintStream. Therefore we can just delegate the work.
	 * @param stream
	 */
	@SCJAllowed
	public SimplePrintStream(OutputStream stream) {
		ps = (PrintStream) stream;
	}

	@SCJAllowed
	public boolean checkError() {
		return false;
	}

	/**
	 * do we have those protected methods? Check j.l.PrintStream
	 */
	@SCJAllowed
	protected void setError() {
	}

	@SCJAllowed
	protected void clearError() {
	}

	@SCJAllowed
	public synchronized void print(CharSequence sequence) {
		ps.print(sequence);
	}

	@SCJAllowed
	public void println() {
		ps.println();
	}

	@SCJAllowed
	public void println(CharSequence sequence) {
		ps.println(sequence);
	}

	/**
	 * Do we need this?
	 */
	@Override
	public void write(int b) throws IOException {
		ps.write(b);
	}
}
