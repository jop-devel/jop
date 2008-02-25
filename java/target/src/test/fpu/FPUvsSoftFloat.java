/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Stephan Ramberger

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
import com.jopdesign.sys.Const;
import com.jopdesign.sys.SoftFloat;

public class FPUvsSoftFloat {

  static float [] nums = {
   Float.NaN,               // NaN 0x7fc0 0000 == FPU.QNaN
   Float.MAX_VALUE,         // MAX 0x7f7f ffff
   Float.MIN_VALUE,         // MIN 0x0000 0001 == FPU.MIN_VALUE
   Float.NEGATIVE_INFINITY, // N8  0xff80 0000 == FPU.NEG_INF
   Float.POSITIVE_INFINITY, // P8  0x7f80 0000 == FPU.POS_INF
   Native.toFloat(0x00000002),
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
/* -- the numbers below are not part of the max/min/avg test
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
*/
};

/*
 * from: [Floating Point Unit by Jidan Al-Eryani (c) 2006]
 * 
 * 2^{e-127}*1.Fraction denormalized (e>0)
 * 2^{-126}* 0.Fraction normalized   (e=0)
 * Special Numbers (from FPU)
 *  Sign | Exponent(s) | --------- Fraction --------- | --- Hex --- | Value
 *    x  |  xxxx xxxx  | xxx xxxx xxxx xxxx xxxx xxxx | 0xXXXX XXXX |
 *    0  |  0000 0000  | 000 0000 0000 0000 0000 0000 | 0x0000 0000 | +0 (positive zero)
 *    1  |  0000 0000  | 000 0000 0000 0000 0000 0000 | 0x8000 0000 | -0 (negative zero)
 *    0  |  0000 0000  | 000 0000 0000 0000 0000 0001 | 0x0000 0001 | smallest value
 *    0  |  1111 1110  | 111 1111 1111 1111 1111 1111 | 0x7f7f ffff | biggest value
 *    0  |  0111 1111  | 000 0000 0000 0000 0000 0000 | 0x3f80 0000 | +1
 *    x  |  xxxx xxxx  | xxx xxxx xxxx xxxx xxxx xxxx | 0xXXXX XXXX | biggest value
 *    0  |  1111 1111  | 000 0000 0000 0000 0000 0000 | 0x7f80 0000 | + infinity
 *    1  |  1111 1111  | 000 0000 0000 0000 0000 0000 | 0xff80 0000 | - infinity
 *    0  |  1111 1111  | 100 0000 0000 0000 0000 0000 | 0x7fc0 0000 | QNan (quiet NaN) - result
 *    0  |  1111 1111  | 000 0000 0000 0000 0000 0001 | 0x7f80 0001 | SNaN (signaling NaN) - exception
 *    1  |  1111 1111  | 100 0010 0010 0000 0000 1100 | 0xffc2 200c | NaN
 */
/*
 * Rounding modes:
 *  00.) Round to nearest even
 *  01.) Round to Zero
 *  10.) Round Up
 *  11.) Round Down
 */
/*
 * Documentation Error: p4
 * <  where
 * <        f   =({b_{23}}^{-1}+{b_{22}}^{-2}+{b_i}^n+..+{b_0}^{-23}) where b_i^n=1 or 0
 * ----
 * >        f   =(b_{23}*2^{-1}+b_{22}*2^{-2}+b_i*2^n+..+b_0*2^{-23}) where b_i^n=1 or 0
 * <        s   =sign(0 is positive; 1 is negative)
 *
 */
public static int iADDFPUError,iSUBFPUError,iMULFPUError,iDIVFPUError;
public static int iADDJOPError,iSUBJOPError,iMULJOPError,iDIVJOPError;
// medium  execution cycle values
public static int iADDFPUTime,iSUBFPUTime,iMULFPUTime,iDIVFPUTime;
public static int iADDSoftTime,iSUBSoftTime,iMULSoftTime,iDIVSoftTime;
public static int iADDJOPTime,iSUBJOPTime,iMULJOPTime,iDIVJOPTime;
// maximum execution cycle values
public static int iADDFPUMaxTime,iSUBFPUMaxTime,iMULFPUMaxTime,iDIVFPUMaxTime;
public static int iADDSoftMaxTime,iSUBSoftMaxTime,iMULSoftMaxTime,iDIVSoftMaxTime;
public static int iADDJOPMaxTime,iSUBJOPMaxTime,iMULJOPMaxTime,iDIVJOPMaxTime;
// minimum execution cycle values
public static int iADDFPUMinTime,iSUBFPUMinTime,iMULFPUMinTime,iDIVFPUMinTime;
public static int iADDSoftMinTime,iSUBSoftMinTime,iMULSoftMinTime,iDIVSoftMinTime;
public static int iADDJOPMinTime,iSUBJOPMinTime,iMULJOPMinTime,iDIVJOPMinTime;


public static void PrintErrors(String s, int iADDErr, int iSUBErr, int iMULErr, int iDIVErr)
{
  System.out.println(s+ " Errors - ADD: "+iADDErr+" SUB: "+iSUBErr+" MUL: "+iMULErr+" DIV: "+iDIVErr);
}

public static void PrintSummary()
{
  int iRuns = 100;
  int iTimer,iTime,iIndex;

  iTimer = 0;

  System.out.println("Time         MAX: Soft -" +
                     " ADD: "+iADDSoftMaxTime+
                     " SUB: "+iSUBSoftMaxTime+
                     " MUL: "+iMULSoftMaxTime+
                     " DIV: "+iDIVSoftMaxTime);
  System.out.println("Time/instruction: Soft -" +
                     " ADD: "+iADDSoftTime/(nums.length*nums.length)+
                     " SUB: "+iSUBSoftTime/(nums.length*nums.length)+
                     " MUL: "+iMULSoftTime/(nums.length*nums.length)+
                     " DIV: "+iDIVSoftTime/(nums.length*nums.length));
  System.out.println("Time         MIN: Soft -" +
                     " ADD: "+iADDSoftMinTime+
                     " SUB: "+iSUBSoftMinTime+
                     " MUL: "+iMULSoftMinTime+
                     " DIV: "+iDIVSoftMinTime);

  System.out.println("Time         MAX: FPU  -" +
                     " ADD: "+iADDFPUMaxTime+
                     " SUB: "+iSUBFPUMaxTime+
                     " MUL: "+iMULFPUMaxTime+
                     " DIV: "+iDIVFPUMaxTime);
  System.out.println("Time/instruction: FPU  -" +
                     " ADD: "+iADDFPUTime/(nums.length*nums.length)+
                     " SUB: "+iSUBFPUTime/(nums.length*nums.length)+
                     " MUL: "+iMULFPUTime/(nums.length*nums.length)+
                     " DIV: "+iDIVFPUTime/(nums.length*nums.length));
  System.out.println("Time         MIN: FPU  -" +
                     " ADD: "+iADDFPUMinTime+
                     " SUB: "+iSUBFPUMinTime+
                     " MUL: "+iMULFPUMinTime+
                     " DIV: "+iDIVFPUMinTime);

  System.out.println("Time         MAX: JOP  -" +
                     " ADD: "+iADDJOPMaxTime+
                     " SUB: "+iSUBJOPMaxTime+
                     " MUL: "+iMULJOPMaxTime+
                     " DIV: "+iDIVJOPMaxTime);
  System.out.println("Time/instruction: JOP  -" +
                     " ADD: "+iADDJOPTime/(nums.length*nums.length)+
                     " SUB: "+iSUBJOPTime/(nums.length*nums.length)+
                     " MUL: "+iMULJOPTime/(nums.length*nums.length)+
                     " DIV: "+iDIVJOPTime/(nums.length*nums.length));
  System.out.println("Time         MIN: JOP  -" +
                     " ADD: "+iADDJOPMinTime+
                     " SUB: "+iSUBJOPMinTime+
                     " MUL: "+iMULJOPMinTime+
                     " DIV: "+iDIVJOPMinTime);

  for(iIndex=0;iIndex<iRuns;iIndex++)
  {
    iTime = Native.rd(Const.IO_CNT);
    iTimer += Native.rd(Const.IO_CNT)-iTime;
  }
  System.out.println("Timer takes approx: " + iTimer/iRuns);

  iTime = Native.rd(Const.IO_CNT);
  iTimer = Native.rd(Const.IO_CNT)-iTime;
  System.out.println("Single Timer takes approx: " + iTimer);
}

public static int TestValues(String sOP,String sSRC, int i, int k, int iSoft,int iTest,int doOutput)
{
  if ((Float.isNaN(Float.intBitsToFloat(iTest)) != Float.isNaN(Float.intBitsToFloat(iSoft)))
       || ((!Float.isNaN(Float.intBitsToFloat(iTest))) && (iTest != iSoft)))
  {
    if (doOutput!=0)
      System.out.println(sOP+
          " num["+i+"]= "+Integer.toHexString(Float.floatToRawIntBits(nums[i]))+
          " num["+k+"]= "+Integer.toHexString(Float.floatToRawIntBits(nums[k]))+
          " Soft= "+Integer.toHexString(iSoft)+
          " "+sSRC+"= "+Integer.toHexString(iTest));
    return 1;
  }
  else
  {
    return 0;
  }
}

public static void TestADD()
{
  float fA,fB,fJOP;
  int iA,iB,iFPU,iSoft,iJOP;
  int iTimerCnt;
  
  iADDFPUError = 0;
  iADDJOPError = 0;
  iADDSoftTime = 0; iADDSoftMaxTime = 0; iADDSoftMinTime = 0x7FFFFFFF;
  iADDFPUTime  = 0; iADDFPUMaxTime  = 0; iADDFPUMinTime  = 0x7FFFFFFF;
  iADDJOPTime  = 0; iADDJOPMaxTime  = 0; iADDJOPMinTime  = 0x7FFFFFFF;
  iFPU = 0;

  System.out.println("Testing ADD (+) - START -");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      fA = nums[i];
      fB = nums[k];
      iA = Float.floatToRawIntBits(fA);
      iB = Float.floatToRawIntBits(fB);
      
      iTimerCnt = Native.rd(Const.IO_CNT);
      iSoft = SoftFloat.float32_add(iA,iB);
      iTimerCnt = Native.rd(Const.IO_CNT) - iTimerCnt;
      iADDSoftTime += iTimerCnt;
      if (iTimerCnt > iADDSoftMaxTime) iADDSoftMaxTime = iTimerCnt;
      if (iTimerCnt < iADDSoftMinTime) iADDSoftMinTime = iTimerCnt;

      iTimerCnt = Native.rd(Const.IO_CNT);
      Native.wrMem(iA, Const.FPU_A);
      Native.wrMem(iB, Const.FPU_B);
      Native.wrMem(Const.FPU_OP_ADD, Const.FPU_OP);
      iFPU = Native.rdMem(Const.FPU_RES);
      iTimerCnt = Native.rd(Const.IO_CNT) - iTimerCnt;
      iADDFPUTime += iTimerCnt;
      if (iTimerCnt > iADDFPUMaxTime) iADDFPUMaxTime = iTimerCnt;
      if (iTimerCnt < iADDFPUMinTime) iADDFPUMinTime = iTimerCnt;

      iTimerCnt = Native.rd(Const.IO_CNT);
      fJOP = fA + fB;
      iTimerCnt = Native.rd(Const.IO_CNT) - iTimerCnt;
      iADDJOPTime += iTimerCnt;
      if (iTimerCnt > iADDJOPMaxTime) iADDJOPMaxTime = iTimerCnt;
      if (iTimerCnt < iADDJOPMinTime) iADDJOPMinTime = iTimerCnt;

      iJOP = Float.floatToRawIntBits(fJOP);

      iADDFPUError += TestValues("ADD","FPU",i,k,iSoft,iFPU,0);
      iADDJOPError += TestValues("ADD","JOP",i,k,iSoft,Float.floatToRawIntBits(fJOP),0);
    }
  }
  System.out.println("Testing ADD (+) - FINISHED -");
  System.out.println("----------------------------");
}

