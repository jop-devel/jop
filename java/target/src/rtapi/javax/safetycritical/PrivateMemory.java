package javax.safetycritical;


import javax.realtime.SizeEstimator;
import javax.safetycritical.annotate.SCJAllowed;

@SCJAllowed
public class PrivateMemory extends ManagedMemory
{
  public PrivateMemory(long size) { super(size); }

  public PrivateMemory(SizeEstimator estimator) { super(estimator); }

}
