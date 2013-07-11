package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;

/**
 * An interface to a long accessor object. An accessor object encapsulates the
 * protocol required to access a long in raw memory.
 * 
 */
@SCJAllowed(javax.safetycritical.annotate.Level.LEVEL_0)
public interface RawLong extends RawLongRead, RawLongWrite {

}
