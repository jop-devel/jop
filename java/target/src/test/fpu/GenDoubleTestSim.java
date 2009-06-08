/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package fpu;

import com.jopdesign.sys.Native;

// class Native {
// 	public static double toDouble(long v) {
// 		return Double.longBitsToDouble(v);
// 	}
// }

public class GenDoubleTestSim {

  static double [] nums = { Double.NaN,
	Double.MAX_VALUE,
	Double.MIN_VALUE,
	Double.NEGATIVE_INFINITY,
	Double.POSITIVE_INFINITY,
	0.0,
	1.0,
	-1.0,
	-126.0,
	-24.0,
	-2.0,
	2.0,
	24.0,
	127.0,
	3.14159,
	2.71828,

	// we have no invokestatic in Startup.interpret()
	// use special native function
	Native.toDouble(0x0080000000800000L),
	Native.toDouble(0x3380000033800000L),
	Native.toDouble(0x3E8000003E800000L),
	Native.toDouble(0x3F0000003F000000L),
	Native.toDouble(0x3F8000003F800000L),
	Native.toDouble(0x4000000040000000L),
	Native.toDouble(0x4080000040800000L),
	Native.toDouble(0x4B8000004B800000L),
	Native.toDouble(0x7F0000007F000000L),
	Native.toDouble(0x8080000080800000L),
	Native.toDouble(0xB3800000B3800000L),
	Native.toDouble(0xBE800000BE800000L),
	Native.toDouble(0xBF000000BF000000L),
	Native.toDouble(0xBF800000BF800000L),
	Native.toDouble(0xC0000000C0000000L),
	Native.toDouble(0xC0800000C0800000L),
	Native.toDouble(0xCB800000CB800000L),
	Native.toDouble(0xFE800000FE800000L),
	Native.toDouble(0x0000000000000000L),
	Native.toDouble(0x0000000100000001L),
	Native.toDouble(0x0000000200000002L),
	Native.toDouble(0x0000000400000004L),
	Native.toDouble(0x0000000800000008L),
	Native.toDouble(0x0000001000000010L),
	Native.toDouble(0x0000002000000020L),
	Native.toDouble(0x0000004000000040L),
	Native.toDouble(0x0000008000000080L),
	Native.toDouble(0x0000010000000100L),
	Native.toDouble(0x0000020000000200L),
	Native.toDouble(0x0000040000000400L),
	Native.toDouble(0x0000080000000800L),
	Native.toDouble(0x0000100000001000L),
	Native.toDouble(0x0000200000002000L),
	Native.toDouble(0x0000400000004000L),
	Native.toDouble(0x0000800000008000L),
	Native.toDouble(0x0001000000010000L),
	Native.toDouble(0x0002000000020000L),
	Native.toDouble(0x0004000000040000L),
	Native.toDouble(0x0008000000080000L),
	Native.toDouble(0x0010000000100000L),
	Native.toDouble(0x0020000000200000L),
	Native.toDouble(0x0040000000400000L),
	Native.toDouble(0x0080000000800000L),
	Native.toDouble(0x0100000001000000L),
	Native.toDouble(0x0200000002000000L),
	Native.toDouble(0x0400000004000000L),
	Native.toDouble(0x0800000008000000L),
	Native.toDouble(0x1000000010000000L),
	Native.toDouble(0x2000000020000000L),
	Native.toDouble(0x4000000040000000L),
	Native.toDouble(0x8000000080000000L),
	Native.toDouble(0xC0000000C0000000L),
	Native.toDouble(0xE0000000E0000000L),
	Native.toDouble(0xF0000000F0000000L),
	Native.toDouble(0xF8000000F8000000L),
	Native.toDouble(0xFC000000FC000000L),
	Native.toDouble(0xFE000000FE000000L),
	Native.toDouble(0xFF000000FF000000L),
	Native.toDouble(0xFF800000FF800000L),
	Native.toDouble(0xFFC00000FFC00000L),
	Native.toDouble(0xFFE00000FFE00000L),
	Native.toDouble(0xFFF00000FFF00000L),
	Native.toDouble(0xFFF80000FFF80000L),
	Native.toDouble(0xFFFC0000FFFC0000L),
	Native.toDouble(0xFFFE0000FFFE0000L),
	Native.toDouble(0xFFFF0000FFFF0000L),
	Native.toDouble(0xFFFF8000FFFF8000L),
	Native.toDouble(0xFFFFC000FFFFC000L),
	Native.toDouble(0xFFFFE000FFFFE000L),
	Native.toDouble(0xFFFFF000FFFFF000L),
	Native.toDouble(0xFFFFF800FFFFF800L),
	Native.toDouble(0xFFFFFC00FFFFFC00L),
	Native.toDouble(0xFFFFFE00FFFFFE00L),
	Native.toDouble(0xFFFFFF00FFFFFF00L),
	Native.toDouble(0xFFFFFF80FFFFFF80L),
	Native.toDouble(0xFFFFFFC0FFFFFFC0L),
	Native.toDouble(0xFFFFFFE0FFFFFFE0L),
	Native.toDouble(0xFFFFFFF0FFFFFFF0L),
	Native.toDouble(0xFFFFFFF8FFFFFFF8L),
	Native.toDouble(0xFFFFFFFCFFFFFFFCL),
	Native.toDouble(0xFFFFFFFEFFFFFFFEL),
	Native.toDouble(0xFFFFFFFFFFFFFFFFL),
	Native.toDouble(0xFFFFFFFEFFFFFFFEL),
	Native.toDouble(0xFFFFFFFDFFFFFFFDL),
	Native.toDouble(0xFFFFFFFBFFFFFFFBL),
	Native.toDouble(0xFFFFFFF7FFFFFFF7L),
	Native.toDouble(0xFFFFFFEFFFFFFFEFL),
	Native.toDouble(0xFFFFFFDFFFFFFFDFL),
	Native.toDouble(0xFFFFFFBFFFFFFFBFL),
	Native.toDouble(0xFFFFFF7FFFFFFF7FL),
	Native.toDouble(0xFFFFFEFFFFFFFEFFL),
	Native.toDouble(0xFFFFFDFFFFFFFDFFL),
	Native.toDouble(0xFFFFFBFFFFFFFBFFL),
	Native.toDouble(0xFFFFF7FFFFFFF7FFL),
	Native.toDouble(0xFFFFEFFFFFFFEFFFL),
	Native.toDouble(0xFFFFDFFFFFFFDFFFL),
	Native.toDouble(0xFFFFBFFFFFFFBFFFL),
	Native.toDouble(0xFFFF7FFFFFFF7FFFL),
	Native.toDouble(0xFFFEFFFFFFFEFFFFL),
	Native.toDouble(0xFFFDFFFFFFFDFFFFL),
	Native.toDouble(0xFFFBFFFFFFFBFFFFL),
	Native.toDouble(0xFFF7FFFFFFF7FFFFL),
	Native.toDouble(0xFFEFFFFFFFEFFFFFL),
	Native.toDouble(0xFFDFFFFFFFDFFFFFL),
	Native.toDouble(0xFFBFFFFFFFBFFFFFL),
	Native.toDouble(0xFF7FFFFFFF7FFFFFL),
	Native.toDouble(0xFEFFFFFFFEFFFFFFL),
	Native.toDouble(0xFDFFFFFFFDFFFFFFL),
	Native.toDouble(0xFBFFFFFFFBFFFFFFL),
	Native.toDouble(0xF7FFFFFFF7FFFFFFL),
	Native.toDouble(0xEFFFFFFFEFFFFFFFL),
	Native.toDouble(0xDFFFFFFFDFFFFFFFL),
	Native.toDouble(0xBFFFFFFFBFFFFFFFL),
	Native.toDouble(0x7FFFFFFF7FFFFFFFL),
	Native.toDouble(0x3FFFFFFF3FFFFFFFL),
	Native.toDouble(0x1FFFFFFF1FFFFFFFL),
	Native.toDouble(0x0FFFFFFF0FFFFFFFL),
	Native.toDouble(0x07FFFFFF07FFFFFFL),
	Native.toDouble(0x03FFFFFF03FFFFFFL),
	Native.toDouble(0x01FFFFFF01FFFFFFL),
	Native.toDouble(0x00FFFFFF00FFFFFFL),
	Native.toDouble(0x007FFFFF007FFFFFL),
	Native.toDouble(0x003FFFFF003FFFFFL),
	Native.toDouble(0x001FFFFF001FFFFFL),
	Native.toDouble(0x000FFFFF000FFFFFL),
	Native.toDouble(0x0007FFFF0007FFFFL),
	Native.toDouble(0x0003FFFF0003FFFFL),
	Native.toDouble(0x0001FFFF0001FFFFL),
	Native.toDouble(0x0000FFFF0000FFFFL),
	Native.toDouble(0x00007FFF00007FFFL),
	Native.toDouble(0x00003FFF00003FFFL),
	Native.toDouble(0x00001FFF00001FFFL),
	Native.toDouble(0x00000FFF00000FFFL),
	Native.toDouble(0x000007FF000007FFL),
	Native.toDouble(0x000003FF000003FFL),
	Native.toDouble(0x000001FF000001FFL),
	Native.toDouble(0x000000FF000000FFL),
	Native.toDouble(0x0000007F0000007FL),
	Native.toDouble(0x0000003F0000003FL),
	Native.toDouble(0x0000001F0000001FL),
	Native.toDouble(0x0000000F0000000FL),
	Native.toDouble(0x0000000700000007L),
	Native.toDouble(0x0000000300000003L),
	Native.toDouble(0x8000000080000000L),
	Native.toDouble(0x8000000180000001L),
	Native.toDouble(0x8000000280000002L),
	Native.toDouble(0x8000000480000004L),
	Native.toDouble(0x8000000880000008L),
	Native.toDouble(0x8000001080000010L),
	Native.toDouble(0x8000002080000020L),
	Native.toDouble(0x8000004080000040L),
	Native.toDouble(0x8000008080000080L),
	Native.toDouble(0x8000010080000100L),
	Native.toDouble(0x8000020080000200L),
	Native.toDouble(0x8000040080000400L),
	Native.toDouble(0x8000080080000800L),
	Native.toDouble(0x8000100080001000L),
	Native.toDouble(0x8000200080002000L),
	Native.toDouble(0x8000400080004000L),
	Native.toDouble(0x8000800080008000L),
	Native.toDouble(0x8001000080010000L),
	Native.toDouble(0x8002000080020000L),
	Native.toDouble(0x8004000080040000L),
	Native.toDouble(0x8008000080080000L),
	Native.toDouble(0x8010000080100000L),
	Native.toDouble(0x8020000080200000L),
	Native.toDouble(0x8040000080400000L),
	Native.toDouble(0x8080000080800000L),
	Native.toDouble(0x8100000081000000L),
	Native.toDouble(0x8200000082000000L),
	Native.toDouble(0x8400000084000000L),
	Native.toDouble(0x8800000088000000L),
	Native.toDouble(0x9000000090000000L),
	Native.toDouble(0xA0000000A0000000L),
	Native.toDouble(0xC0000000C0000000L),
	Native.toDouble(0x8000000080000000L),
	Native.toDouble(0xBFFFFFFFBFFFFFFFL),
	Native.toDouble(0x9FFFFFFF9FFFFFFFL),
	Native.toDouble(0x8FFFFFFF8FFFFFFFL),
	Native.toDouble(0x87FFFFFF87FFFFFFL),
	Native.toDouble(0x83FFFFFF83FFFFFFL),
	Native.toDouble(0x81FFFFFF81FFFFFFL),
	Native.toDouble(0x80FFFFFF80FFFFFFL),
	Native.toDouble(0x807FFFFF807FFFFFL),
	Native.toDouble(0x803FFFFF803FFFFFL),
	Native.toDouble(0x801FFFFF801FFFFFL),
	Native.toDouble(0x800FFFFF800FFFFFL),
	Native.toDouble(0x8007FFFF8007FFFFL),
	Native.toDouble(0x8003FFFF8003FFFFL),
	Native.toDouble(0x8001FFFF8001FFFFL),
	Native.toDouble(0x8000FFFF8000FFFFL),
	Native.toDouble(0x80007FFF80007FFFL),
	Native.toDouble(0x80003FFF80003FFFL),
	Native.toDouble(0x80001FFF80001FFFL),
	Native.toDouble(0x80000FFF80000FFFL),
	Native.toDouble(0x800007FF800007FFL),
	Native.toDouble(0x800003FF800003FFL),
	Native.toDouble(0x800001FF800001FFL),
	Native.toDouble(0x800000FF800000FFL),
	Native.toDouble(0x8000007F8000007FL),
	Native.toDouble(0x8000003F8000003FL),
	Native.toDouble(0x8000001F8000001FL),
	Native.toDouble(0x8000000F8000000FL),
	Native.toDouble(0x8000000780000007L),
	Native.toDouble(0x8000000380000003L),
	Native.toDouble(0xabfb2c81abfb2c81L),
	Native.toDouble(0xeb21feceeb21feceL),
	Native.toDouble(0x361b388b361b388bL),
	Native.toDouble(0x7621d1447621d144L),
	Native.toDouble(0x838b1c87838b1c87L),
	Native.toDouble(0x4476555a4476555aL),
	Native.toDouble(0xac7cc276ac7cc276L),
	Native.toDouble(0xec4111feec4111feL),
	Native.toDouble(0x83bd1c1b83bd1c1bL),
	Native.toDouble(0x43bd69ec43bd69ecL),
	Native.toDouble(0xb08711b2b08711b2L),
	Native.toDouble(0x70e7fc0170e7fc01L),
	Native.toDouble(0xb6c4c126b6c4c126L),
	Native.toDouble(0xf84a5aedf84a5aedL),
	Native.toDouble(0x9e1b55f99e1b55f9L),
	Native.toDouble(0x5e99b6605e99b660L),
	Native.toDouble(0xbfc02e00bfc02e00L),
	Native.toDouble(0xc2020300c2020300L),
	Native.toDouble(0xc22a0300c22a0300L),
	Native.toDouble(0xc2020300c2020300L),
	Native.toDouble(0xc1780200c1780200L),
	Native.toDouble(0xc1e40400c1e40400L),
	Native.toDouble(0xbfc02c00bfc02c00L),
	Native.toDouble(0xc1180d00c1180d00L),
	Native.toDouble(0xc2df0100c2df0100L),
	Native.toDouble(0xc19c0000c19c0000L),
	Native.toDouble(0xc2950100c2950100L),
	Native.toDouble(0xc2a30100c2a30100L),
	Native.toDouble(0xc2c30100c2c30100L),
	Native.toDouble(0x3effffff3effffffL)
  };