public static void TestSUB()
{
  float fA,fB,fJOP;
  int iA,iB,iFPU,iSoft,iJOP;
  int iTimerCnt;
  
  iSUBFPUError = 0;
  iSUBJOPError = 0;
  iSUBSoftTime = 0; iSUBSoftMaxTime = 0; iSUBSoftMinTime = 0x7FFFFFFF;
  iSUBFPUTime  = 0; iSUBFPUMaxTime  = 0; iSUBFPUMinTime  = 0x7FFFFFFF;
  iSUBJOPTime  = 0; iSUBJOPMaxTime  = 0; iSUBJOPMinTime  = 0x7FFFFFFF;
  iFPU = 0;

  System.out.println("Testing SUB (-) - START -");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      fA = nums[i];
      fB = nums[k];
      iA = Float.floatToRawIntBits(fA);
      iB = Float.floatToRawIntBits(fB);
 
      iTimerCnt = Native.rd(Const.IO_CNT);
      iSoft = SoftFloat.float32_sub(iA,iB);
      iTimerCnt = Native.rd(Const.IO_CNT) - iTimerCnt;
      iSUBSoftTime += iTimerCnt;
      if (iTimerCnt > iSUBSoftMaxTime) iSUBSoftMaxTime = iTimerCnt;
      if (iTimerCnt < iSUBSoftMinTime) iSUBSoftMinTime = iTimerCnt;
 
      iTimerCnt = Native.rd(Const.IO_CNT);
      Native.wrMem(iA, Const.FPU_A);
      Native.wrMem(iB, Const.FPU_B);
      Native.wrMem(Const.FPU_OP_SUB, Const.FPU_OP);
      iFPU = Native.rdMem(Const.FPU_RES);
      iTimerCnt = Native.rd(Const.IO_CNT) - iTimerCnt;
      iSUBFPUTime += iTimerCnt;
      if (iTimerCnt > iSUBFPUMaxTime) iSUBFPUMaxTime = iTimerCnt;
      if (iTimerCnt < iSUBFPUMinTime) iSUBFPUMinTime = iTimerCnt;

      iTimerCnt = Native.rd(Const.IO_CNT);
      fJOP = fA - fB;
      iTimerCnt = Native.rd(Const.IO_CNT) - iTimerCnt;
      iSUBJOPTime += iTimerCnt;
      if (iTimerCnt > iSUBJOPMaxTime) iSUBJOPMaxTime = iTimerCnt;
      if (iTimerCnt < iSUBJOPMinTime) iSUBJOPMinTime = iTimerCnt;

      iJOP = Float.floatToRawIntBits(fJOP);
        
      iSUBFPUError += TestValues("SUB","FPU",i,k,iSoft,iFPU,0);
      iSUBJOPError += TestValues("SUB","JOP",i,k,iSoft,Float.floatToRawIntBits(fJOP),0);
    }
  }
  System.out.println("Testing SUB (-) - FINISHED -");
  System.out.println("----------------------------");
}

