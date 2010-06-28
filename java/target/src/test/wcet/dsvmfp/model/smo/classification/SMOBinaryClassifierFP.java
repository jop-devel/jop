package wcet.dsvmfp.model.smo.classification;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import wcet.dsvmfp.model.smo.kernel.FP;
import wcet.dsvmfp.model.smo.kernel.KFP;

// Fixed point guide
// 1. comparison ok without FP
// 2. ==0 ok
// 3. postfix int vars with "_fp"
// 4. postfix method name with "FP" if it returns an int_fp

/**
 * Class SMOBinaryClassifier, which is the model for performing a binary
 * classification task. It will receive an input set to train it self. After
 * training of the model it will retire the datapoints that are not part of the
 * model. Only the support vectors are kept. The original index of the data
 * points are kept. <br>
 * Internal responsibilities: 1. Provide support for binary classification.<br>
 * External responsibilities: 1. Provide the classification of the training
 * points. 2. Provide the classification of test points.
 */
public class SMOBinaryClassifierFP{

	final static boolean PRINT = false;

  /** Number of lagrange multipliers in deployed RT model */
  public final static int ALPHA_RT = 5;

  static boolean info;

  static public boolean printSMOInfo;

  /** The F cache. */
  static public int[] fcache_fp;

  /** Kernel cache i==i kernels. */
  static public int[] kernelCache_fp;

  /**
   * The index set. The value is an int that determines if the point belongs to
   * index set. It is updated after each <code>takestep</code>. <br>
   * 0: 0<a<C <br>
   * 1: y_fp=+1,a=0 <br>
   * 2: y_fp=-1,a=C <br>
   * 3: y_fp=+1,a=C <br>
   * 4: y_fp=-1,a=0
   */
  static public int[] findexset;

  /**
   * A full length array that has holds the position of the point in findexset0.
   * Only entries that ==0 in findexset are valid.
   */
  static public int[] findexset0pos;

  /**
   * Index set 0: 0 <a <C. The length of the set is changed dynamically to match
   * the number of NB points.
   */
  static public int[] findexset0;

  /**
   * Length of findexset0.
   */
  static public int findexset0Size;

  /** The [m] Lagrange multipliers. */
  static public int[] alpha_fp;

  /** Sorted to have largest alpha index on [0] and so on.*/
  static public int[] alpha_index_sorted;

  /** The target vector of {-1,+1}. */
  static public int[] y_fp;

  /** The data vector [rows][columns]. */
  static public int[][] data_fp;

  /** The high bound. */
  static public int c_fp;

  /** The error tolerance. */
  static public int tol_fp;

  /** The error tolerance, that is used for KKT violation checks. */
  static public int eps_fp;

  /**
   * Accuracy used when dividing the alpha_fp values into sets. For example: if
   * (a <alphaTol_fp) then a = 0
   */
  static public int alphaTol_fp;

  /** Parameters used for keeping the indices of the low and high Fs. */
  static public int b_up_fp;

  static public int b_low_fp;

  static public int i_low, i_up;

  /** The bias_fp. */
  static public int bias_fp;

  static public int i1, i2;

  /**
   * The number of training points. It is declared final to avoid
   * synchronization problems.
   */
  public static int m;

  /** The input space dimensinality. */
  static public int n;

  static public boolean takeStepFlag;

  static public boolean takeStepResult;

  // ////////////Performance Variables////////////////////
  static public int takeStepCount;

  static public int numChanged;

  static public boolean examineAll;

  static public int loop;


  /**
   * Method mainRoutine, which estimates the SVM parameters. The parameters are
   * intialized before the training of the SVM is conducted.
   *
   * @return true if the trainng went well
   */
  static public boolean mainRoutine() {
    int time = 0;
    if (PRINT) System.out.println("SMO.mainroutine");
    time = Native.rd(Const.IO_US_CNT);
    //for(int i=0;i<1000000;i++);
    info = false;
    // FP.init();
    // The number of updated that significantly changed the
    // Lagrange multipliers
    numChanged = 0;
    // Indicates if all examples has been tested
    examineAll = true;
    // Reset alpha_fp array , errorarray , b, and C
    takeStepCount = 0;
    initParams();

    // System.out.println("main:bef manual call");
    // printSMOInfo = true;
    // while(printSMOInfo)
    // smo.waitForNextPeriod();
    // i1 = 0;
    // i2 = 1;
    // takeStep();
    // System.out.println("main:aft manual call");
    // while(printSMOInfo)
    // smo.waitForNextPeriod();
    // while(i1>-10); //foreverloop
    loop = 0;
    while (numChanged > 0 || examineAll) { // @WCA loop=2
      // while (loop>=0) { //temp debug forever loop
//      System.out.print("Starting loop=");
//      System.out.println(loop);
      loop++;
//      System.out.print("numChanged=");
//      System.out.println(numChanged);
//      if (examineAll)
//        System.out.println("examineAll=true");
//      else
//        System.out.println("examineAll=false");
      for (int i = 0; i < 100000; i++) // @WCA loop=2
        ; // slow it down to print
      numChanged = 0;
      if (examineAll) {
        for (i2 = 0; i2 < m; i2++) { // @WCA loop=2
          if (examineExample()) {
            numChanged++;
          }
        }
      } else {
        // Inner loop success
        numChanged = innerLoop();
        //numChanged = 0; // TODO: remove
      }
      if (examineAll) {
        examineAll = false;
      } else if (numChanged == 0) {
        examineAll = true;
      }
      //break;
    }
    if (PRINT) System.out.println("SMO.mainroutine.trained");
    sortAlphaIndex();
    time = Native.rd(Const.IO_US_CNT)-time;
    if (PRINT) {
        System.out.println("Done!");
        smoInfo();
        System.out.print("total time:");
        System.out.println(time);
    }


    return true;
  }

