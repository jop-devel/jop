/**
 * @author Frank Zeyda
 */
package scjlibs.examples.hijac.cdx;

//import javax.safetycritical.AperiodicEvent;
import javax.safetycritical.AperiodicEventHandler;

/**
 * This class is used to control the execution of OutputCollisionsHandler.
 * In particular, it ensures the release of this handler once all instances
 * of DetectorHandler have finished their computational work.
 */
public class DetectorControl {
  private final boolean[] idle;
//  private final AperiodicEvent output;
  private final AperiodicEventHandler outputHandler;

//  public DetectorControl(AperiodicEvent event, int n) {
	public DetectorControl(AperiodicEventHandler eventHandler, int n) {

	  idle = new boolean[n];
//    output = event;
	  outputHandler = eventHandler;
  }

  public synchronized void start() {
    for (int index = 0; index < idle.length; index++) {
      idle[index] = false;
    }
  }

  public synchronized void notify(int id) {
    idle[id - 1] = true;
    if (done()) {
        /* Release handler to output the detected collisions. */
//        output.fire();
        outputHandler.release();
    }
  }

	protected synchronized boolean done() {
		boolean result = true;
		for (int index = 0; index < idle.length; index++) {
			if (!idle[index]) {
				result = false;
			}
		}
		return result;
	}
}
