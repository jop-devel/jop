package testrt;
import util.*;

public class A {

	public static void main(String[] args) {

		Timer.init(20000000, 5);	// just for the watch dog or some usleep
		Dbg.initSer();				// use serial line for debug output

		int t = Timer.cnt();
		t = Timer.cnt()-t;
		Dbg.intVal(t);
		int off = t;

		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		t = Timer.cnt();
		f(true);
		t = Timer.cnt()-t;
		Dbg.intVal(t-off);


		for (;;) {
			t = Timer.cnt();
			for (int i=0; i<386000; ++i) ;
			Timer.wd();
			Dbg.wr('*');
			t = Timer.cnt()-t;
			Dbg.intVal(t-off);
		}
	}

	static void f(boolean ret) {
if (ret) return;
	}
}
