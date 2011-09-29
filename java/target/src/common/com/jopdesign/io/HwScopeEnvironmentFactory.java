package com.jopdesign.io;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.JVMHelp;
import com.jopdesign.sys.Native;

public class HwScopeEnvironmentFactory extends IOFactory {

		private HWSensorA sensA;
		private HWSensorB sensB;

		// Handles should be the first static fields!
		private static int SENSA_PTR;
		private static int SENSA_MTAB;
		
		private static int SENSB_PTR;
		private static int SENSB_MTAB;


		HwScopeEnvironmentFactory() {
			sensA = (HWSensorA) makeHWObject(new HWSensorA(),Const.SENSA_BASE, 0);
			sensB = (HWSensorB) makeHWObject(new HWSensorB(),Const.SENSB_BASE, 1);

		};
		// that has to be overridden by each sub class to get
		// the correct cp
		private static Object makeHWObject(Object o, int address, int idx) {
			int cp = Native.rdIntMem(Const.RAM_CP);
			return JVMHelp.makeHWObject(o, address, idx, cp);
		}
		
		static HwScopeEnvironmentFactory single = new HwScopeEnvironmentFactory();
		
		public static HwScopeEnvironmentFactory getEnvironmentFactory() {		
			return single;
		}

		public HWSensorA getSensA() { return sensA; }
		public HWSensorB getSensB() { return sensB; }

	}
