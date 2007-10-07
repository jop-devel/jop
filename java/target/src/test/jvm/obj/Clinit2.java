
package jvm.obj;

import jvm.TestCase;

/**
 * Test <clinit> order
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class Clinit2 extends TestCase {

	
	static int abc = 123;
	static int invsta = 0;
	static int indir = 0;

	static {
		abc = 456;
		// the X static initializer runns later
		// This issue is not solvable with a predefined <clinit> order!
		// we do not allow to pass in JOPizer
		// new Clinit2_X();
		
		// <clinit> Clinit2_invsta should run before
		invsta = Clinit2_invstat.getS();
		
		// <clinit> Clinit2_indir2 should run before
		// results in cyclic dependency when
		// static var indir is changed there
		// new Clinit2_indir();
		// Clinit2_indir.foo();
		
		indir += 10;
	}
	
	public String getName() {
		return "Clinit2";
	}

	public boolean test() {		

		boolean ok = true;
		
		// we cannot resolve this issue!
		// if (abc!=789) ok = false;
		
		// but that should be possible
		if (Clinit2_X.result_y!=2) ok = false;
		if (Clinit2_X.result_z!=4) ok = false;
		if (Clinit2_invstat.getS()!=4711) ok = false;
		if (invsta!=4711) ok = false;
		if (indir!=20) ok = false;
		
		return ok;

	}
	
	public static void main(String[] args) {
		
		Clinit2 clinit = new Clinit2();
		// force our transitive hull generater to put
		// those <clinit> in the wrong order
		// ... not really possible as it is a HashSet
		new Clinit2_X();
		
		System.out.print(clinit.getName());
		if (clinit.test()) {
			System.out.println(" ok");
		} else {
			System.out.println(" failed!");
		}
	}
}

class Clinit2_X {
	
	static int result_y = 0;
	static int result_z = 0;
	
	static {
		Clinit2.abc = 789;
		// Y <clinit> should run before
		result_y = Clinit2_Y.y;
		// Z <clinit> should run before
		new Clinit2_Z();
		result_z = Clinit2_Z.z;
	}
}

class Clinit2_Y {
	
	static int y = 1;
	static {
		y = 2;
	}
}

class Clinit2_Z {

	static int z = 3;
	static {
		z = 4;
	}

}

class Clinit2_invstat {
	
	static int s = 0;
	static {
		s = 4711;
	}
	static int getS() {
		return s;
	}
}

class Clinit2_indir {
	
	static void foo() {
		Clinit2_indir2.foo();		
	}
	
	Clinit2_indir() {
		Clinit2_indir2.foo();
	}
}

class Clinit2_indir2 {
	
	static int xxx = 0;
	static void foo() {};
	static {
		xxx = 333;
		Clinit2.indir = 10;
	}
}