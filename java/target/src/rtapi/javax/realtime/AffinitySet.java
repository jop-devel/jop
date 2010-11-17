package javax.realtime;
import javax.safetycritical.annotate.SCJAllowed;
import static javax.safetycritical.annotate.Level.LEVEL_0;
import static javax.safetycritical.annotate.Level.LEVEL_1;
import static javax.safetycritical.annotate.Level.LEVEL_2;

@SCJAllowed
public final class AffinitySet {

  /*
   * @param At Level 0, the bitSet must have a single bit set.
   *        At level 1, the same bit must not be set in any previously
   *                    created AffinitySet
   * @returns An AffinitySet representing a subset of the processors
   *          in the system. 
   */
//  @SCJAllowed(LEVEL_2)
//  public static AffinitySet generate(java.util.BitSet bitSet) { return null; }
  
  @SCJAllowed(LEVEL_1)
  public static final AffinitySet
  getAffinitySet(BoundAsyncEventHandler handler) {
    return null;
  }
  
  @SCJAllowed(LEVEL_2)
  public static final AffinitySet getAffinitySet(java.lang.Thread thread) {
    return null;
  }
  
//  @SCJAllowed(LEVEL_1)
//  public static final java.util.BitSet getAvailableProcessors() {return null;}
//  
//  @SCJAllowed(LEVEL_1)
//  public static final java.util.BitSet
//  getAvailableProcessors(java.util.BitSet dest) {return null;}
  
  public static final AffinitySet getHeapSoDefaultAffinity() {return null;}
  
  public static final AffinitySet getJavaThreadDefaultAffinity() {return null;}
  
  @SCJAllowed(LEVEL_1)
  public static final AffinitySet getNoHeapSoDefaultAffinity() {return null;}
  
  @SCJAllowed(LEVEL_1)
  public static int getPredefinedAffinitySetCount() {return 2;}
  
  @SCJAllowed(LEVEL_1)
  public static AffinitySet[] getPredefinedAffinitySets() {return null;}
  
  @SCJAllowed(LEVEL_1)
  public static AffinitySet[] getPredefinedAffinitySets(AffinitySet[] dest) {
    return null;
  }
  
  @SCJAllowed(LEVEL_1)
  public static final void
  setProcessorAffinity(AffinitySet set,
                       javax.realtime.BoundAsyncEventHandler aeh) {}
                                              
  @SCJAllowed(LEVEL_2)
  public static final void
  setProcessorAffinity(AffinitySet set, java.lang.Thread thread) {}

//  @SCJAllowed(LEVEL_1)
//  public final java.util.BitSet getBitSet() {
//    return null;
//  }
  
//  @SCJAllowed(LEVEL_1)
//  public final java.util.BitSet getProcessors(java.util.BitSet dest) {
//    return null;
//  }

  @SCJAllowed(LEVEL_2)
  public final boolean isProcessorInSet(int processorNumber) {
    return true;
  }

}
