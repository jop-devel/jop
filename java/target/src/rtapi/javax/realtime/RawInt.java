package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import javax.safetycritical.annotate.SCJAllowed;

/**
 * An interface to a int accessor object. An accessor object encapsulates the protocol
required to access a int in raw memory.

 *
 */
@SCJAllowed(LEVEL_0)
public interface RawInt extends RawIntRead, RawIntWrite{

}
