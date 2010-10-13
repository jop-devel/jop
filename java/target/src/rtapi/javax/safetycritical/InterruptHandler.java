package javax.safetycritical;

import static javax.safetycritical.annotate.Level.LEVEL_1;
import javax.safetycritical.annotate.SCJAllowed;

@SCJAllowed(LEVEL_1)
public abstract class InterruptHandler {

  /**
   * Create and register an interrupt handler.
   * Can only be called during the initialization
   * phase of a mission.
   * The interrupt is automatically enabled.
   * The ceiling of the objects is set to the
   * hardware priority of the interrupt.
   * It is assumed that the associated MissionManager
   * will unregister the interrupt handler on mission
   * termination.
   * @throws IllegalArgument when InterruptId is unsupported
   * @throws IllegalStateException when a handler is already registered
   *         or if called outside the initialization phase.
   */
  @SCJAllowed(LEVEL_1)
  public InterruptHandler(int InterruptID) { }

  /**
   * Override this method to provide the first level interrupt
   * handler. It is TBD whether global interrupts are automatically
   * enabled before this method is called.
   */
  @SCJAllowed(LEVEL_1)
  public synchronized void handleInterrupt() { }

  /* @SCJAllowed(LEVEL_1) */
  static public void enableGlobalInterrupts() {}

  /**
   * Registers an interrupt handler.
   * @throws IllegalArgument if unsupported InterruptId
   *         IllegalStateException if handler already registered
   */
  static void registerInterruptHandler(int InterruptId,
                    InterruptHandler IH) { }


  /**
   * Every interrupt has an implementation-defined integer id.
   *
   * @return The priority of the code that the first-level
   *  interrupts code executes. The returned value is always greater
   *  than PriorityScheduler.getMaxPriority().
   * @throws IllegalArgument if unsupported InterruptId
   */
  @SCJAllowed(LEVEL_1)
  public static int getInterruptPriority(int InterruptId) { return 33; }
}
