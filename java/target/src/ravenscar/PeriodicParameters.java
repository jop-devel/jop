package ravenscar;
import javax.realtime.*;

public class PeriodicParameters // extends ReleaseParameters
{

  public PeriodicParameters(AbsoluteTime startTime, RelativeTime period)
  {
    // super();
    myEpoch =  startTime;
    myPeriod = period;
  }
  
  private AbsoluteTime myEpoch;
  private RelativeTime myPeriod;
  
  protected AbsoluteTime getEpoch()
  {
    return myEpoch;
  }
  
  protected RelativeTime getPeriod()
  {
    return myPeriod;
  }
  
}
