/*
 * Created on 30.07.2005
 *
 */
package jvm;

import jvm.math.Float;
import jvm.math.Imul;
import jvm.math.Long;
import jvm.obj.Basic;
import jvm.obj.Basic2;
import jvm.obj.Clinit;
import jvm.obj.Static;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class DoAll {

	public static void main(String[] args) {
		
		TestCase tc[] = {
				new Basic(),
				new Basic2(),
				new Static(),
				new Array(),
				new Clinit(),
//				new Float(),
				new Imul(),
				new Long(),
				new MultiArray(),
				new Switch(),
//				new Except(),
		};
		
		for (int i=0; i<tc.length; ++i) {
			System.out.print(tc[i].getName());
			if (tc[i].test()) {
				System.out.println(" ok");
			} else {
				System.out.println(" failed!");
			}
		}
		
//		jbe.DoAll.main(args);
	}
}
