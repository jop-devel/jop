package javax.realtime;

public interface RawRealAccess {
  public double getDouble(long offset);
 /*        throws javax.realtime.OffsetOutOfBoundsException,
                javax.realtime.SizeOutOfBoundsException; */
  public void getDoubles(long offset, double[] doubles, int low, int number);
/*         throws javax.realtime.OffsetOutOfBoundsException,
                javax.realtime.SizeOutOfBoundsException;*/
  public float getFloat(long offset);
/*         throws javax.realtime.OffsetOutOfBoundsException,
                javax.realtime.SizeOutOfBoundsException;*/
  public void getFloats(long offset, float[] floats, int low, int number);
/*         throws javax.realtime.OffsetOutOfBoundsException,
                javax.realtime.SizeOutOfBoundsException;*/
  public void setDouble(long offset, double value);
/*         throws javax.realtime.OffsetOutOfBoundsException,
                javax.realtime.SizeOutOfBoundsException;*/
  public void setDoubles(long offset, double[] doubles, int low, int number);
/*         throws javax.realtime.OffsetOutOfBoundsException,
                javax.realtime.SizeOutOfBoundsException;*/
  public void setFloat(long offset, float value);
/*         throws javax.realtime.OffsetOutOfBoundsException,
                javax.realtime.SizeOutOfBoundsException;*/
  public void setFloats(long offset, float[] floats, int low, int number);
/*         throws javax.realtime.OffsetOutOfBoundsException,
                javax.realtime.SizeOutOfBoundsException;*/

}
