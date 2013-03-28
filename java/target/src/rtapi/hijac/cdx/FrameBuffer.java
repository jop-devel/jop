/**
 *  This file is part of miniCDx benchmark of oSCJ.
 *
 *   miniCDx is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   miniCDx is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with miniCDx.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *   Copyright 2009, 2010
 *   @authors  Daniel Tang, Ales Plsek, Frank Zeyda, Kun Wai
 *
 *   See: http://sss.cs.purdue.edu/projects/oscj/
 */
package hijac.cdx;

import hijac.cdx.Constants;
import hijac.cdx.RawFrame;

/**
 * This class models interaction with the hardware layer that reads radar
 * frames from an external device.
 */
public class FrameBuffer {
  protected RawFrame buffer;
  protected byte[] callsigns;
  protected float time;

  public FrameBuffer() {
    buffer = new RawFrame();
    callsigns =
      new byte[Constants.NUMBER_OF_PLANES * Constants.LENGTH_OF_CALLSIGN];
    time = 0.0f;
  }

  /**
   * Generate a new radar frame.
   */
  public void readFrame() {
    // Generate callsigns using ASCII codes.
    for (byte k = 0; k < Constants.NUMBER_OF_PLANES; k++) {
      callsigns[Constants.LENGTH_OF_CALLSIGN * k] = 112;
      callsigns[Constants.LENGTH_OF_CALLSIGN * k + 1] = 108;
      callsigns[Constants.LENGTH_OF_CALLSIGN * k + 2] = 97;
      callsigns[Constants.LENGTH_OF_CALLSIGN * k + 3] = 110;
      callsigns[Constants.LENGTH_OF_CALLSIGN * k + 4] = 101;
      callsigns[Constants.LENGTH_OF_CALLSIGN * k + 5] = (byte) (49 + k);
    }

    /* Calculate new positions of aircrafts. */
    float positions[] = new float[Constants.NUMBER_OF_PLANES * 3];

    for (int k = 0; k < Constants.NUMBER_OF_PLANES / 2; k++) {
      positions[3 * k] = (float) (100 * Math.cos(time) + 500 + 50 * k);
      positions[3 * k + 1] = 100.0f;
      positions[3 * k + 2] = 5.0f;
      positions[Constants.NUMBER_OF_PLANES / 2 * 3 + 3 * k]
        = (float) (100 * Math.sin(time) + 500 + 50 * k);
      positions[Constants.NUMBER_OF_PLANES / 2 * 3 + 3 * k + 1] = 100.0f;
      positions[Constants.NUMBER_OF_PLANES / 2 * 3 + 3 * k + 2] = 5.0f;
    }

    // Advance simulation time via incrementing the time variable.
    time = time + 0.25f;

    // Copy the content of callsigns and positions to the bound RawFrame.
    buffer.copy(callsigns, positions);
  }

  /**
   * Return the last generated radar frame.
   */
  public RawFrame getFrame() {
    return buffer;
  }
}
