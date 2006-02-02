package gctest;

import util.Dbg;
import com.jopdesign.sys.*;

// A test of the GCStackWalker
// One must insert a log statement the last line in GC.push
// log("Adding to grey list", ref); 
// If the root scan is enableb it should not add the "ref"
// If it is disables for conservative scanning it should add it it

public class GCTest9 {

  public static void main(String s[]) {
	  System.out.println("GC");
	  int i = 53800; // this value depends on the handle area
	  GC.gc();
	} //main
}