  // METHODS////////////////////////////////////////
  /**
   * Method examineExample, which will take a step using a number of heuristics.
   * The points coming into this method is from the outer in the SMO main
   * routine: first choice heuristic
   *
   * @param i2 -
   *          zero based index of second point to classify, which is chosen by
   *          the outer loop in smo
   * @return true if it was possible to take a step
   */
  static boolean examineExample() {
     i1 = -1;

    // Target
    // double y2_fp;
    // Lagrange multiplier
    // double alph2_fp;
    // Index of first point
    // Functional output
    // double f2_fp;
    // Assignment to local variables

    int f2_fp;

    {
      // System.out.println("eaa");
      f2_fp = getfFP(i2);
      // System.out.println("ea1");
      fcache_fp[i2] = f2_fp;
      // System.out.println("ea2");
      // Update (b_low_fp, i_low), (b_up_fp, i_up) using (F2,i2)
      if ((findexset[i2] == 1 || findexset[i2] == 2) && (f2_fp < b_up_fp)) {
        b_up_fp = f2_fp;
        i_up = i2;
      } else if ((findexset[i2] == 3 || findexset[i2] == 4)
          && (f2_fp > b_low_fp)) {
        b_low_fp = f2_fp;
        i_low = i2;
      }
    }

    // System.out.println("eb");
    // Check optimality using current blow and bup and, if violated,
    // find and index i1 to do joint optimization with i2
    boolean optimality = true;
    if (findexset[i2] == 0 || findexset[i2] == 1 || findexset[i2] == 2) {
      if (FP.sub(b_low_fp, f2_fp) > FP.mul(FP.intToFp(2), tol_fp)) {
        optimality = false;
        i1 = i_low;
      }
    }
    if (findexset[i2] == 0 || findexset[i2] == 3 || findexset[i2] == 4) {
      if (FP.sub(f2_fp, b_up_fp) > FP.mul(FP.intToFp(2), tol_fp)) {
        optimality = false;
        i1 = i_up;
      }
    }

    if (optimality) {
      return false;
    }
    // For i2 choose the better i1...
    if (findexset[i2] == 0) {
      if (FP.sub(b_low_fp, f2_fp) > FP.sub(f2_fp, b_up_fp)) {
        i1 = i_low;
      } else {
        i1 = i_up;
      }
    }

    takeStep();
    return takeStepResult;
  }


