/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2012, Martin Schoeberl (martin@jopdesign.com)

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

package examples.scjhello;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.Mission;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.io.SimplePrintStream;

public class HelloHandler extends PeriodicEventHandler {

	SimplePrintStream out;
	int cnt;
	
	public HelloHandler(SimplePrintStream sps) {
		super(new PriorityParameters(11), new PeriodicParameters(
			new RelativeTime(0, 0), new RelativeTime(500, 0)),
			new StorageParameters(10000, null), 500);
		out = sps;
	}

	@Override
	public void handleAsyncEvent() {
		out.println("Ping " + cnt);
		++cnt;
	}

}