public static void TestMUL()
{
  float fA,fB,fJOP;
  int iA,iB,iFPU,iSoft,iJOP;
  int iTimerCnt;
  
  iMULFPUError = 0;
  iMULJOPError = 0;
  iMULSoftTime = 0; iMULSoftMaxTime = 0; iMULSoftMinTime = 0x7FFFFFFF;
  iMULFPUTime  = 0; iMULFPUMaxTime  = 0; iMULFPUMinTime  = 0x7FFFFFFF;
  iMULJOPTime  = 0; iMULJOPMaxTime  = 0; iMULJOPMinTime  = 0x7FFFFFFF;
  iFPU = 0;

  System.out.println("Testing MUL (*) - START -");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      fA = nums[i];
      fB = nums[k];
      iA = Float.floatToRawIntBits(fA);
      iB = Float.floatToRawIntBits(fB);

      iTimerCnt = Native.rd(Const.IO_CNT);
      iSoft = SoftFloat.float32_mul(iA,iB);
      iTimerCnt = Native.rd(Const.IO_CNT) - iTimerCnt;
      iMULSoftTime += iTimerCnt;
      if (iTimerCnt > iMULSoftMaxTime) iMULSoftMaxTime = iTimerCnt;
      if (iTimerCnt < iMULSoftMinTime) iMULSoftMinTime = iTimerCnt;

      iTimerCnt = Native.rd(Const.IO_CNT);
      Native.wrMem(iA, Const.FPU_A);
      Native.wrMem(iB, Const.FPU_B);
      Native.wrMem(Const.FPU_OP_MUL, Const.FPU_OP);
      iFPU = Native.rdMem(Const.FPU_RES);
      iTimerCnt = Native.rd(Const.IO_CNT) - iTimerCnt;
      iMULFPUTime += iTimerCnt;
      if (iTimerCnt > iMULFPUMaxTime) iMULFPUMaxTime = iTimerCnt;
      if (iTimerCnt < iMULFPUMinTime) iMULFPUMinTime = iTimerCnt;

      iTimerCnt = Native.rd(Const.IO_CNT);
      fJOP = fA * fB;
      iTimerCnt = Native.rd(Const.IO_CNT) - iTimerCnt;
      iMULJOPTime += iTimerCnt;
      if (iTimerCnt > iMULJOPMaxTime) iMULJOPMaxTime = iTimerCnt;
      if (iTimerCnt < iMULJOPMinTime) iMULJOPMinTime = iTimerCnt;

      iJOP = Float.floatToRawIntBits(fJOP);

      iMULFPUError += TestValues("MUL","FPU",i,k,iSoft,iFPU,0);
      iMULJOPError += TestValues("MUL","JOP",i,k,iSoft,Float.floatToRawIntBits(fJOP),0);
    }
  }
  System.out.println("Testing MUL (*) - FINISHED -");
  System.out.println("----------------------------");
}

