package ravenscar;
import javax.realtime.*;

public class ScopedMemory extends javax.realtime.ScopedMemory
{

  public ScopedMemory(long size)
  {
    super(size);
  };
  public ScopedMemory(SizeEstimator size)
  {
    super(size);
  };
  
  public void enter() 
  {
    super.enter();
  }
  
  public java.lang.Object getPortal()
  {
    return super.getPortal();
  }
  
  public void setPoart(java.lang.Object object)
  {
    super.setPortal(object);
  }
  
}
    
