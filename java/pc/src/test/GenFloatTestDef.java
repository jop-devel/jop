package test;
import java.io.*;
import java.util.*;
import com.jopdesign.sys.SoftFloat;

public class GenFloatTestDef {

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

  public static void main (String[] args) {

  float [] nums = { Float.NaN,
	Float.MAX_VALUE,
	Float.MIN_VALUE,
	Float.NEGATIVE_INFINITY,
	Float.POSITIVE_INFINITY,
	0.0f,
	1.0f,
	-1.0f,
	-126.0f,
	-24.0f,
	-2.0f,
	2.0f,
	24.0f,
	127.0f,
	3.14159f,
	2.71828f,
	Float.intBitsToFloat(0x00800000),
	Float.intBitsToFloat(0x33800000),
	Float.intBitsToFloat(0x3E800000),
	Float.intBitsToFloat(0x3F000000),
	Float.intBitsToFloat(0x3F800000),
	Float.intBitsToFloat(0x40000000),
	Float.intBitsToFloat(0x40800000),
	Float.intBitsToFloat(0x4B800000),
	Float.intBitsToFloat(0x7F000000),
	Float.intBitsToFloat(0x80800000),
	Float.intBitsToFloat(0xB3800000),
	Float.intBitsToFloat(0xBE800000),
	Float.intBitsToFloat(0xBF000000),
	Float.intBitsToFloat(0xBF800000),
	Float.intBitsToFloat(0xC0000000),
	Float.intBitsToFloat(0xC0800000),
	Float.intBitsToFloat(0xCB800000),
	Float.intBitsToFloat(0xFE800000),
	Float.intBitsToFloat(0x00000000),
	Float.intBitsToFloat(0x00000001),
	Float.intBitsToFloat(0x00000002),
	Float.intBitsToFloat(0x00000004),
	Float.intBitsToFloat(0x00000008),
	Float.intBitsToFloat(0x00000010),
	Float.intBitsToFloat(0x00000020),
	Float.intBitsToFloat(0x00000040),
	Float.intBitsToFloat(0x00000080),
	Float.intBitsToFloat(0x00000100),
	Float.intBitsToFloat(0x00000200),
	Float.intBitsToFloat(0x00000400),
	Float.intBitsToFloat(0x00000800),
	Float.intBitsToFloat(0x00001000),
	Float.intBitsToFloat(0x00002000),
	Float.intBitsToFloat(0x00004000),
	Float.intBitsToFloat(0x00008000),
	Float.intBitsToFloat(0x00010000),
	Float.intBitsToFloat(0x00020000),
	Float.intBitsToFloat(0x00040000),
	Float.intBitsToFloat(0x00080000),
	Float.intBitsToFloat(0x00100000),
	Float.intBitsToFloat(0x00200000),
	Float.intBitsToFloat(0x00400000),
	Float.intBitsToFloat(0x00800000),
	Float.intBitsToFloat(0x01000000),
	Float.intBitsToFloat(0x02000000),
	Float.intBitsToFloat(0x04000000),
	Float.intBitsToFloat(0x08000000),
	Float.intBitsToFloat(0x10000000),
	Float.intBitsToFloat(0x20000000),
	Float.intBitsToFloat(0x40000000),
	Float.intBitsToFloat(0x80000000),
	Float.intBitsToFloat(0xC0000000),
	Float.intBitsToFloat(0xE0000000),
	Float.intBitsToFloat(0xF0000000),
	Float.intBitsToFloat(0xF8000000),
	Float.intBitsToFloat(0xFC000000),
	Float.intBitsToFloat(0xFE000000),
	Float.intBitsToFloat(0xFF000000),
	Float.intBitsToFloat(0xFF800000),
	Float.intBitsToFloat(0xFFC00000),
	Float.intBitsToFloat(0xFFE00000),
	Float.intBitsToFloat(0xFFF00000),
	Float.intBitsToFloat(0xFFF80000),
	Float.intBitsToFloat(0xFFFC0000),
	Float.intBitsToFloat(0xFFFE0000),
	Float.intBitsToFloat(0xFFFF0000),
	Float.intBitsToFloat(0xFFFF8000),
	Float.intBitsToFloat(0xFFFFC000),
	Float.intBitsToFloat(0xFFFFE000),
	Float.intBitsToFloat(0xFFFFF000),
	Float.intBitsToFloat(0xFFFFF800),
	Float.intBitsToFloat(0xFFFFFC00),
	Float.intBitsToFloat(0xFFFFFE00),
	Float.intBitsToFloat(0xFFFFFF00),
	Float.intBitsToFloat(0xFFFFFF80),
	Float.intBitsToFloat(0xFFFFFFC0),
	Float.intBitsToFloat(0xFFFFFFE0),
	Float.intBitsToFloat(0xFFFFFFF0),
	Float.intBitsToFloat(0xFFFFFFF8),
	Float.intBitsToFloat(0xFFFFFFFC),
	Float.intBitsToFloat(0xFFFFFFFE),
	Float.intBitsToFloat(0xFFFFFFFF),
	Float.intBitsToFloat(0xFFFFFFFE),
	Float.intBitsToFloat(0xFFFFFFFD),
	Float.intBitsToFloat(0xFFFFFFFB),
	Float.intBitsToFloat(0xFFFFFFF7),
	Float.intBitsToFloat(0xFFFFFFEF),
	Float.intBitsToFloat(0xFFFFFFDF),
	Float.intBitsToFloat(0xFFFFFFBF),
	Float.intBitsToFloat(0xFFFFFF7F),
	Float.intBitsToFloat(0xFFFFFEFF),
	Float.intBitsToFloat(0xFFFFFDFF),
	Float.intBitsToFloat(0xFFFFFBFF),
	Float.intBitsToFloat(0xFFFFF7FF),
	Float.intBitsToFloat(0xFFFFEFFF),
	Float.intBitsToFloat(0xFFFFDFFF),
	Float.intBitsToFloat(0xFFFFBFFF),
	Float.intBitsToFloat(0xFFFF7FFF),
	Float.intBitsToFloat(0xFFFEFFFF),
	Float.intBitsToFloat(0xFFFDFFFF),
	Float.intBitsToFloat(0xFFFBFFFF),
	Float.intBitsToFloat(0xFFF7FFFF),
	Float.intBitsToFloat(0xFFEFFFFF),
	Float.intBitsToFloat(0xFFDFFFFF),
	Float.intBitsToFloat(0xFFBFFFFF),
	Float.intBitsToFloat(0xFF7FFFFF),
	Float.intBitsToFloat(0xFEFFFFFF),
	Float.intBitsToFloat(0xFDFFFFFF),
	Float.intBitsToFloat(0xFBFFFFFF),
	Float.intBitsToFloat(0xF7FFFFFF),
	Float.intBitsToFloat(0xEFFFFFFF),
	Float.intBitsToFloat(0xDFFFFFFF),
	Float.intBitsToFloat(0xBFFFFFFF),
	Float.intBitsToFloat(0x7FFFFFFF),
	Float.intBitsToFloat(0x3FFFFFFF),
	Float.intBitsToFloat(0x1FFFFFFF),
	Float.intBitsToFloat(0x0FFFFFFF),
	Float.intBitsToFloat(0x07FFFFFF),
	Float.intBitsToFloat(0x03FFFFFF),
	Float.intBitsToFloat(0x01FFFFFF),
	Float.intBitsToFloat(0x00FFFFFF),
	Float.intBitsToFloat(0x007FFFFF),
	Float.intBitsToFloat(0x003FFFFF),
	Float.intBitsToFloat(0x001FFFFF),
	Float.intBitsToFloat(0x000FFFFF),
	Float.intBitsToFloat(0x0007FFFF),
	Float.intBitsToFloat(0x0003FFFF),
	Float.intBitsToFloat(0x0001FFFF),
	Float.intBitsToFloat(0x0000FFFF),
	Float.intBitsToFloat(0x00007FFF),
	Float.intBitsToFloat(0x00003FFF),
	Float.intBitsToFloat(0x00001FFF),
	Float.intBitsToFloat(0x00000FFF),
	Float.intBitsToFloat(0x000007FF),
	Float.intBitsToFloat(0x000003FF),
	Float.intBitsToFloat(0x000001FF),
	Float.intBitsToFloat(0x000000FF),
	Float.intBitsToFloat(0x0000007F),
	Float.intBitsToFloat(0x0000003F),
	Float.intBitsToFloat(0x0000001F),
	Float.intBitsToFloat(0x0000000F),
	Float.intBitsToFloat(0x00000007),
	Float.intBitsToFloat(0x00000003),
	Float.intBitsToFloat(0x80000000),
	Float.intBitsToFloat(0x80000001),
	Float.intBitsToFloat(0x80000002),
	Float.intBitsToFloat(0x80000004),
	Float.intBitsToFloat(0x80000008),
	Float.intBitsToFloat(0x80000010),
	Float.intBitsToFloat(0x80000020),
	Float.intBitsToFloat(0x80000040),
	Float.intBitsToFloat(0x80000080),
	Float.intBitsToFloat(0x80000100),
	Float.intBitsToFloat(0x80000200),
	Float.intBitsToFloat(0x80000400),
	Float.intBitsToFloat(0x80000800),
	Float.intBitsToFloat(0x80001000),
	Float.intBitsToFloat(0x80002000),
	Float.intBitsToFloat(0x80004000),
	Float.intBitsToFloat(0x80008000),
	Float.intBitsToFloat(0x80010000),
	Float.intBitsToFloat(0x80020000),
	Float.intBitsToFloat(0x80040000),
	Float.intBitsToFloat(0x80080000),
	Float.intBitsToFloat(0x80100000),
	Float.intBitsToFloat(0x80200000),
	Float.intBitsToFloat(0x80400000),
	Float.intBitsToFloat(0x80800000),
	Float.intBitsToFloat(0x81000000),
	Float.intBitsToFloat(0x82000000),
	Float.intBitsToFloat(0x84000000),
	Float.intBitsToFloat(0x88000000),
	Float.intBitsToFloat(0x90000000),
	Float.intBitsToFloat(0xA0000000),
	Float.intBitsToFloat(0xC0000000),
	Float.intBitsToFloat(0x80000000),
	Float.intBitsToFloat(0xBFFFFFFF),
	Float.intBitsToFloat(0x9FFFFFFF),
	Float.intBitsToFloat(0x8FFFFFFF),
	Float.intBitsToFloat(0x87FFFFFF),
	Float.intBitsToFloat(0x83FFFFFF),
	Float.intBitsToFloat(0x81FFFFFF),
	Float.intBitsToFloat(0x80FFFFFF),
	Float.intBitsToFloat(0x807FFFFF),
	Float.intBitsToFloat(0x803FFFFF),
	Float.intBitsToFloat(0x801FFFFF),
	Float.intBitsToFloat(0x800FFFFF),
	Float.intBitsToFloat(0x8007FFFF),
	Float.intBitsToFloat(0x8003FFFF),
	Float.intBitsToFloat(0x8001FFFF),
	Float.intBitsToFloat(0x8000FFFF),
	Float.intBitsToFloat(0x80007FFF),
	Float.intBitsToFloat(0x80003FFF),
	Float.intBitsToFloat(0x80001FFF),
	Float.intBitsToFloat(0x80000FFF),
	Float.intBitsToFloat(0x800007FF),
	Float.intBitsToFloat(0x800003FF),
	Float.intBitsToFloat(0x800001FF),
	Float.intBitsToFloat(0x800000FF),
	Float.intBitsToFloat(0x8000007F),
	Float.intBitsToFloat(0x8000003F),
	Float.intBitsToFloat(0x8000001F),
	Float.intBitsToFloat(0x8000000F),
	Float.intBitsToFloat(0x80000007),
	Float.intBitsToFloat(0x80000003),
	Float.intBitsToFloat(0xabfb2c81),
	Float.intBitsToFloat(0xeb21fece),
	Float.intBitsToFloat(0x361b388b),
	Float.intBitsToFloat(0x7621d144),
	Float.intBitsToFloat(0x838b1c87),
	Float.intBitsToFloat(0x4476555a),
	Float.intBitsToFloat(0xac7cc276),
	Float.intBitsToFloat(0xec4111fe),
	Float.intBitsToFloat(0x83bd1c1b),
	Float.intBitsToFloat(0x43bd69ec),
	Float.intBitsToFloat(0xb08711b2),
	Float.intBitsToFloat(0x70e7fc01),
	Float.intBitsToFloat(0xb6c4c126),
	Float.intBitsToFloat(0xf84a5aed),
	Float.intBitsToFloat(0x9e1b55f9),
	Float.intBitsToFloat(0x5e99b660),
	Float.intBitsToFloat(0xbfc02e00),
	Float.intBitsToFloat(0xc2020300),
	Float.intBitsToFloat(0xc22a0300),
	Float.intBitsToFloat(0xc2020300),
	Float.intBitsToFloat(0xc1780200),
	Float.intBitsToFloat(0xc1e40400),
	Float.intBitsToFloat(0xbfc02c00),
	Float.intBitsToFloat(0xc1180d00),
	Float.intBitsToFloat(0xc2df0100),
	Float.intBitsToFloat(0xc19c0000),
	Float.intBitsToFloat(0xc2950100),
	Float.intBitsToFloat(0xc2a30100),
	Float.intBitsToFloat(0xc2c30100),
	Float.intBitsToFloat(0x3effffff)  };

  System.err.println("Testing predefined values...");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
    {
      int pc_val = Float.floatToRawIntBits(nums[i] + nums[k]);
      int jop_val = SoftFloat.float32_add (Float.floatToRawIntBits(nums[i]), Float.floatToRawIntBits(nums[k]));
      if ((Float.isNaN(Float.intBitsToFloat(pc_val)) != Float.isNaN(Float.intBitsToFloat(jop_val))) || (!Float.isNaN(Float.intBitsToFloat(pc_val)) && (pc_val != jop_val))) {
        System.err.println("("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+"+" +Integer.toHexString(Float.floatToRawIntBits(nums[k]))+") != SoftFloat.float32_add ("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+"," +Integer.toHexString(Float.floatToRawIntBits(nums[k]))+")");
        System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
      }
    }
    }
  }
  System.err.println("Testing SoftFloat.float32_add finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
    {
      int pc_val = Float.floatToRawIntBits(nums[i] - nums[k]);
      int jop_val = SoftFloat.float32_sub (Float.floatToRawIntBits(nums[i]), Float.floatToRawIntBits(nums[k]));
      if ((Float.isNaN(Float.intBitsToFloat(pc_val)) != Float.isNaN(Float.intBitsToFloat(jop_val))) || (!Float.isNaN(Float.intBitsToFloat(pc_val)) && (pc_val != jop_val))) {
        System.err.println("("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+"-" +Integer.toHexString(Float.floatToRawIntBits(nums[k]))+") != SoftFloat.float32_sub ("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+"," +Integer.toHexString(Float.floatToRawIntBits(nums[k]))+")");
        System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
      }
    }
    }
  }
  System.err.println("Testing SoftFloat.float32_sub finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
    {
      int pc_val = Float.floatToRawIntBits(nums[i] * nums[k]);
      int jop_val = SoftFloat.float32_mul (Float.floatToRawIntBits(nums[i]), Float.floatToRawIntBits(nums[k]));
      if ((Float.isNaN(Float.intBitsToFloat(pc_val)) != Float.isNaN(Float.intBitsToFloat(jop_val))) || (!Float.isNaN(Float.intBitsToFloat(pc_val)) && (pc_val != jop_val))) {
        System.err.println("("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+"*" +Integer.toHexString(Float.floatToRawIntBits(nums[k]))+") != SoftFloat.float32_mul ("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+"," +Integer.toHexString(Float.floatToRawIntBits(nums[k]))+")");
        System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
      }
    }
    }
  }
  System.err.println("Testing SoftFloat.float32_mul finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
    {
      int pc_val = Float.floatToRawIntBits(nums[i] / nums[k]);
      int jop_val = SoftFloat.float32_div (Float.floatToRawIntBits(nums[i]), Float.floatToRawIntBits(nums[k]));
      if ((Float.isNaN(Float.intBitsToFloat(pc_val)) != Float.isNaN(Float.intBitsToFloat(jop_val))) || (!Float.isNaN(Float.intBitsToFloat(pc_val)) && (pc_val != jop_val))) {
        System.err.println("("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+"/" +Integer.toHexString(Float.floatToRawIntBits(nums[k]))+") != SoftFloat.float32_div ("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+"," +Integer.toHexString(Float.floatToRawIntBits(nums[k]))+")");
        System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
      }
    }
    }
  }
  System.err.println("Testing SoftFloat.float32_div finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
    {
      int pc_val = Float.floatToRawIntBits(nums[i] % nums[k]);
      int jop_val = SoftFloat.float32_rem (Float.floatToRawIntBits(nums[i]), Float.floatToRawIntBits(nums[k]));
      if ((Float.isNaN(Float.intBitsToFloat(pc_val)) != Float.isNaN(Float.intBitsToFloat(jop_val))) || (!Float.isNaN(Float.intBitsToFloat(pc_val)) && (pc_val != jop_val))) {
        System.err.println("("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+"%" +Integer.toHexString(Float.floatToRawIntBits(nums[k]))+") != SoftFloat.float32_rem ("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+"," +Integer.toHexString(Float.floatToRawIntBits(nums[k]))+")");
        System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
      }
    }
    }
  }
  System.err.println("Testing SoftFloat.float32_rem finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
    {
       int pc_val = fcmpl (nums[i], nums[k]);
       int jop_val = SoftFloat.float32_cmpl (Float.floatToRawIntBits(nums[i]), Float.floatToRawIntBits(nums[k]));
       if (pc_val != jop_val) {
         System.err.println("fcmpl ("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+"," +Integer.toHexString(Float.floatToRawIntBits(nums[k]))+") != SoftFloat.float32_cmpl ("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+"," +Integer.toHexString(Float.floatToRawIntBits(nums[k]))+")");
         System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
       }
    }
    }
  }
  System.err.println("Testing SoftFloat.float32_cmpl finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
    {
       int pc_val = fcmpg (nums[i], nums[k]);
       int jop_val = SoftFloat.float32_cmpg (Float.floatToRawIntBits(nums[i]), Float.floatToRawIntBits(nums[k]));
       if (pc_val != jop_val) {
         System.err.println("fcmpg ("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+"," +Integer.toHexString(Float.floatToRawIntBits(nums[k]))+") != SoftFloat.float32_cmpg ("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+"," +Integer.toHexString(Float.floatToRawIntBits(nums[k]))+")");
         System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
       }
    }
    }
  }
  System.err.println("Testing SoftFloat.float32_cmpg finished.");
  for (int i=0; i<nums.length; i++) {
    {
       int pc_val = Math.round (nums[i]);
       int jop_val = SoftFloat.float32_to_int32 (Float.floatToRawIntBits(nums[i]));
       if (pc_val != jop_val) {
         System.err.println("Math.round ("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+") != SoftFloat.float32_to_int32 ("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+")");
         System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
       }
    }
  }
  System.err.println("Testing SoftFloat.float32_to_int32 finished.");
  for (int i=0; i<nums.length; i++) {
    {
       int pc_val = (int) (nums[i]);
       int jop_val = SoftFloat.float32_to_int32_round_to_zero (Float.floatToRawIntBits(nums[i]));
       if (pc_val != jop_val) {
         System.err.println("(int) ("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+") != SoftFloat.float32_to_int32_round_to_zero ("+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+")");
         System.err.println(Integer.toHexString(pc_val)+" != "+Integer.toHexString(jop_val));
       }
    }
  }
  System.err.println("Testing SoftFloat.float32_to_int32_round_to_zero finished.");
    System.err.println("All tests finished.");
  }
}
