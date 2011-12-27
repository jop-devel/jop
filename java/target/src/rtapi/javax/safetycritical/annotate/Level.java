package javax.safetycritical.annotate;

@SCJAllowed
public enum Level
{
  @SCJAllowed
  LEVEL_0        { @Override public int value() { return 0; } },

  @SCJAllowed
  LEVEL_1        { @Override public int value() { return 1; } },

  @SCJAllowed
  LEVEL_2        { @Override public int value() { return 2; } },

  @SCJAllowed
  SUPPORT        { @Override public int value() { return 3; } },

  @SCJAllowed
  INFRASTRUCTURE { @Override public int value() { return 4; } },

  @SCJAllowed
  HIDDEN         { @Override public int value() { return 5; } };

  @SCJAllowed
  public abstract int value();
  @SCJAllowed
  public static Level getLevel(String value)
  {
    if ("0".equals(value))
      return LEVEL_0;
    else if ("1".equals(value))
      return LEVEL_1;
    else if ("2".equals(value))
      return LEVEL_2;
    else
      throw new IllegalArgumentException("The value" + value +
                                         " is not a legal level.");
  }
}