package wcet.dsvmfp.model.smo.kernel;

import util.Dbg;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * @author rup & ms
 */
public class KFP {

  // mask to test a resolution in the dot kernel functions
  // 0xFF:1111 1111
  // 0x3F:0011 1111
  // 0x0F:0000 1111
  // 0x03:0000 0011
  // 0x00:0000 0000
  // 0xC0:1100 0000
  // 0xF0:1111 0000
  // 0xFC:1111 1100
  // 0xFF:1111 1111

  // 16:16 0xFFFFFFFF
  // 12:12 0x0FFFFFF0
  // 8:8 0x00FFFF00
  // 4:4 0x000FF000
  final static int mask = 0xffffffff;

  public final static int DOTKERNEL = 1;

  public final static int GAUSSIANKERNEL = 2;

  // All decimal numbers are FP
  private static int[][] data;// data

  private static int m; // number of rows (observations)

  private static int n; // number of columns (dimensions)

  private static int[] x; // FP test data

  private static int[] kernelCache; // kernelDot(i,i)

  private static int sigma2; // sigma squared

  private static int gaussConst;// -0.5/(sigma*sigma)

  private static int kernelType;// GAUSSIANKERNEL or DOTKERNEL;

  // A few variables to avoid too deep calls (and stack overflows)
  private static int tmp1;

  private static int tmp2;

  // Gauss k(data[i1],data[i2])
  private static int kernelGauss(int i1, int i2) {
//    Dbg.wr("K-SP: ", Native.getSP());
    tmp1 = FP.mul(FP.TWO, kernelDot(i1, i2));
//    Dbg.wr("K-SP1: ", Native.getSP());
    tmp2 = FP.sub(FP.add(kernelCache[i1], kernelCache[i2]), tmp1);
//    Dbg.wr("K-SP2: ", Native.getSP());
    tmp1 = FP.mul(gaussConst, tmp2);
//    Dbg.wr("K-SP3: ", Native.getSP());
//    Dbg.wr("tmp1: ", tmp1);
    tmp1 = FP.exp(tmp1);
//    Dbg.wr("K-SP4: ", Native.getSP());
//    Dbg.wr("tmp1: ", tmp1);
    return tmp1;
  }

  // Gauss k(data[i1],x)
  private static int kernelGaussX(int i1) {
    return FP.exp(FP.mul(gaussConst, FP.sub(FP.add(kernelCache[i1],
        kernelDotXX()), FP.mul(FP.TWO, kernelDotX(i1)))));
  }

  /*
   * this is the original code and takes 4-5us for MAC unit and 8us for the SW
   * version. SW: 485 cycles HW: 273 cycles BUT: this was measured with n=1 BTW:
   * The SW version is invoked more often - does learning take longer with the
   * less resolution multiplication. A point in the paper!
   *  // Dot k(data[i1],data[i2]) private static int kernelDot(int i1, int i2) {
   * int r = 0; int t;
   *
   * t = Native.rd(Const.IO_CNT); if (USEMAC) { for (int j = 0; j < n; j++) {
   * Native.wrMem(data[i1][j], Const.IO_MAC_A); Native.wrMem(data[i2][j],
   * Const.IO_MAC_B); } r = Native.rdMem(Const.IO_MAC_A);
   * Native.rdMem(Const.IO_MAC_B); } else { for (int j = 0; j < n; j++) { r =
   * FP.add(r, FP.mul(data[i1][j], data[i2][j])); } } t =
   * Native.rd(Const.IO_CNT)-t; System.out.print("time in cycles: ");
   * System.out.println(t); return r; }
   */

  /*
   * Optimizations: Baseline: HW 273 cycles j as local 2: 269 n as local: 244
   * avoid if, r=0: 244 (not part of measurement) single array access: 146 BTW:
   * These numbers are with n=1!!! now n=2: Baseline: HW: 499, SW: 939 HW: same
   * opt. as above: 258 HW: only n, no j: 244
   *
   * SW: same as above: 686 SW: inline add: 486 SW: inline >>8 mul: 286 SW:
   * 'correct' mul: 650
   */

