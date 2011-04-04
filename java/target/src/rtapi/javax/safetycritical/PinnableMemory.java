package javax.safetycritical;

/**
 * TBD: I don't think this class belongs in javax.safetycritical. It can lead to
 * fragmentation of memory.
 */
public class PinnableMemory extends PrivateMemory
{
  public PinnableMemory(long size) { super(size); }
	
  public void Pin() {}

  public void Unpin() {}
}
