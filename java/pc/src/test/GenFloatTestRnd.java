package test;
import java.io.*;
import java.util.*;
import com.jopdesign.sys.SoftFloat;

public class GenFloatTestRnd {

  private static int fcmpl(float a, float b) {
  if (Float.isNaN(a)) return -1;
  if (Float.isNaN(b)) return -1;
  if (a == b) return 0;
  if (a < b) return -1;
  return 1;
  }

  private static int fcmpg(float a, float b) {
  if (Float.isNaN(a)) return 1;
  if (Float.isNaN(b)) return 1;
  if (a == b) return 0;
  if (a < b) return -1;
  return 1;
  }

  static long l = 0;

  public static void main (String[] args) {

  System.err.println("Testing random values...");
  final long tim = System.currentTimeMillis();
  new Thread() {
    public void run() {
      for (;;) {
        System.out.print((l/1000000)+" M ");
        long diff = System.currentTimeMillis()-tim;
        diff /= 1000;
        if (diff!=0) {
          System.out.print((l/diff)+"/s");
        }
        try { Thread.sleep(1000); } catch(Exception e) {}
        System.out.print("\r");
      }
    }
  }.start();
  Random rnd = new Random();
  float a = 0;
  float b = 0;
  for (l = 0; true; ++l) {
    a = Float.intBitsToFloat(rnd.nextInt());
    b = Float.intBitsToFloat(rnd.nextInt());
    {
      int pc_val = Float.floatToRawIntBits(a + b);
      int jop_val = SoftFloat.float32_add (Float.floatToRawIntBits(a), Float.floatToRawIntBits(b));
      if ((Float.isNaN(Float.intBitsToFloat(pc_val)) != Float.isNaN(Float.intBitsToFloat(jop_val))) || (!Float.isNaN(Float.intBitsToFloat(pc_val)) && (pc_val != jop_val))) {
        System.err.println("("+Integer.toHexString(Float.floatToRawIntBits(a))+"+" +Integer.toHexString(Float.floatToRawIntBits(b))+") != SoftFloat.float32_add ("+Integer.toHexString(Float.floatToRawIntBits(a))+"," +Integer.toHexString(Float.floatToRawIntBits(b))+")");
        System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
      }
    }
    {
      int pc_val = Float.floatToRawIntBits(a - b);
      int jop_val = SoftFloat.float32_sub (Float.floatToRawIntBits(a), Float.floatToRawIntBits(b));
      if ((Float.isNaN(Float.intBitsToFloat(pc_val)) != Float.isNaN(Float.intBitsToFloat(jop_val))) || (!Float.isNaN(Float.intBitsToFloat(pc_val)) && (pc_val != jop_val))) {
        System.err.println("("+Integer.toHexString(Float.floatToRawIntBits(a))+"-" +Integer.toHexString(Float.floatToRawIntBits(b))+") != SoftFloat.float32_sub ("+Integer.toHexString(Float.floatToRawIntBits(a))+"," +Integer.toHexString(Float.floatToRawIntBits(b))+")");
        System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
      }
    }
    {
      int pc_val = Float.floatToRawIntBits(a * b);
      int jop_val = SoftFloat.float32_mul (Float.floatToRawIntBits(a), Float.floatToRawIntBits(b));
      if ((Float.isNaN(Float.intBitsToFloat(pc_val)) != Float.isNaN(Float.intBitsToFloat(jop_val))) || (!Float.isNaN(Float.intBitsToFloat(pc_val)) && (pc_val != jop_val))) {
        System.err.println("("+Integer.toHexString(Float.floatToRawIntBits(a))+"*" +Integer.toHexString(Float.floatToRawIntBits(b))+") != SoftFloat.float32_mul ("+Integer.toHexString(Float.floatToRawIntBits(a))+"," +Integer.toHexString(Float.floatToRawIntBits(b))+")");
        System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
      }
    }
    {
      int pc_val = Float.floatToRawIntBits(a / b);
      int jop_val = SoftFloat.float32_div (Float.floatToRawIntBits(a), Float.floatToRawIntBits(b));
      if ((Float.isNaN(Float.intBitsToFloat(pc_val)) != Float.isNaN(Float.intBitsToFloat(jop_val))) || (!Float.isNaN(Float.intBitsToFloat(pc_val)) && (pc_val != jop_val))) {
        System.err.println("("+Integer.toHexString(Float.floatToRawIntBits(a))+"/" +Integer.toHexString(Float.floatToRawIntBits(b))+") != SoftFloat.float32_div ("+Integer.toHexString(Float.floatToRawIntBits(a))+"," +Integer.toHexString(Float.floatToRawIntBits(b))+")");
        System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
      }
    }
    {
      int pc_val = Float.floatToRawIntBits(a % b);
      int jop_val = SoftFloat.float32_rem (Float.floatToRawIntBits(a), Float.floatToRawIntBits(b));
      if ((Float.isNaN(Float.intBitsToFloat(pc_val)) != Float.isNaN(Float.intBitsToFloat(jop_val))) || (!Float.isNaN(Float.intBitsToFloat(pc_val)) && (pc_val != jop_val))) {
        System.err.println("("+Integer.toHexString(Float.floatToRawIntBits(a))+"%" +Integer.toHexString(Float.floatToRawIntBits(b))+") != SoftFloat.float32_rem ("+Integer.toHexString(Float.floatToRawIntBits(a))+"," +Integer.toHexString(Float.floatToRawIntBits(b))+")");
        System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
      }
    }
    {
       int pc_val = fcmpl (a, b);
       int jop_val = SoftFloat.float32_cmpl (Float.floatToRawIntBits(a), Float.floatToRawIntBits(b));
       if (pc_val != jop_val) {
         System.err.println("fcmpl ("+Integer.toHexString(Float.floatToRawIntBits(a))+"," +Integer.toHexString(Float.floatToRawIntBits(b))+") != SoftFloat.float32_cmpl ("+Integer.toHexString(Float.floatToRawIntBits(a))+"," +Integer.toHexString(Float.floatToRawIntBits(b))+")");
         System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
       }
    }
    {
       int pc_val = fcmpg (a, b);
       int jop_val = SoftFloat.float32_cmpg (Float.floatToRawIntBits(a), Float.floatToRawIntBits(b));
       if (pc_val != jop_val) {
         System.err.println("fcmpg ("+Integer.toHexString(Float.floatToRawIntBits(a))+"," +Integer.toHexString(Float.floatToRawIntBits(b))+") != SoftFloat.float32_cmpg ("+Integer.toHexString(Float.floatToRawIntBits(a))+"," +Integer.toHexString(Float.floatToRawIntBits(b))+")");
         System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
       }
    }
    {
       int pc_val = Math.round (a);
       int jop_val = SoftFloat.float32_to_int32 (Float.floatToRawIntBits(a));
       if (pc_val != jop_val) {
         System.err.println("Math.round ("+Integer.toHexString(Float.floatToRawIntBits(a))+") != SoftFloat.float32_to_int32 ("+Integer.toHexString(Float.floatToRawIntBits(a))+")");
         System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
       }
    }
    {
       int pc_val = (int) (a);
       int jop_val = SoftFloat.float32_to_int32_round_to_zero (Float.floatToRawIntBits(a));
       if (pc_val != jop_val) {
         System.err.println("(int) ("+Integer.toHexString(Float.floatToRawIntBits(a))+") != SoftFloat.float32_to_int32_round_to_zero ("+Integer.toHexString(Float.floatToRawIntBits(a))+")");
         System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
       }
    }
    }
  }
}
