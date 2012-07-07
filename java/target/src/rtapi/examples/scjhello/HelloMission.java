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

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.Mission;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.io.SimplePrintStream;

public class HelloMission extends Mission {

	@Override
	protected void initialize() {

		OutputStream os = null;
		try {
			os = Connector.openOutputStream("console:");
		} catch (IOException e) {
			throw new Error("No console available");
		}

		HelloHandler hh = new HelloHandler(new SimplePrintStream(os));
		hh.register();
	}

	@Override
	public long missionMemorySize() {
		return 100000;
	}

}
