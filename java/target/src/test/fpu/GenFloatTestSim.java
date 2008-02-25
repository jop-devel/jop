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

public class GenFloatTestSim {

  static float [] nums = { Float.NaN,
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
	
	// we have no invokestatic in Startup.interpret()
	// use special native function
	Native.toFloat(0x00800000),
	Native.toFloat(0x33800000),
	Native.toFloat(0x3E800000),
	Native.toFloat(0x3F000000),
	Native.toFloat(0x3F800000),
	Native.toFloat(0x40000000),
	Native.toFloat(0x40800000),
	Native.toFloat(0x4B800000),
	Native.toFloat(0x7F000000),
	Native.toFloat(0x80800000),
	Native.toFloat(0xB3800000),
	Native.toFloat(0xBE800000),
	Native.toFloat(0xBF000000),
	Native.toFloat(0xBF800000),
	Native.toFloat(0xC0000000),
	Native.toFloat(0xC0800000),
	Native.toFloat(0xCB800000),
	Native.toFloat(0xFE800000),
	Native.toFloat(0x00000000),
	Native.toFloat(0x00000001),
	Native.toFloat(0x00000002),
	Native.toFloat(0x00000004),
	Native.toFloat(0x00000008),
	Native.toFloat(0x00000010),
	Native.toFloat(0x00000020),
	Native.toFloat(0x00000040),
	Native.toFloat(0x00000080),
	Native.toFloat(0x00000100),
	Native.toFloat(0x00000200),
	Native.toFloat(0x00000400),
	Native.toFloat(0x00000800),
	Native.toFloat(0x00001000),
	Native.toFloat(0x00002000),
	Native.toFloat(0x00004000),
	Native.toFloat(0x00008000),
	Native.toFloat(0x00010000),
	Native.toFloat(0x00020000),
	Native.toFloat(0x00040000),
	Native.toFloat(0x00080000),
	Native.toFloat(0x00100000),
	Native.toFloat(0x00200000),
	Native.toFloat(0x00400000),
	Native.toFloat(0x00800000),
	Native.toFloat(0x01000000),
	Native.toFloat(0x02000000),
	Native.toFloat(0x04000000),
	Native.toFloat(0x08000000),
	Native.toFloat(0x10000000),
	Native.toFloat(0x20000000),
	Native.toFloat(0x40000000),
	Native.toFloat(0x80000000),
	Native.toFloat(0xC0000000),
	Native.toFloat(0xE0000000),
	Native.toFloat(0xF0000000),
	Native.toFloat(0xF8000000),
	Native.toFloat(0xFC000000),
	Native.toFloat(0xFE000000),
	Native.toFloat(0xFF000000),
	Native.toFloat(0xFF800000),
	Native.toFloat(0xFFC00000),
	Native.toFloat(0xFFE00000),
	Native.toFloat(0xFFF00000),
	Native.toFloat(0xFFF80000),
	Native.toFloat(0xFFFC0000),
	Native.toFloat(0xFFFE0000),
	Native.toFloat(0xFFFF0000),
	Native.toFloat(0xFFFF8000),
	Native.toFloat(0xFFFFC000),
	Native.toFloat(0xFFFFE000),
	Native.toFloat(0xFFFFF000),
	Native.toFloat(0xFFFFF800),
	Native.toFloat(0xFFFFFC00),
	Native.toFloat(0xFFFFFE00),
	Native.toFloat(0xFFFFFF00),
	Native.toFloat(0xFFFFFF80),
	Native.toFloat(0xFFFFFFC0),
	Native.toFloat(0xFFFFFFE0),
	Native.toFloat(0xFFFFFFF0),
	Native.toFloat(0xFFFFFFF8),
	Native.toFloat(0xFFFFFFFC),
	Native.toFloat(0xFFFFFFFE),
	Native.toFloat(0xFFFFFFFF),
	Native.toFloat(0xFFFFFFFE),
	Native.toFloat(0xFFFFFFFD),
	Native.toFloat(0xFFFFFFFB),
	Native.toFloat(0xFFFFFFF7),
	Native.toFloat(0xFFFFFFEF),
	Native.toFloat(0xFFFFFFDF),
	Native.toFloat(0xFFFFFFBF),
	Native.toFloat(0xFFFFFF7F),
	Native.toFloat(0xFFFFFEFF),
	Native.toFloat(0xFFFFFDFF),
	Native.toFloat(0xFFFFFBFF),
	Native.toFloat(0xFFFFF7FF),
	Native.toFloat(0xFFFFEFFF),
	Native.toFloat(0xFFFFDFFF),
	Native.toFloat(0xFFFFBFFF),
	Native.toFloat(0xFFFF7FFF),
	Native.toFloat(0xFFFEFFFF),
	Native.toFloat(0xFFFDFFFF),
	Native.toFloat(0xFFFBFFFF),
	Native.toFloat(0xFFF7FFFF),
	Native.toFloat(0xFFEFFFFF),
	Native.toFloat(0xFFDFFFFF),
	Native.toFloat(0xFFBFFFFF),
	Native.toFloat(0xFF7FFFFF),
	Native.toFloat(0xFEFFFFFF),
	Native.toFloat(0xFDFFFFFF),
	Native.toFloat(0xFBFFFFFF),
	Native.toFloat(0xF7FFFFFF),
	Native.toFloat(0xEFFFFFFF),
	Native.toFloat(0xDFFFFFFF),
	Native.toFloat(0xBFFFFFFF),
	Native.toFloat(0x7FFFFFFF),
	Native.toFloat(0x3FFFFFFF),
	Native.toFloat(0x1FFFFFFF),
	Native.toFloat(0x0FFFFFFF),
	Native.toFloat(0x07FFFFFF),
	Native.toFloat(0x03FFFFFF),
	Native.toFloat(0x01FFFFFF),
	Native.toFloat(0x00FFFFFF),
	Native.toFloat(0x007FFFFF),
	Native.toFloat(0x003FFFFF),
	Native.toFloat(0x001FFFFF),
	Native.toFloat(0x000FFFFF),
	Native.toFloat(0x0007FFFF),
	Native.toFloat(0x0003FFFF),
	Native.toFloat(0x0001FFFF),
	Native.toFloat(0x0000FFFF),
	Native.toFloat(0x00007FFF),
	Native.toFloat(0x00003FFF),
	Native.toFloat(0x00001FFF),
	Native.toFloat(0x00000FFF),
	Native.toFloat(0x000007FF),
	Native.toFloat(0x000003FF),
	Native.toFloat(0x000001FF),
	Native.toFloat(0x000000FF),
	Native.toFloat(0x0000007F),
	Native.toFloat(0x0000003F),
	Native.toFloat(0x0000001F),
	Native.toFloat(0x0000000F),
	Native.toFloat(0x00000007),
	Native.toFloat(0x00000003),
	Native.toFloat(0x80000000),
	Native.toFloat(0x80000001),
	Native.toFloat(0x80000002),
	Native.toFloat(0x80000004),
	Native.toFloat(0x80000008),
	Native.toFloat(0x80000010),
	Native.toFloat(0x80000020),
	Native.toFloat(0x80000040),
	Native.toFloat(0x80000080),
	Native.toFloat(0x80000100),
	Native.toFloat(0x80000200),
	Native.toFloat(0x80000400),
	Native.toFloat(0x80000800),
	Native.toFloat(0x80001000),
	Native.toFloat(0x80002000),
	Native.toFloat(0x80004000),
	Native.toFloat(0x80008000),
	Native.toFloat(0x80010000),
	Native.toFloat(0x80020000),
	Native.toFloat(0x80040000),
	Native.toFloat(0x80080000),
	Native.toFloat(0x80100000),
	Native.toFloat(0x80200000),
	Native.toFloat(0x80400000),
	Native.toFloat(0x80800000),
	Native.toFloat(0x81000000),
	Native.toFloat(0x82000000),
	Native.toFloat(0x84000000),
	Native.toFloat(0x88000000),
	Native.toFloat(0x90000000),
	Native.toFloat(0xA0000000),
	Native.toFloat(0xC0000000),
	Native.toFloat(0x80000000),
	Native.toFloat(0xBFFFFFFF),
	Native.toFloat(0x9FFFFFFF),
	Native.toFloat(0x8FFFFFFF),
	Native.toFloat(0x87FFFFFF),
	Native.toFloat(0x83FFFFFF),
	Native.toFloat(0x81FFFFFF),
	Native.toFloat(0x80FFFFFF),
	Native.toFloat(0x807FFFFF),
	Native.toFloat(0x803FFFFF),
	Native.toFloat(0x801FFFFF),
	Native.toFloat(0x800FFFFF),
	Native.toFloat(0x8007FFFF),
	Native.toFloat(0x8003FFFF),
	Native.toFloat(0x8001FFFF),
	Native.toFloat(0x8000FFFF),
	Native.toFloat(0x80007FFF),
	Native.toFloat(0x80003FFF),
	Native.toFloat(0x80001FFF),
	Native.toFloat(0x80000FFF),
	Native.toFloat(0x800007FF),
	Native.toFloat(0x800003FF),
	Native.toFloat(0x800001FF),
	Native.toFloat(0x800000FF),
	Native.toFloat(0x8000007F),
	Native.toFloat(0x8000003F),
	Native.toFloat(0x8000001F),
	Native.toFloat(0x8000000F),
	Native.toFloat(0x80000007),
	Native.toFloat(0x80000003),
	Native.toFloat(0xabfb2c81),
	Native.toFloat(0xeb21fece),
	Native.toFloat(0x361b388b),
	Native.toFloat(0x7621d144),
	Native.toFloat(0x838b1c87),
	Native.toFloat(0x4476555a),
	Native.toFloat(0xac7cc276),
	Native.toFloat(0xec4111fe),
	Native.toFloat(0x83bd1c1b),
	Native.toFloat(0x43bd69ec),
	Native.toFloat(0xb08711b2),
	Native.toFloat(0x70e7fc01),
	Native.toFloat(0xb6c4c126),
	Native.toFloat(0xf84a5aed),
	Native.toFloat(0x9e1b55f9),
	Native.toFloat(0x5e99b660),
	Native.toFloat(0xbfc02e00),
	Native.toFloat(0xc2020300),
	Native.toFloat(0xc22a0300),
	Native.toFloat(0xc2020300),
	Native.toFloat(0xc1780200),
	Native.toFloat(0xc1e40400),
	Native.toFloat(0xbfc02c00),
	Native.toFloat(0xc1180d00),
	Native.toFloat(0xc2df0100),
	Native.toFloat(0xc19c0000),
	Native.toFloat(0xc2950100),
	Native.toFloat(0xc2a30100),
	Native.toFloat(0xc2c30100),
	Native.toFloat(0x3effffff)  
/*
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
	Float.intBitsToFloat(0x3effffff)  
*/

	};

