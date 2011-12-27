package com.jopdesign.io;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.JVMHelp;
import com.jopdesign.sys.Native;

public class HwScopeEnvironmentFactory extends IOFactory {

		private HWSensorM sensM;
		private HWSensorC sensC;

		// Handles should be the first static fields!
		private static int SENS_M_PTR;
		private static int SENS_M_MTAB;
		
		private static int SENS_C_PTR;
		private static int SENS_C_MTAB;


		HwScopeEnvironmentFactory() {
			sensM = (HWSensorM) makeHWObject(new HWSensorM(),Const.SENS_M_BASE, 0);
			sensC = (HWSensorC) makeHWObject(new HWSensorC(),Const.SENS_C_BASE, 1);

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

		public HWSensorM getSensM() { return sensM; }
		public HWSensorC getSensC() { return sensC; }

	}