  /**
   * Method takeStep, which optimizes two lagrange multipliers at a time. The
   * method uses DsvmUtilFP.epsEqual to determine if there was positive
   * progress.
   *
   * @param i1 -
   *          second choice heuristics
   * @param i2 -
   *          first choice heuristics
   * @return true if a positive step has occured
   */
  public static boolean takeStep() {

    if (i1 == i2) {
      takeStepResult = false;
      takeStepFlag = false;
      return false;
    }

    int alph1_fp = alpha_fp[i1];
    int alph2_fp = alpha_fp[i2];

    int f1_fp = getfFP(i1);

    // System.out.println("ts here3a");
    int f2_fp = getfFP(i2);
    // System.out.println("ts here4");
    int s_fp = FP.mul(y_fp[i1], y_fp[i2]);
    // System.out.println("ts here5");
    int eta_fp = getEtaFP(i1, i2);
    // System.out.println("ts here5");
    int l_fp = getLowerClipFP(i1, i2);
    // System.out.println("ts here6");
    int h_fp = getUpperClipFP(i1, i2); // TODO: It returned 6 (393216_fp)
    // System.out.println("ts here7");
    // on
    int a2_fp, a1_fp;
    // inner loop with 1,2,3 example

    if (eta_fp < 0) {
      // a2 = alph2 - y2 * (f1 - f2) / eta;
      a2_fp = FP.sub(alph2_fp, FP.mul(y_fp[i2], FP.div(FP.sub(f1_fp, f2_fp),
          eta_fp))); // TODO: This one is wrong on first inner loop.
      // a2 evaluates to zero
      if (a2_fp < l_fp) {
        a2_fp = l_fp;
      } else if (a2_fp > h_fp) {
        a2_fp = h_fp;
      }
    } else {
      // TODO: Check how often this is called and tune
      // getObjectivefunction
      // if needed
      int tempAlpha2_fp = alpha_fp[i2];
      alpha_fp[i2] = l_fp;
      int lObj_fp = getObjectiveFunctionFP();
      alpha_fp[i2] = h_fp;
      int hObj_fp = getObjectiveFunctionFP();
      alpha_fp[i2] = tempAlpha2_fp;
      if (lObj_fp > FP.add(hObj_fp, eps_fp)) {
        a2_fp = l_fp;
      } else if (lObj_fp < FP.sub(hObj_fp, eps_fp)) {
        a2_fp = h_fp;
      } else {
        a2_fp = alph2_fp;
      }
    } // Return false if no significant optimization has taken place
    if (FP.abs(FP.sub(a2_fp, alph2_fp)) < FP.mul(eps_fp, FP.add(a2_fp, FP.add(
        alph2_fp, eps_fp)))) {
      takeStepResult = false;
      takeStepFlag = false;
      return false;
    }
    // System.out.println("ts:y");
    a1_fp = FP.add(alph1_fp, FP.mul(s_fp, FP.sub(alph2_fp, a2_fp)));
    if (a1_fp < alphaTol_fp) {
      a1_fp = 0;
    }
    if (a2_fp < alphaTol_fp) {
      a2_fp = 0;
    }
    if (a1_fp > FP.add(c_fp, alphaTol_fp)) {
      System.out.println("a1_fp too large");// a1_fp = c_fp; //
      // TODO: Can this be
      // removed
    }
    if (a2_fp > FP.add(c_fp, alphaTol_fp)) {
      System.out.println("a2_fp too large");// a2_fp = c_fp; //
      // TODO: Can this be
      // removed
    }
    // System.out.println("ts:update fcache");
    // Update fcache_fp for i in I_0 using new Lagrange multipliers
    int fcache_fp_tmpi1 = 0;
    int fcache_fp_tmpi2 = 0;
    int fcache_fp_tmp_sum = 0;
    for (int i = 0; i < findexset0Size; i++) { // @WCA loop=2

      fcache_fp_tmpi1 = FP.mul(y_fp[i1], FP.mul(FP.sub(a1_fp, alph1_fp),
          getKernelOutputFP(i1, findexset0[i], true)));
      fcache_fp_tmpi2 = FP.mul(y_fp[i2], FP.mul(FP.sub(a2_fp, alph2_fp),
          getKernelOutputFP(i2, findexset0[i], true)));
      fcache_fp_tmp_sum = FP.add(fcache_fp_tmpi1, fcache_fp_tmpi2);
      fcache_fp[findexset0[i]] = FP.add(fcache_fp[findexset0[i]],
          fcache_fp_tmp_sum);
    }
    // Update the alphas //TODO: Check if FP 516 is too low
    alpha_fp[i1] = a1_fp;
    alpha_fp[i2] = a2_fp;
    // and F1 if not covered by I_0 //TODO:Check this for first run

    if (findexset[i1] != 0) {

      fcache_fp_tmpi1 = FP.mul(FP.mul(y_fp[i1], FP.sub(a1_fp, alph1_fp)),
          getKernelOutputFP(i1, i1, true));
      fcache_fp_tmpi2 = FP.mul(FP.mul(y_fp[i2], FP.sub(a2_fp, alph2_fp)),
          getKernelOutputFP(i1, i2, true));
      fcache_fp_tmp_sum = FP.add(fcache_fp_tmpi1, fcache_fp_tmpi2);
      fcache_fp[i1] = FP.add(f1_fp, fcache_fp_tmp_sum);
    }

    // and F2 if not covered by I_0
    if (findexset[i2] != 0) {

      fcache_fp_tmpi1 = FP.mul(FP.mul(y_fp[i1], FP.sub(a1_fp, alph1_fp)),
          getKernelOutputFP(i1, i2, true));
      fcache_fp_tmpi2 = FP.mul(FP.mul(y_fp[i2], FP.sub(a2_fp, alph2_fp)),
          getKernelOutputFP(i2, i2, true));
      fcache_fp_tmp_sum = FP.add(fcache_fp_tmpi1, fcache_fp_tmpi2);
      fcache_fp[i2] = FP.add(f2_fp, fcache_fp_tmp_sum);
    }

    // System.out.println("tsII start");
    updatefindex(i1);

    updatefindex(i2);

    updateiandbpartially();

    bias_fp = FP.div(FP.add(b_low_fp, b_up_fp), FP.TWO);

    takeStepCount++;
    takeStepResult = true;
    takeStepFlag = false;
    return true;
  }


  static int changed;
  static boolean inner_loop_success;
  /**
   * Method innerLoop, which is the inner loop that iterates until the examples
   * in the inner loop are self consistent.
   *
   * @return the number of changed examples
   */
  static int innerLoop() {
    changed = 0;
    inner_loop_success = true;
    do {
      i1 = i_up;
      i2 = i_low;

      takeStep();
      takeStepFlag = true;

      inner_loop_success = takeStepResult;

      if (inner_loop_success) {
        changed++;
      }
      if (!inner_loop_success) {
        break;
      }
      if (b_up_fp > FP.sub(b_low_fp, FP.mul(FP.TWO, tol_fp))) {
        break;
      }
      if ((i1 == i_up && i2 == i_low) || (i1 == i_low && i2 == i_up)) // avoid
        // deadlock
        break;
    } while (true);
    return changed;
  }

