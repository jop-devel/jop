package javax.realtime;

//import java.io.Serializable;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJProtected;

@SCJAllowed
public class ThrowBoundaryError extends Error { // implements Serializable {

  @SCJProtected
  public ThrowBoundaryError() {
  }
  
  @SCJProtected
  public ThrowBoundaryError(String description) {
    super(description);
  }
}
