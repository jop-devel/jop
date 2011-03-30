package javax.safetycritical;

/**
 * This interface marked those objects that are managed
 * by some mission and provides a means to obtain the
 * manager for that mission.
 */
public abstract class MissionManager extends PortalExtender
{
  public Mission getMission() { return null; }
}
