package rttm.skiplist;

public class Const {

    // Create deterministic output for bvt
    public static boolean DETERMINISTIC = false;
	public static final int MAGIC = -10000;
	public static final int CNT = 100;
    // Number of threads to run
    public static int THREADS        = 4;

    // Number of successful operations per thread (-1 => loop forever)
    public static int OP_COUNT       = 10;

    // Range of key values, 0..KEY_SPACE_MASK
    // public static int KEY_SPACE_MASK = 0xffff;
    public static int KEY_SPACE_MASK = 0xff;

    // Workload mix, should sum to 0x100
    public static int LOOKUP_FRAC    = 0xc0;
    public static int REMOVE_FRAC    = 0x20;
    public static int INSERT_FRAC    = 0x20;
    public enum RUN_KIND {LOCK, TM}
    public static RUN_KIND run_kind = RUN_KIND.TM;
}
