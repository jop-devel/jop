package ravenscar;
import javax.realtime.*;
public class SporadicInterrupt extends javax.realtime.AsyncEvent
{
  public SporadicInterrupt(SporadicEventHandler handler, java.lang.String happening)
  {
    super();
    super.bindTo(happening);
    super.addHandler(handler);
  }
  
};

