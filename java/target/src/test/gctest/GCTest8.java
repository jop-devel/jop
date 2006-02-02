package gctest;

import util.Dbg;
import com.jopdesign.sys.*;

// A test of the GCStackWalker
// TODO: Why does the led not turn red. JOP writes "Stack overflow" to the serial line.

public class GCTest8 {

  public static void main(String s[]) {
	  
    int c = 0;
		for(int i=0;i<1000;i++){
			c++;
			//GC.gc(); // This line makes it work
			System.out.println("no crash.no crash.no crash.no crash.no crash.no crash.no crash.no crash.no crash:"+c);
		}
		System.out.println("Test 8 ok");
	} //main
}
