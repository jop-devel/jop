package gctest;

import util.Dbg;
import com.jopdesign.sys.*;

// A test of the GCStackWalker
// TODO: It crashes

public class GCTest5 {

  public static void main(String s[]) {
	  
    int c = 0;
		for(;;){
			c++;
			System.out.println("crash.crash.crash.crash.crash.crash.crash.crash.crash:"+c);
		}
	} //main
}