  // this and an optimized version of kernelDotX should be inlined!
  private static int kernelDot(int i1, int i2) {
    int r;
    int n = KFP.n;

//    int t;
    //t = Native.rd(Const.IO_CNT);
    int a[] = data[i1];
    int b[] = data[i2];
    // t = Native.rd(Const.IO_CNT);

    // long lr = 0;
    // HW version
//    while (n != 0) {
//      n = n - 1;
//      // lr += ((long) a[n]) * ((long) b[n]);
//      Native.wrMem(a[n], Const.IO_MAC_A);
//      Native.wrMem(b[n], Const.IO_MAC_B);
//    }
//    r = a[0]; // we need this time for the MAC to finish!
//    r = Native.rdMem(Const.IO_MAC_A);

    // check the MAC unit
    // if (r != ((int) (lr>>16))) {
    // System.out.print("result=");
    // System.out.println(r);
    // System.out.print("long result=");
    // System.out.println((int) (lr>>16));
    // }

    // SW version
    r = 0;
    while (n != 0) {
      n = n - 1;
      // the mask is for experimenting with #sv and test err vs. resolution
      //r += ((a[n] & mask) >> 8) * ((b[n] & mask) >> 8);
      r += (a[n] >> 8) * (b[n] >> 8);
    }


    // SW 2
    // // int f1 = a[n]; int f2 = b[n];
    // // r += ((f1 >> 16) * (f2 >> 16)) << 16; //AH*BH
    // // r += ((f1 >> 16) * (f2 & 0x0000FFFF)) ; //AH*BL
    // // r += ((f1 & 0x0000FFFF) * (f2 >> 16)) ; //AL*BH
    // // r += ((f1 & 0x0000FFFF) * (f2 & 0x0000FFFF)) >> 16; //AL*BL
    //
    // }

    // t = Native.rd(Const.IO_CNT) - t;
    // System.out.print("time in cycles: ");
    // System.out.println(t);
    return r;
  }

  // Dot k(data[i1],x)
  public static int kernelDotX(int i1) {
    int r;
    int n = KFP.n;
    // int t = Native.rd(Const.IO_CNT);
    int a[] = data[i1];

    // HW version
     while(n!=0){
       n=n-1;
       Native.wrMem(a[n], Const.IO_MAC_A);
       Native.wrMem(x[n], Const.IO_MAC_B);
     }
     r = a[0]; // we need this time for the MAC to finish!
     r = Native.rdMem(Const.IO_MAC_A);

    // SW version
//    r = 0;
//    while (n != 0) {
//      n = n - 1;
//      //r += ((a[n] & mask) >> 8) * ((x[n] & mask) >> 8);
//      r += (a[n] >> 8) * (x[n] >> 8);
//    }

    // t = Native.rd(Const.IO_CNT) - t;
    // System.out.print("dotX time in cycles: ");
    // System.out.println(t);
    // System.exit(-1);

    return r;
  }

  // Dot k(x,x)
  private static int kernelDotXX() {
    int r = 0;
    for (int j = 0; j < n; j++) {
      r = FP.add(r, FP.mul(x[j], x[j]));
    }
    return r;
  }

  // TRAINING //

  // kernelType: k(data[i1],data[i1]
  public static int kernel(int i1, int i2) {
    if (kernelType == DOTKERNEL)
      return kernelDot(i1, i2);

    if (kernelType == GAUSSIANKERNEL)
      return kernelGauss(i1, i2);

    return -1;
  }

  // Sum over i k(data[i1],data[i])
  public static int kernelArray(int i1) {
    int s = 0;
    for (int i = 0; i < m; i++) {
      s = FP.add(s, kernel(i1, i));
    }
    return s;
  }

  // TESTING //

  // kernelType k(data[i1],x)
  public static int kernelX(int i1) {
    if (kernelType == DOTKERNEL)
      return kernelDotX(i1);

    if (kernelType == GAUSSIANKERNEL)
      return kernelGaussX(i1);

    return -1;
  }

  // Sum over i kernelType k(data[i],x)
  public static int kernelXArray() {
    int s = 0;
    for (int i = 0; i < m; i++) {
      s = FP.add(s, kernelX(i));
    }
    return s;
  }

  // SETUP //

  public static void setData(int[][] data) {
    KFP.data = data;
    m = data.length;
    n = data[0].length;
    kernelCache = new int[m];
    for (int i = 0; i < m; i++) {
      kernelCache[i] = kernelDot(i, i);
    }
  }

  public static void setX(int[] x) {
    KFP.x = x;
  }

  public static void setKernelType(int kernelType) {
    KFP.kernelType = kernelType;
  }

  public static void setSigma2(int sigma2) {
    KFP.sigma2 = sigma2;
    gaussConst = FP.div(-FP.HALF, sigma2);
  }
}