package javax.safetycritical.annotate;

@SCJAllowed
public enum Phase
{
  @SCJAllowed
  INITIALIZATION { @Override public int value() { return 0; } },

  @SCJAllowed
  RUN            { @Override public int value() { return 1; } },

  @SCJAllowed
  CLEANUP        { @Override public int value() { return 2; } },

  @SCJAllowed
  ALL            { @Override public int value() { return 3; } };

  public abstract int value();
}