  /**
   * Method initParams, which will init the parameters of the SMO algorithm.
   * This method should only be called once, which would be just before the
   * mainRoutine().
   */
  static void initParams() {

    takeStepFlag = false;
    m = y_fp.length;
    n = data_fp[0].length;
    alpha_fp = new int[m];
    alpha_index_sorted = new int[m];
    findexset = new int[m];
    findexset0 = new int[m];
    findexset0Size = 0;
    findexset0pos = new int[m];
    // System.out.println("A initParams()");
    c_fp = FP.mul(FP.ONE, FP.intToFp(1));
    // System.out.println("Bd initParams()");
    bias_fp = FP.intToFp(0);
    // System.out.println("Ca initParams()");
    eps_fp = FP.div(FP.ONE, FP.intToFp(100));
    // System.out.println("Cb initParams()");
    tol_fp = FP.div(FP.ONE, FP.intToFp(10));
    // System.out.println("Cc initParams()");
    alphaTol_fp = FP.div(FP.ONE, FP.intToFp(100));

    KFP.setSigma2(FP.mul(FP.ONE, FP.ONE));
    KFP.setKernelType(KFP.DOTKERNEL);// GAUSSIANKERNEL or DOTKERNEL

    // Kernel type must be set first
    KFP.setData(data_fp);

    for (int i = 0; i < m; i++) {
      findexset0pos[i] = -1;
      findexset0[i] = -1;
      findexset[i] = calculatefindex(i);
      alpha_index_sorted[i] = i;
    }
    fcache_fp = new int[m];
    // Kernel cache init
    kernelCache_fp = new int[m];

    for (int i = 0; i < m; i++) {
      kernelCache_fp[i] = getKernelOutputFP(i, i, false);
    }

    b_up_fp = FP.intToFp(-1);
    b_low_fp = FP.intToFp(1);
    for (int i = 0; i < m; i++) {
      if (y_fp[i] == FP.ONE) {// Classs 1
        i_up = i;
        fcache_fp[i] = FP.intToFp(-1);
      } else if (y_fp[i] == -FP.ONE) // Class 2
      {
        i_low = i;
        fcache_fp[i] = FP.intToFp(1);
      }
    }
    // System.out.println("D initParams()");
    // 1 / 100000000;
    int loopStartInit = 1;
    // System.out.println("E initParams()");
    // constants

    // logFile = null;
  }


  /**
   * Method calculatefindex, which return which set the point belongs to based
   * on the alpha_fp value and the target.
   *
   * @param alpha_array -
   *          the alpha_fp value
   * @return target - the target value
   */
  static int calculatefindex(int i) {


    int retVal = -1;
    if (alpha_fp[i] > alphaTol_fp) {
      if (alpha_fp[i] < FP.sub(c_fp, alphaTol_fp)) {
        retVal = 0;
      } else if (y_fp[i] == FP.ONE) {
        retVal = 3;
      } else {
        retVal = 2;
      }
    } else if (y_fp[i] == FP.ONE) {
      retVal = 1;
    } else {
      retVal = 4;
    }

    if (retVal == -1) {
      System.out.println("calculatefindex not working!");
      System.exit(1);
    }
    return retVal;
  }

  /**
   * Method updatefindex, which is a cache maintainer that updates findexset,
   * findexset0, findexset0pos.
   *
   * @param i -
   *          the point
   */
  static void updatefindex(int i) {

    int index = calculatefindex(i);
    // Only do the work if the index has changed
    if (index != findexset[i]) {
      // The point entered I_0
      if (index == 0) {
        // Updated index code based on a stack principle based on idea
        // from Group A, DDM Class, 2005
        findexset0[findexset0Size] = i;
        findexset0pos[i] = findexset0Size;
        findexset0Size++;
        /*
         * // Create the new array with new element in the end int[]
         * newfindexset0 = new int[findexset0.length + 1]; // Assign the i'th
         * element to the new position newfindexset0[findexset0.length] = i; //
         * Update the position array findexset0pos[i] = findexset0.length; //
         * Copy the old array into the new System.arraycopy(findexset0, 0,
         * newfindexset0, 0, findexset0.length); findexset0 = null; findexset0 =
         * newfindexset0;
         */
      }
      // The point has left I_0
      if (findexset[i] == 0) {
        if (findexset0pos[i] != findexset0Size - 1) {
          findexset0[findexset0pos[i]] = findexset0[findexset0Size - 1];
          findexset0pos[findexset0[findexset0Size - 1]] = findexset0pos[i];
        }
        findexset0Size--;


      }
      // Update findexset
      findexset[i] = index;
    }
  }

  /**
   * Method getf, which calculates and returns the functional output without
   * using a bias_fp. see keerti99
   *
   * @param i -
   *          index
   * @return the non-biased functional output.
   */

