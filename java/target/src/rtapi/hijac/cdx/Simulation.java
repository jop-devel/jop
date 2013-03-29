/**
 * This file is a simplified version of ImmortalEntry in the original CDx.
 *
 * @authors Kun Wei, Frank Zeyda
 */
package hijac.cdx;

import hijac.cdx.Constants;
import hijac.cdx.FrameBuffer;

/**
 * Class to generated simulated radar frames and maintain simulation data.
 */
public class Simulation {
  /* Encapsulates hardware access for reading radar frames. */
  public FrameBuffer frameBuffer;

  /* Records the number of processed frames. */
  public int framesProcessed = 0;

  /* Records the number of dropped frames. */
  public int droppedFrames = 0;

  /* Records the state of the detector of being ready to process the next
   * radar frame. */
  public boolean detectorReady = false;

  public Simulation() {
    System.out.println(
      "Simulation: detector priority is " + Constants.DETECTOR_PRIORITY);

    System.out.println(
      "Simulation: detector period is " + Constants.DETECTOR_PERIOD);

    frameBuffer = new FrameBuffer();
  }
}
