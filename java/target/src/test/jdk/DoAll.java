/*
 * Created on 30.07.2005
 *
 */
package jdk;



import java.util.*;
import jvm.TestCase;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class DoAll {

	public static void main(String[] args) {
		
		TestCase tc[] = {
				new HashCode(),
				new TestVector(),
				new BArrayInputStream(),
				new BArrayOutputStream(),
				new DInputStream(),
				new DOutputStream(),
				new PrimitiveClasses(),
				new PrimitiveClasses2(),
				new PrimitiveClasses3()
		};
				
		for (int i=0; i<tc.length; ++i) {
			System.out.print(tc[i].getName());
			if (tc[i].test()) {
				System.out.println(" ok");
			} else {
				System.out.println(" failed!");
			}
		}
		
	}
}
