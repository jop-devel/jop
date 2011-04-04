package javax.realtime;

public class MemoryParameters // implements Cloneable
{
  public static final long NO_MAX = -1;

  public MemoryParameters(long maxMemoryArea, long maxImmortal)
    throws IllegalArgumentException
  {
    this(maxMemoryArea, maxImmortal,NO_MAX);
  }

  public MemoryParameters(long maxMemoryArea,
			  long maxImmortal,
			  long allocationRate)
    throws IllegalArgumentException
  {
  }

  public long getAllocationRate() { return 0L; }

  public long getMaxImmortal()    { return 0L; }

  public long getMaxMemoryArea()  { return 0L; }

  public void setAllocationRate(long allocationRate) {}

  public boolean setMaxImmortalIfFeasible(long maximum) { return false; }

  public boolean setMaxMemoryAreaIfFeasible(long maximum) { return false; }

  public boolean setAllocationRateIfFeasible(long allocationRate)
  {
    return false;
  }

  boolean setMaxImmortal(long maximum) { return false; }

  boolean setMaxMemoryArea(long maximum) { return false; }

  public Object clone() { return null; }
}
