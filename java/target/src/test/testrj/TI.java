/*
 * Created on 22.10.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package testrj;

import util.*;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TI implements Ia, Ib {

	public static int stat_field;

	public int obj_field;

	public void b() {
		Dbg.wr('b');
	}

	public void a() {
		Dbg.wr('a');
	}

	public static void aa(Ia x) {
		x.a();
	}
	public static void bb(Ib x) {
		x.b();
	}
	public static void ic_c(Ic x) {
		x.c();
	}
	public static void ic_b(Ic x) {
		x.b();
	}
	public static void ic_x(Ic x) {
		x.x('!');
	}
	
	static class T2 implements Ic, Ia {
		public void c() {
			Dbg.wr('C');
		}
		public void b() {
			Dbg.wr('p');
		}
		public void x(int i) {
			Dbg.wr(i);
		}
		public void a() {
			Dbg.wr('A');
		}

	}

	public static void main(String[] args) {
		util.Dbg.initSer();

		TI t = new TI();
		t.a();
		t.b();
		aa(t);
		bb(t);
		T2 t2 = new T2();
		t2.a();
		t2.c();
		aa(t2);
		ic_c(t2);
		ic_b(t2);
		ic_x(t2);

		for (;;);
	}
}