  static int getfFP(int i) {
    // First check if i is in I_0 or i_low or i_up
    // System.out.println("g:"); // +(cnt++));
    if (findexset[i] == 0 || i == i_low || i == i_up) {
      return fcache_fp[i];
    } else {
      // Calculate f using I_0
      int f_fp = 0;
      int f_fp_tmp = 0;
      for (int j = 0; j < m; j++) {
        f_fp_tmp = FP.mul(y_fp[j], alpha_fp[j]);
        f_fp_tmp = FP.mul(f_fp_tmp, getKernelOutputFP(i, j, true));
        f_fp = FP.add(f_fp, f_fp_tmp);
      }
      f_fp = FP.sub(f_fp, y_fp[i]);
      return f_fp;
    }
  }

  /**
   * Method updateI0cache, which will update the fcache_fp. It is needed when
   * the the kernel has been changed. A little trick is used to get the getf
   * method to update the fcache_fp.
   */
  static void updatefCache() {
    for (int i = 0; i < findexset0Size; i++) {
      // This will make getf to recalculate
      findexset[findexset0[i]] = -1;
      fcache_fp[findexset0[i]] = getfFP(findexset0[i]);
      findexset[findexset0[i]] = 0;
    }
    // Now check if i_low is not in I0 and update if not
    if (findexset[i_low] != 0) {
      fcache_fp[i_low] = getfFP(i_low);
    }
    // Now check if i_low is not in I0 and update if not
    if (findexset[i_up] != 0) {
      fcache_fp[i_up] = getfFP(i_up);
    }
  }

  /**
   * Method updateI0indices, which will update the findexset arrray.
   */
  static void updatesetindices() {
    for (int i = 0; i < m; i++) {
      updatefindex(i);
    }
  }

  /**
   * Method updateiandbfully, which will update b_low_fp, b_up_fp, i_low, i_up.
   * The update is a partial one using only the point previously in I0 or i_low
   * i_up itself. The method loops over all examples.
   */
  static void updateiandbfully() {
    // Compute i_low, b_low_fp and i_up, b_up_fp
    // System.out.println("updatefully A");
    // printScalar("b_low_fp",b_low_fp);
    b_low_fp = FP.intToFp(-10000);
    // b_low_fp = FP.MIN;
    b_up_fp = FP.intToFp(10000);// FP.MAX;
    i_low = -1;
    i_up = -1;


    for (int i = 0; i < m; i++) {
      // I0
      if (findexset[i] == 0) {
        if (y_fp[i] == FP.ONE) {
          if (b_up_fp >= fcache_fp[i]) {
            b_up_fp = fcache_fp[i];
            i_up = i;
          }
        } else {// y_fp == -1
          if (b_low_fp < fcache_fp[i]) {
            b_low_fp = fcache_fp[i];
            i_low = i;
          }
        }
      } else if (findexset[i] == 1 || findexset[i] == 2) {
        if (b_up_fp > fcache_fp[i]) {
          b_up_fp = fcache_fp[i];
          i_up = i;
        }
      } else if (findexset[i] == 3 || findexset[i] == 4) {
        if (b_low_fp < fcache_fp[i]) {
          b_low_fp = fcache_fp[i];
          i_low = i;
        }
      }
    }

    if (i_low == -1 || i_up == -1) {
      System.out
          .println("updateiandbfully() problem: i_low or i_up is not set");
      System.exit(-1);
    }
    if (i_low == i_up) {
      System.out
          .println("updateiandbfully() problem: i_low or i_up is the same point");
      System.exit(-1);
    }

    // System.out.println("exit updatefully");
  }

  /**
   * Method updateiandb, which will update b_low_fp, b_up_fp, i_low, i_up. The
   * update is a partial one using only the point previously in I0 or i_low i_up
   * itself. b_low is max(I0,I3,I4)<br>
   * b_max is min(I0,I1,I2)
   */
  static void updateiandbpartially() {


    // Compute i_low, b_low_fp and i_up, b_up_fp
    b_low_fp = FP.intToFp(-10000);// FP.MIN;

    b_up_fp = FP.intToFp(10000);// FP.MAX;

    i_low = -1;

    i_up = -1;// TODO: check below

    for (int i = 0; i < findexset0Size; i++) {

      if (fcache_fp[findexset0[i]] >= b_low_fp) {
        b_low_fp = fcache_fp[findexset0[i]];
        i_low = findexset0[i];
      }
      if (fcache_fp[findexset0[i]] < b_up_fp) {
        b_up_fp = fcache_fp[findexset0[i]];
        i_up = findexset0[i];
      }

    }

    // Now check i1 if not in I_0
    if (findexset[i1] != 0) {
      if (findexset[i1] == 1 || findexset[i1] == 2) { // I_1 or I_2
        if (b_up_fp > fcache_fp[i1] || findexset0Size == 1) { // TODO:

          b_up_fp = fcache_fp[i1];
          i_up = i1;
        }
      } else { // I_3 or I_4
        if (b_low_fp < fcache_fp[i1] || findexset0Size == 1) {
          b_low_fp = fcache_fp[i1];
          i_low = i1;
        }
      }
    }


    // Now check i2 if not in I_0
    if (findexset[i2] != 0) {
      if (findexset[i2] == 1 || findexset[i2] == 2) {
        if (b_up_fp > fcache_fp[i2] || findexset0Size == 1) {
          b_up_fp = fcache_fp[i2];
          i_up = i2;
        }
      } else { // I_3 or I_4
        if (b_low_fp < fcache_fp[i2] || findexset0Size == 1) {
          b_low_fp = fcache_fp[i2];
          i_low = i2;
        }
      }
    }


    if (i_low == -1 || i_up == -1) {
      System.out
          .println("updateiandbpartially() problem: i_low or i_up is not set");
      System.out.println("findexset0Size:"+findexset0Size);
      System.out.println("i1:"+i1);
      System.out.println("i2:"+i2);
      System.out.println("i_low:"+i_low);
      System.out.println("i_up:"+i_up);
      System.out.println("findexset[i1]:"+findexset[i1]);
      System.out.println("findexset[i2]:"+findexset[i2]);
      for (int i = 0; i < findexset0Size; i++) {
        System.out.print("fcache_fp[findexset0[");
        System.out.print(i);
        System.out.print("]]:");
        System.out.print(fcache_fp[findexset0[i]]);
        System.out.print(" ");
        System.out.println(FP.fpToInt(fcache_fp[findexset0[i]]));
      }

      System.exit(-1);
    }
    if (i_low == i_up) {
      System.out
          .println("updateiandbpartially() problem: i_low or i_up is the same point");
      System.exit(-1);
    }

  }

