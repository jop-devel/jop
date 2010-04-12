package scd_micro;

import java.util.Collection;

public class RawFrame {
	public static final int MAX_PLANES = 10; //1000;
	public static final  int MAX_PLANES_POW2 = 1<<4;
	public static final  int MAX_COLLISIONS = (MAX_PLANES*(MAX_PLANES-1)/2);
	public static final  int MAX_COLLISIONS_POW2 = 64;
	public static final  int MAX_SIGNS = 10 * MAX_PLANES;
	public static final  int MAX_CALLSIGN_LENGTH = 10; 

    public final int[] lengths = new int[MAX_PLANES];
    public final byte[] callsigns = new byte[MAX_SIGNS];
    public final float[] positions = new float[3 * MAX_PLANES];
    public int planeCnt;

    public void copy(final int[] lengths_, final byte[] signs_, final float[] positions_) {
        for (int i = 0, pos = 0, pos2 = 0, pos3 = 0, pos4 = 0; i < lengths_.length; i++) {
            lengths[pos++] = lengths_[i];
            positions[pos2++] = positions_[3 * i];
            positions[pos2++] = positions_[3 * i + 1];
            positions[pos2++] = positions_[3 * i + 2]; //        MAX_CALLSIGN_LENGTH
            for (int j = 0; j < lengths_[i]; j++) { //@WCA loop<=10
                callsigns[pos3++] = signs_[pos4 + j];
            }
            pos4 += lengths_[i];
        }
        planeCnt = lengths_.length;
    }
}