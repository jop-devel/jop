package ravenscar;
import javax.realtime.*;

public class SporadicParameters extends ReleaseParameters
{

  public SporadicParameters(RelativeTime minInterarrival, int bufferSize)
  {
    super();
    myArrival = minInterarrival;
  }
  

  private RelativeTime myArrival;
  
  protected RelativeTime getMinInterarrival()
  {
    return myArrival;
  }
  
}