  /**
   * Method to check if the example violates the KKT conditions. The method
   * assumes that the constrained Lagrange multipliers has been explicitly set
   * to the the appropriate boundary value zero or C. This equation relates to
   * equation 12.2 in Platts paper.
   *
   * @param p
   *          the example to check
   * @return true if example violates KKT
   */
  static boolean isKktViolated(int p) {
    boolean violation = true;
    int f_fp = getFunctionOutputFP(p);
    // Is alpha_fp on lower bound?
    if (alpha_fp[p] == 0) {
      if (FP.mul(y_fp[p], f_fp) >= FP.sub(1, eps_fp)) {
        violation = false;
      }
    } // or is alpha_fp in non-bound, NB, set?
    else if (alpha_fp[p] > 0 && alpha_fp[p] < c_fp) {
      if (FP.mul(y_fp[p], f_fp) > FP.sub(1, eps_fp)
          && FP.mul(y_fp[p], f_fp) < FP.add(1, eps_fp)) {
        violation = false;
      }
    } // alpha_fp is on upper bound
    else {
      if (FP.mul(y_fp[p], f_fp) <= FP.add(1, eps_fp)) {
        violation = false;
      }
    }
    return violation;
  }

  /**
   * Method getObjectiveFunction, which calculates and returns the value of the
   * objective function based on the values in the alpha_fp array.
   *
   * @return the objective function (6.1 in Christianini).
   */
  static int getObjectiveFunctionFP() {
    // TODO: Check how often this is called and tune if possible
    int objfunc_fp = 0;
    for (int i = 0; i < m; i++) {
      // Don't do the calculation for zero alphas
      if (alpha_fp[i] > 0) {
        objfunc_fp = FP.add(objfunc_fp, alpha_fp[i]);
        for (int j = 0; j < m; j++) {
          if (alpha_fp[j] > 0) {
            objfunc_fp = FP.sub(objfunc_fp, FP.mul(FP.mul(FP.mul(FP.mul(FP.mul(
                FP.HALF, y_fp[i]), y_fp[j]), alpha_fp[i]), alpha_fp[j]),
                getKernelOutputFP(i, j, true)));
          }
        }
      }
    }
    return objfunc_fp;
  }

  /**
   * Method calculatedError, which calculates the error from from scratch.
   *
   * @param p -
   *          point to calculte error for
   * @return calculated error
   */
  static int getCalculatedErrorFP(int p) {
    return FP.sub(getFunctionOutputFP(p), y_fp[p]);
  }

  /**
   * Method getFunctionOutput, which will return the functional output for point
   * p.
   *
   * @param p -
   *          the point index
   * @return the functinal output
   */
  static int getFunctionOutputFP(int p) {
    int functionalOutput_fp = 0;
    int kernelOutput_fp = 0;
    for (int i = 0; i < m; i++) {
      // Don't do the kernel if it is epsequal
      if (alpha_fp[i] > 0) {
        kernelOutput_fp = getKernelOutputFP(i, p, true);
        functionalOutput_fp = FP.add(functionalOutput_fp, FP.mul(FP.mul(
            alpha_fp[i], y_fp[i]), kernelOutput_fp));
      }
    } // Make a check here to see any alphas has been modified after
    functionalOutput_fp = FP.sub(functionalOutput_fp, bias_fp);
    return functionalOutput_fp;
  }

  /**
   * Method getKernelOutput, which returns the kernel of two points.
   *
   * @param i1 -
   *          index of alpha_fp 1
   * @param i2 -
   *          index of alpha_fp 2
   * @param useCache
   *          TODO
   * @param useCache -
   *          will use the cache if possible
   * @return kernel output
   */
  static int getKernelOutputFP(int i1, int i2, boolean useCache) {
    if (i1 == i2 && useCache) {
      return kernelCache_fp[i1];
    }
    return KFP.kernel(i1, i2);
  }

