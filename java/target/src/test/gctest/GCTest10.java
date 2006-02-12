package gctest;

import util.Dbg;
import util.Timer;
import joprt.RtThread;
import com.jopdesign.sys.*;

// A timing test of the GCStackWalker

public class GCTest10 {

  static Object mutex;

  public static void main(String s[]) {
    System.out.println("GC Example 10");

    // Results in us:
    // threadcnt  | conservative | exact
    // (nt+1)     | block  total | block totol
    // 1 (nt=0)   |                  
    // 10 (nt=9)  |               
    // 100 (nt=99)|         

    // Number of extra threads in addition to main thread:
    int nt = 9;

    for (int i = 0; i < nt; i++) {
      System.out.print("Extra thread:");
      System.out.println(i);
      new RtThread(20 + i, 2000000 + i) {
        public void run() {
          waitForNextPeriod();
        }
      };
    }

    RtThread.startMission();
    int ts;
    synchronized (mutex) {
      ts = Timer.us();
      GC.gc();
      ts = Timer.us() - ts;
    }
    System.out.print("Total GC took ");
    System.out.print(ts);
    System.out.println(" us");
    System.exit(0);

  } // main
}