  public static void main (String[] args) {

  System.out.println("Testing floating-point operations...");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      System.out.println(Integer.toHexString(Float.floatToIntBits(nums[i] + nums[k])));
    }
  }
  System.out.println("Testing + finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      System.out.println(Integer.toHexString(Float.floatToIntBits(nums[i] - nums[k])));
    }
  }
  System.out.println("Testing - finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      System.out.println(Integer.toHexString(Float.floatToIntBits(nums[i] * nums[k])));
    }
  }
  System.out.println("Testing * finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      System.out.println(Integer.toHexString(Float.floatToIntBits(nums[i] / nums[k])));
    }
  }
  System.out.println("Testing / finished.");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      System.out.println(Integer.toHexString(Float.floatToIntBits(nums[i] % nums[k])));
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
    System.out.println(Integer.toHexString(Float.floatToIntBits(-(nums[i]))));
  }
  System.out.println("Testing - finished.");
  for (int i=0; i<nums.length; i++) {
    System.out.println(Integer.toHexString(Float.floatToIntBits((int)(nums[i]))));
  }
  System.out.println("Testing (int) finished.");
  for (int i=0; i<nums.length; i++) {
    System.out.println(Integer.toHexString(Float.floatToIntBits(Math.round(nums[i]))));
  }
  System.out.println("Testing Math.round finished.");
    System.err.println("All tests finished.");
  }
}