  /**
   * Method getEta, which returns eta_fp = 2*k12-k11-k22
   *
   * @param i1 -
   *          index of first point
   * @param i2
   *          -index of second point
   * @return double - eta_fp
   */
  static int getEtaFP(int i1, int i2) {
    int eta_fp;
    int eta_fp_tmp;
    int kernel11_fp, kernel22_fp, kernel12_fp;
    kernel11_fp = getKernelOutputFP(i1, i1, true);
    kernel22_fp = getKernelOutputFP(i2, i2, true);
    kernel12_fp = getKernelOutputFP(i1, i2, true);
    eta_fp = FP.sub(FP.sub(FP.mul(FP.TWO, kernel12_fp), kernel11_fp),
        kernel22_fp);
    return eta_fp;
  }

  /**
   * Method getLowerClip, which returns the lower clip value for some pair of
   * Lagrange multipliers. Pls. refer to Nello's book for more info.
   *
   * @param i1 -
   *          first point
   * @param i2 -
   *          second point
   * @return the lower clip value
   */
  static int getLowerClipFP(int i1, int i2) {
    int u_fp = 0;
    if (y_fp[i1] == y_fp[i2]) {
      u_fp = FP.sub(FP.add(alpha_fp[i1], alpha_fp[i2]), c_fp);
      if (u_fp < 0) {
        u_fp = 0;
      }
    } else {
      u_fp = FP.sub(alpha_fp[i2], alpha_fp[i1]);
      if (u_fp < 0) {
        u_fp = 0;
      }
    }
    return u_fp;
  }

  /**
   * Method getUpperClip, which will return the upper clip based on two Lagrange
   * multipliers.
   *
   * @param i1 -
   *          first point
   * @param i2 -
   *          second point
   * @return the upper clip
   */
  static int getUpperClipFP(int i1, int i2) {
    int v_fp = 0;
    if (y_fp[i1] == y_fp[i2]) {
      v_fp = FP.add(alpha_fp[i1], alpha_fp[i2]);
      if (v_fp > c_fp) {
        v_fp = c_fp;
      }
    } else {
      v_fp = FP.add(c_fp, FP.sub(alpha_fp[i2], alpha_fp[i1]));
      if (v_fp > c_fp) {
        v_fp = c_fp;
      }
    }
    return v_fp;
  }

  static public int getTrainingErrorCountFP() {
    int errorCount = 0;
    for (int i = 0; i < m; i++) {
      int fout_fp = getFunctionOutputFP(i);

      if (fout_fp > 0 && y_fp[i] < 0) {
        errorCount++;

      } else if (fout_fp < 0 && y_fp[i] > 0) {
        errorCount++;

      }
    }
    return errorCount;
  }