  public static void main (String[] args) {

  System.out.println("Checking values...");
  for (int i=0; i<nums.length; i++) {
	  System.out.println(Long.toHexString(Double.doubleToLongBits(nums[i])));
  }

  System.out.println("Testing double precision floating-point operations...");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      System.out.println(Long.toHexString(Double.doubleToLongBits(nums[i] + nums[k])));
    }
  }
  System.out.println("Testing + finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      System.out.println(Long.toHexString(Double.doubleToLongBits(nums[i] - nums[k])));
    }
  }
  System.out.println("Testing - finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      System.out.println(Long.toHexString(Double.doubleToLongBits(nums[i] * nums[k])));
    }
  }
  System.out.println("Testing * finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      System.out.println(Long.toHexString(Double.doubleToLongBits(nums[i] / nums[k])));
    }
  }
  System.out.println("Testing / finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      System.out.println(Long.toHexString(Double.doubleToLongBits(nums[i] % nums[k])));
    }
  }
  System.out.println("Testing % finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      if (nums[i] < nums[k])
        System.err.println("true");
      else
        System.err.println("false");
    }
  }
  System.out.println("Testing < finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      if (nums[i] > nums[k])
        System.err.println("true");
      else
        System.err.println("false");
    }
  }
  System.out.println("Testing > finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      if (nums[i] <= nums[k])
        System.err.println("true");
      else
        System.err.println("false");
    }
  }
  System.out.println("Testing <= finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      if (nums[i] >= nums[k])
        System.err.println("true");
      else
        System.err.println("false");
    }
  }
  System.out.println("Testing >= finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      if (nums[i] == nums[k])
        System.err.println("true");
      else
        System.err.println("false");
    }
  }
  System.out.println("Testing == finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      if (nums[i] != nums[k])
        System.err.println("true");
      else
        System.err.println("false");
    }
  }
  System.out.println("Testing != finished.");
  for (int i=0; i<nums.length; i++) {
	  System.out.println(Long.toHexString(Double.doubleToLongBits(-(nums[i]))));
  }
  System.out.println("Testing - finished.");
  for (int i=0; i<nums.length; i++) {
	  System.out.println(Long.toHexString(Double.doubleToLongBits((int)(nums[i]))));
  }
  System.out.println("Testing (int) finished.");
  for (int i=0; i<nums.length; i++) {
	  System.out.println(Long.toHexString(Double.doubleToLongBits(Math.round(nums[i]))));
  }
  System.out.println("Testing Math.round finished.");
  System.err.println("All tests finished.");
  }
}
