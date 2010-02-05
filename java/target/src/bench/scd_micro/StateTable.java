package scd_micro;

import java.util.HashMap;


/**
 * The instance lives and the constructor runs in the persistent detector scope.
 * The put method and the get method are called from the transient detector scope - see below.
 * 
 * previous_state is map call signs to 3D vectors
 *   - the call signs are in persistent detector scope
 *   - the vectors are in persistent detector scope (allocated here)
 */
public class StateTable {

//    final private static int MAX_AIRPLANES = 10000;
    final private static int MAX_AIRPLANES = 50;
    
    private Vector3d[] allocatedVectors;
    private int usedVectors;

    /** Mapping Aircraft -> Vector3d. */
    final private HashMap motionVectors = new HashMap();


    StateTable() {
        allocatedVectors = new Vector3d[MAX_AIRPLANES];
        for (int i = 0; i < allocatedVectors.length; i++)
            allocatedVectors[i] = new Vector3d();
        
        usedVectors = 0;
    }


    private class R implements Runnable {
        CallSign callsign;
        float x, y, z;

        public void run() {
            Vector3d v = (Vector3d) motionVectors.get(callsign);
            if (v == null) {
                v = allocatedVectors[usedVectors++]; // FIXME: What if we exceed MAX?
                motionVectors.put(callsign, v);
            }
            v.x = x;
            v.y = y;
            v.z = z;
        }
    }
    private final R r = new R();

    public void put(final CallSign callsign, final float x, final float y, final float z) {
        r.callsign = callsign;
        r.x = x;
        r.y = y;
        r.z = z;
        // MemoryArea.getMemoryArea(this).executeInArea(r);
        r.run();
    }
    
    public Vector3d get(final CallSign callsign) {
    	return (Vector3d) motionVectors.get(callsign);
    }
}