  /**
   * Method calculateW, which calculates the weight vector. This is used for
   * linear SVMs.
   *
   * @return the weight [n] vector
   */
  static int[] calculateWFP() {
    int[] w_fp;
    w_fp = new int[n];
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        w_fp[j] = FP.add(w_fp[j], FP.mul(FP.mul(y_fp[i], alpha_fp[i]),
            data_fp[i][j]));
      }
    }
    return w_fp;
  }

  /**
   * Method isExampleBound, which will return true if the point p is on the
   * bound as defined as less then (0+tol_fp) or greater than (C-tol_fp).
   *
   * @param p -
   *          index of point
   * @return true if p is on bound
   */
  static boolean isExampleOnBound(int p) {
    return alpha_fp[p] < tol_fp || alpha_fp[p] > FP.sub(c_fp, tol_fp);
  }

  /**
   * Method getFunctionOutput, which will return the functional output for point
   * represented by a input vector only.
   *
   * @param xtest -
   *          the input vector
   * @return the functinal output
   */
  static public int getFunctionOutputTestPointFP(int[] xtest) {
    int functionalOutput_fp = 0;
    int[][] data_fp_local = data_fp;
    int m = data_fp_local.length;
    int func_out = 0;
    //System.out.println("---ALIVE1m---" + m);
    int n = xtest.length;
    int n2 = data_fp_local[0].length;

    for (int i = 0; i < ALPHA_RT; i++) { // @WCA loop=5

      n = xtest.length;

      while (n != 0) { // @WCA loop=2
        n = n - 1;

        func_out += (data_fp_local[alpha_index_sorted[i]][n] >> 8) * (xtest[n] >> 8);
      }
      if (alpha_fp[alpha_index_sorted[i]] > 0) {
        functionalOutput_fp += func_out;
      }
      func_out = 0;
    }
    functionalOutput_fp -= bias_fp;
    return functionalOutput_fp;
  }

  /**
   * Method getFunctionOutput, which will return the functional output for point
   * represented by a input vector only.
   *
   * @param xtest -
   *          the input vector
   * @return the functinal output
   */
  static public int getFunctionOutputTestPointFP_OOAD(int[] xtest) {
    int functionalOutput_fp = 0;
    int kernelOutput_fp = 0;
    KFP.setX(xtest);
    for (int i = 0; i < m; i++) {
      // Don't do the kernel if it is epsequal
      if (alpha_fp[i] > 0) {
        kernelOutput_fp = KFP.kernelX(i);
        functionalOutput_fp = FP.add(functionalOutput_fp, FP.mul(FP.mul(
            alpha_fp[i], y_fp[i]), kernelOutput_fp));
      }
    } // Make a check here to see any alphas has been modified after
    functionalOutput_fp = FP.sub(functionalOutput_fp, bias_fp);
    return functionalOutput_fp;
  }


  static public void setData_fp(int[][] data_fp) {
    SMOBinaryClassifierFP.data_fp = data_fp;
  }

  static public void setY_fp(int[] y_fp) {
    SMOBinaryClassifierFP.y_fp = y_fp;
  }

  static void gccheck() {
    System.out.print("GC free words ");
    // System.out.println(GC.free());
  }

  static void sp() {
    System.out.print("sp=");
    System.out.println(Native.rd(com.jopdesign.sys.Const.IO_WD));
  }

  static public void smoInfo10() {
    for (int i = 0; i < 10; i++) {
      printSMOInfo = true;
//      while (printSMOInfo)
//        smo.waitForNextPeriod();
    }
  }

  static public void smoInfo() {
    // printScalar("wd",Native.rd(Const.IO_WD)); //TODO: Can it be read?
    System.out.println("======SMO INFO START======");
    // printScalar("sp",Native.rd(com.jopdesign.sys.Const.IO_WD));
    printScalar("i1", i1);
    printScalar("i2", i2);
    printScalar("i_low", i_low);
    printScalar("i_up", i_up);
    printScalar("b_low_fp", b_low_fp);
    printScalar("b_up_fp", b_up_fp);
    printScalar("bias_fp", bias_fp);
    printScalar("m", m);
    printScalar("n", n);
    printMatrix("data_fp", data_fp);
    printVector("y_fp", y_fp);
    printVector("fcache_fp", fcache_fp);
    printVector("kernelCache_fp", kernelCache_fp);
    printVector("findexset", findexset);
    printVector("findexset0pos", findexset0pos);
    printVector("findexset0", findexset0);
    printScalar("findexset0Size", findexset0Size);
    printVector("alpha_fp", alpha_fp);
    printScalar("alphaTol_fp", alphaTol_fp);
    printScalar("c_fp", c_fp);
    printScalar("tol_fp", tol_fp);
    printScalar("eps_fp", eps_fp);
    printBoolean("takeStepFlag", takeStepFlag);
    printBoolean("takeStepResult", takeStepResult);
    printScalar("takeStepCount", takeStepCount);
    int svs = 0;
    for(int i=0;i<m;i++){
      if(alpha_fp[i]>alphaTol_fp)
        svs++;
    }
    printScalar("#sv", svs);
    printScalar("training err cnt",getTrainingErrorCountFP());


    for (int i = 0; i < 100000; i++)
      ;
    System.out.println("======SMO INFO END======");
  }

  static void printBoolean(String str, boolean b) {
    System.out.print(str);
    System.out.print(':');
    if (b)
      System.out.println("true");
    else
      System.out.println("false");
  }

  static void printScalar(String str, int sca) {
    System.out.print(str);
    System.out.print(':');
    System.out.println(sca);
    for (int i = 0; i < 100; i++)
      ;
  }

  static void printVector(String str, int[] ve) {
    System.out.print(str);
    System.out.print(" {");
    for (int i = 0; i < ve.length; i++) {
      System.out.print(i);
      System.out.print(':');
      System.out.print(ve[i]);
      if (i < (ve.length - 1))
        System.out.print(", ");

      for (int j = 0; j < 1000; j++)
        ;
    }
    System.out.println("}");
    for (int i = 0; i < 1000; i++)
      ;
  }

  static void printMatrix(String str, int[][] ma) {
    for (int i = 0; i < ma.length; i++) {
      System.out.print(str);
      System.out.print("[");
      System.out.print(i);
      System.out.print("]");
      System.out.print(":");
      printVector("", ma[i]);
    }
  }

  static public int getSV(){
    int svs = 0;
    for(int i=0;i<m;i++){
      if(alpha_fp[i]>alphaTol_fp)
        svs++;
    }
    return svs;
  }

  // sorts the indeces of the alphas
  static void sortAlphaIndex(){

	int changed;
	if (PRINT) System.out.println("SMO.sortalphaindex");
	do {
	  changed = 0;
	  if (PRINT) System.out.println("SMO.sort1");
      for(int i = 0; i < (m-1); i++){
	    if(alpha_fp[alpha_index_sorted[i]] < alpha_fp[alpha_index_sorted[i+1]]) {
	      int tmp = alpha_index_sorted[i];
	      alpha_index_sorted[i] = alpha_index_sorted[i+1];
	      alpha_index_sorted[i+1] = tmp;
	      changed++;
	    }
      }
      if (PRINT) System.out.println("SMO.sort2");
	  if (PRINT) {
		  System.out.println("Sorting...");
	  }

	} while(changed > 0);
  }

  /**
   * Do not instanciate me.
   */
  private SMOBinaryClassifierFP(){}
}