public static void TestDIV()
{
  float fA,fB,fJOP;
  int iA,iB,iFPU,iSoft,iJOP;
  int iTimerCnt;
  
  iDIVFPUError = 0;
  iDIVJOPError = 0;
  iDIVSoftTime = 0; iDIVSoftMaxTime = 0; iDIVSoftMinTime = 0x7FFFFFFF;
  iDIVFPUTime  = 0; iDIVFPUMaxTime  = 0; iDIVFPUMinTime  = 0x7FFFFFFF;
  iDIVJOPTime  = 0; iDIVJOPMaxTime  = 0; iDIVJOPMinTime  = 0x7FFFFFFF;
  iFPU = 0;

  System.out.println("Testing DIV (/) - START -");
  for (int i=0; i<nums.length; i++) {
    for (int k=0; k<nums.length; k++) {
      fA = nums[i];
      fB = nums[k];
      iA = Float.floatToRawIntBits(fA);
      iB = Float.floatToRawIntBits(fB);

      iTimerCnt = Native.rd(Const.IO_CNT);
      iSoft = SoftFloat.float32_div(iA,iB);
      iTimerCnt = Native.rd(Const.IO_CNT) - iTimerCnt;
      iDIVSoftTime += iTimerCnt;
      if (iTimerCnt > iDIVSoftMaxTime) iDIVSoftMaxTime = iTimerCnt;
      if (iTimerCnt < iDIVSoftMinTime) iDIVSoftMinTime = iTimerCnt;

      iTimerCnt = Native.rd(Const.IO_CNT);
      Native.wrMem(iA, Const.FPU_A);
      Native.wrMem(iB, Const.FPU_B);
      Native.wrMem(Const.FPU_OP_DIV, Const.FPU_OP);
      iFPU = Native.rdMem(Const.FPU_RES);
      iTimerCnt = Native.rd(Const.IO_CNT) - iTimerCnt;
      iDIVFPUTime += iTimerCnt;
      if (iTimerCnt > iDIVFPUMaxTime) iDIVFPUMaxTime = iTimerCnt;
      if (iTimerCnt < iDIVFPUMinTime) iDIVFPUMinTime = iTimerCnt;

      iTimerCnt = Native.rd(Const.IO_CNT);
      fJOP = fA / fB;
      iTimerCnt = Native.rd(Const.IO_CNT) - iTimerCnt;
      iDIVJOPTime += iTimerCnt;
      if (iTimerCnt > iDIVJOPMaxTime) iDIVJOPMaxTime = iTimerCnt;
      if (iTimerCnt < iDIVJOPMinTime) iDIVJOPMinTime = iTimerCnt;

      iJOP = Float.floatToRawIntBits(fJOP);
 
      iDIVFPUError += TestValues("DIV","FPU",i,k,iSoft,iFPU,0);
      iDIVJOPError += TestValues("DIV","JOP",i,k,iSoft,Float.floatToRawIntBits(fJOP),0);
    }
  }
  System.out.println("Testing DIV (/) - FINISHED -");
  System.out.println("----------------------------");
}

public static void main (String[] args) {
  
  System.out.println("===============================================================");
  System.out.println("Testing floating-point operations...");

  TestADD();
  TestSUB();
  TestMUL();
  TestDIV();
 
  PrintErrors("FPU",iADDFPUError,iSUBFPUError,iMULFPUError,iDIVFPUError);
  PrintErrors("JOP",iADDJOPError,iSUBJOPError,iMULJOPError,iDIVJOPError);
  PrintSummary();
  }
}
