/*
 * Created on 06.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package iface;

/**
 * @author admin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestIf implements IFA, IFB {

	/* (non-Javadoc)
	 * @see iface.IFA#vf()
	 */
	public void vf() {
		System.out.println("TestIf vf");

	}

	/* (non-Javadoc)
	 * @see iface.IFA#ifunc()
	 */
	public int ifunc() {
		// TODO Auto-generated method stub
		return 1;
	}

	/* (non-Javadoc)
	 * @see iface.IFB#vfb()
	 */
	public void vfb() {
		System.out.println("TestIf vfb");
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see iface.IFB#sfb()
	 */
	public String sfb() {
		// TODO Auto-generated method stub
		return "Hello";
	}

	public static void main(String[] args) {
		
		IFA oa = new ClIfA();
		IFA ota = new TestIf();
		IFB ob = new TestIf();
	
		int i = 123;
		System.out.println("oa:");
		int j = 124;
		System.out.println(oa.ifunc());
		oa.vf();
		System.out.println("ota:");
		System.out.println(ota.ifunc());
		ota.vf();
		System.out.println("ob:");
		System.out.println(ob.sfb());
		ob.vfb();
		
		
	}
}
