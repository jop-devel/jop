package hijac.cdx;

import hijac.cdx.ArrayListFactory;
import hijac.cdx.Constants;
import hijac.cdx.MotionFactory;

/**
 * This class acts as a contained for all factories needed to pre-allocate
 * shared objects in mission memory.
 *
 * @author Frank Zeyda
 */
class PersistentData {
  private final ArrayListFactory listFactory;
  private final MotionFactory motionFactory;

  public PersistentData() {
    listFactory = new ArrayListFactory(
      Constants.NUMBER_OF_PLANES * 3,
      Constants.NUMBER_OF_PLANES);

    motionFactory = new MotionFactory(
      Constants.NUMBER_OF_PLANES * Constants.NUMBER_OF_PLANES);
  }

  public ArrayListFactory getListFactory() {
    return listFactory;
  }

  public MotionFactory getMotionFactory() {
    return motionFactory;
  }
}
