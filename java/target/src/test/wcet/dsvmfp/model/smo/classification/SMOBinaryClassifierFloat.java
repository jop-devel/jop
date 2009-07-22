package wcet.dsvmfp.model.smo.classification;

import cmp.Execute;
import cmp.ParallelExecutor;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import wcet.dsvmfp.model.smo.kernel.FloatUtil;
import wcet.dsvmfp.model.smo.kernel.KFloat;

/**
 * Class SMOBinaryClassifier with float.
 */
public class SMOBinaryClassifierFloat {

	final static boolean PRINT = true;

	/** Number of lagrange multipliers in deployed RT model */
	public final static int ALPHA_RT = 2;

	static boolean info;

	static public boolean printSMOInfo;

	/** The F cache. */
	static public float[] fcache_fp;

	/** Kernel cache i==i kernels. */
	static public float[] kernelCache_fp;

	/**
	 * The index set. The value is an int that determines if the point belongs
	 * to index set. It is updated after each <code>takestep</code>. <br>
	 * 0: 0<a<C <br>
	 * 1: y_fp=+1,a=0 <br>
	 * 2: y_fp=-1,a=C <br>
	 * 3: y_fp=+1,a=C <br>
	 * 4: y_fp=-1,a=0
	 */
	static public int[] findexset;

	/**
	 * A full length array that has holds the position of the point in
	 * findexset0. Only entries that ==0 in findexset are valid.
	 */
	static public int[] findexset0pos;

	/**
	 * Index set 0: 0 <a <C. The length of the set is changed dynamically to
	 * match the number of NB points.
	 */
	static public int[] findexset0;

	/**
	 * Length of findexset0.
	 */
	static public int findexset0Size;

	/** The [m] Lagrange multipliers. */
	static public float[] alpha_fp;

	/** Sorted to have largest alpha index on [0] and so on. */
	static public int[] alpha_index_sorted;

	/** The target vector of {-1,+1}. */
	static public float[] y_fp;

	/** The data vector [rows][columns]. */
	static public float[][] data_fp;

	/** The high bound. */
	static public float c_fp;

	/** The error tolerance. */
	static public float tol_fp;

	/** The error tolerance, that is used for KKT violation checks. */
	static public float eps_fp;

	// E1 and E1 as used in takestep
	static public float E1, E2;

	/**
	 * Accuracy used when dividing the alpha_fp values into sets. For example:
	 * if (a <alphaTol_fp) then a = 0
	 */
	static public float alphaTol_fp;

	/** Parameters used for keeping the indices of the low and high Fs. */
	static public float b_up_fp;

	static public float b_low_fp;

	static public int i_low, i_up;

	/** The bias_fp. */
	static public float bias_fp;

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

	// LOCAL METHOD VARIABLES
	// takeStep start
	// alpha1 and alpha2
	// int eta_fp;

	// The box clippings
	// int l_fp, h_fp;

	// The new alphas.and old clipped and non-clipped Lagrange multipliers
	// int a1_fp, a2_fp;

	// The cached old alphas.
	// int alph1_fp, alph2_fp;

	// The target variables for the two points
	// int y1_fp, y2_fp;

	// The modified functional output (without bias_fp (Keerthi99).
	// int f1_fp, f2_fp;
	// Sign
	// int s_fp;

	// takestep end
	// examineexample start
	// int i1, i2;

	// int metai = 0;

	// int metaj = 0;

	/**
	 * Method mainRoutine, which estimates the SVM parameters. The parameters
	 * are intialized before the training of the SVM is conducted.
	 * 
	 * @return true if the trainng went well
	 */
	static public boolean mainRoutine() {

		int time = 0;
		if (PRINT)
			System.out.println("SMO.mainroutine");
		time = Native.rd(Const.IO_US_CNT);
		// for(int i=0;i<1000000;i++);
		info = false;
		// ABC.init();
		// The number of updated that significantly changed the
		// Lagrange multipliers
		numChanged = 0;
		// Indicates if all examples has been tested
		examineAll = true;
		// Reset alpha_fp array , errorarray , b, and C
		takeStepCount = 0;
		initParams();
		System.out.println("After init params");
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

		boolean first = true;
		P("++++Pre While++++");
		while (numChanged > 0 || first) {
			P("----Pre loop----");
			first = false;
			numChanged = 0;
			// assigns i1 and i2
			// getIndex(indexarray);

			for (i1 = 0; i1 < m; i1++) {
				System.out.println("*******takeStep()*****");
				System.out.print("i1:");
				System.out.println(i1);
				for (i2 = 0; i2 < m; i2++) {
					System.out.print("i2:");
					System.out.println(i2);
					takeStep();
					if (takeStepResult)
						numChanged++;
					System.out.print("takeStep: ");
					System.out.println(takeStepResult);
				}
			}
			P("////Post for loop:" + numChanged);
		}
		P("++++Post while++++");

		// OLD SVM
		if (false) {

			while (numChanged > 0 || examineAll) { // @WCA loop=2
				// while (loop>=0) { //temp debug forever loop
				// System.out.print("Starting loop=");
				// System.out.println(loop);
				loop++;
				// System.out.print("numChanged=");
				// System.out.println(numChanged);
				// if (examineAll)
				// System.out.println("examineAll=true");
				// else
				// System.out.println("examineAll=false");
				for (int i = 0; i < 100000; i++)
					// @WCA loop=2
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
					// numChanged = 0; // TODO: remove
				}
				if (examineAll) {
					examineAll = false;
				} else if (numChanged == 0) {
					examineAll = true;
				}
				// break;
			}
			if (PRINT)
				System.out.println("SMO.mainroutine.trained");
			sortAlphaIndex();
			time = Native.rd(Const.IO_US_CNT) - time;
		}// false

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
	 * Method examineExample, which will take a step using a number of
	 * heuristics. The points coming into this method is from the outer in the
	 * SMO main routine: first choice heuristic
	 * 
	 * @param i2
	 *            - zero based index of second point to classify, which is
	 *            chosen by the outer loop in smo
	 * @return true if it was possible to take a step
	 */
	static boolean examineExample() {
		// System.out.print("EXAMINEEXAMPLE, i2:");

		// System.out.println(i2);

		// for(int i=0;i<200000;i++);
		i1 = -1;

		// Target
		// double y2_fp;
		// Lagrange multiplier
		// double alph2_fp;
		// Index of first point
		// Functional output
		// double f2_fp;
		// Assignment to local variables

		float f2_fp;
		// int y2_fp = y_fp[i2];
		// int alph2_fp = alpha_fp[i2];
		// Todo: Enable cache
		/***************************************************************************
		 * if (findexset[i2] == 0) { f2_fp = fcache_fp[i2]; } else
		 **************************************************************************/
		// System.out.println("ea");
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
			if ((b_low_fp - f2_fp) > (2 * tol_fp)) {
				optimality = false;
				i1 = i_low;
			}
		}
		if (findexset[i2] == 0 || findexset[i2] == 3 || findexset[i2] == 4) {
			if (FloatUtil.sub(f2_fp, b_up_fp) > FloatUtil.mul(FloatUtil
					.intToFp(2), tol_fp)) {
				optimality = false;
				i1 = i_up;
			}
		}

		if (optimality) {
			return false;
		}
		// For i2 choose the better i1...
		if (findexset[i2] == 0) {
			if (FloatUtil.sub(b_low_fp, f2_fp) > FloatUtil.sub(f2_fp, b_up_fp)) {
				i1 = i_low;
			} else {
				i1 = i_up;
			}
		}
		// System.out.println("ec");
		// System.out.println("ed");
		// printBoolean("takeStepResult",takeStepResult);
		// if (takeStep()) {
		// return true;
		// } else {
		// return false;
		// }

		// takeStepFlag = true;
		// // Wait until takeStep() is done
		// if (runasthread) {
		// while (takeStepFlag) {
		// smo.waitForNextPeriod();
		// }
		// }
		//
		// return takeStepResult;
		takeStep();
		return takeStepResult;
	}

	// static void rec() {
	// // Dbg.wr((char) (Native.rd(Const.IO_WD)+'0'+1));
	// Dbg.wr(Native.rd(Const.IO_WD));
	// rec();
	// }

	/**
	 * Method takeStep, which optimizes two lagrange multipliers at a time. The
	 * method uses DsvmUtilABC.epsEqual to determine if there was positive
	 * progress.
	 * 
	 * @param i1
	 *            - second choice heuristics
	 * @param i2
	 *            - first choice heuristics
	 * @return true if a positive step has occured
	 */
	public static boolean takeStep() {

		// System.out.print("TAKESTEP START: i1=");
		// System.out.print(i1);
		// System.out.print(", i2=");
		// System.out.println(i2);
		// if(i1 == 15 && i2 ==74){
		// printSMOInfo = true;
		// while(printSMOInfo)
		// smo.waitForNextPeriod();
		// }
		// rec();

		// Native.wr(1, com.jopdesign.sys.Const.IO_WD);

		// rec();
		// this.i1 = i1;
		// this.i2 = i2;
		// int objStart = 0;
		// if (info)
		// objStart = getObjectiveFunctionFP();
		// int alphaCheck = 0;

		// if (info)
		// for (int i = 0; i < m; i++)
		// alphaCheck = ABC.add(alphaCheck, ABC.mul(y_fp[i], alpha_fp[i]));
		// if (ABC.abs(alphaCheck) > alphaTol_fp && info)
		// System.out.println("Entry: Alphas*y does not add up to 0="
		// + ABC.fpToStr(alphaCheck));
		// System.out.println("ts here 1");
		// if (takeStepCount % 1000 == 0 && info) {
		// System.out.println("takeStep(): count=" + takeStepCount
		// + ", Entering method, i1=" + i1 + ", i2=" + i2);
		// }
		// System.out.println("ts here 1a");
		// double qp = printQP("takeStep start", i1, getAlpha(i1), i2,
		// getAlpha(i2), true);
		// dLog.log("qp=" + qp, Log.NORMAL);
		// showQPGraph("MyQpGraph: loop " + loop, i1, getAlpha(i1), i2);
		// Second derivative along the diagonal line in the box constraints for
		// If the first and second point is the same then return false
		if (i1 == i2) {
			takeStepResult = false;
			takeStepFlag = false;
			return false;
		}
		// TODO: remove repeated assignments
		// System.out.println("ts here2");
		float alph1_fp = alpha_fp[i1];
		float alph2_fp = alpha_fp[i2];
		P("S: alph1_fp:" + alph1_fp);
		P("S: alph2_fp:" + alph2_fp);
		// int y1_fp = y_fp[i1];
		// int y2_fp = y_fp[i2];
		// System.out.println("ts here3");
		float f1_fp = getfFP(i1);
		E1 = f1_fp - y_fp[i1];

		// System.out.println("ts here3a");
		float f2_fp = getfFP(i2);
		E2 = f2_fp - y_fp[i2];

		// System.out.println("ts here4");
		float s_fp = FloatUtil.mul(y_fp[i1], y_fp[i2]);
		System.out.println("s_fp:" + s_fp);
		// System.out.println("ts here5");
		float eta_fp = getEtaFP(i1, i2);
		System.out.println("eta:" + eta_fp);
		float l_fp = getLowerClipFP(i1, i2);
		System.out.println("l_fp=" + l_fp);
		float h_fp = getUpperClipFP(i1, i2); // TODO: It returned 6 (393216_fp)
		System.out.println("h_fp=" + h_fp);
		// System.out.println("ts here7");

		// on
		float a2_fp, a1_fp;
		// inner loop with 1,2,3 example

		if (eta_fp < 0) {
			P("!!!: alph2_fp:" + alph2_fp);
			a2_fp = alph2_fp - y_fp[i2] * (E1 - E2) / eta_fp;
			// a2_fp = FloatUtil.sub(alph2_fp, FloatUtil.mul(y_fp[i2], FloatUtil
			// .div(FloatUtil.sub(f1_fp, f2_fp), eta_fp))); // TODO: This
			// // one is
			// wrong on
			// first
			// inner
			// loop.
			P("!!!: a2_fp:" + a2_fp);

			// a2 evaluates to zero
			if (a2_fp < l_fp) {
				a2_fp = l_fp;
			} else if (a2_fp > h_fp) {
				a2_fp = h_fp;
			}
			P("1: a2_fp:" + a2_fp);
		} else {

			// TODO: Check how often this is called and tune
			// getObjectivefunction
			// if needed
			float tempAlpha2_fp = alpha_fp[i2];
			alpha_fp[i2] = l_fp;
			float lObj_fp = getObjectiveFunctionFP();
			alpha_fp[i2] = h_fp;
			float hObj_fp = getObjectiveFunctionFP();
			alpha_fp[i2] = tempAlpha2_fp;
			if (lObj_fp > (hObj_fp + eps_fp)) {
				a2_fp = l_fp;
			} else if (lObj_fp < FloatUtil.sub(hObj_fp, eps_fp)) {
				a2_fp = h_fp;
			} else {
				a2_fp = alph2_fp;
			}
			throw new Error("Wrong path in takestep");
		} // Return false if no significant optimization has taken place

		P("test a2_fp" + a2_fp);
		P("test alph2_fp" + alph2_fp);
		if (FloatUtil.abs(FloatUtil.sub(a2_fp, alph2_fp)) < FloatUtil.mul(
				eps_fp, FloatUtil.add(a2_fp, (alph2_fp + eps_fp)))) {
			takeStepResult = false;
			takeStepFlag = false;
			P("No signigicant optimization");
			return false;
		}
		// System.out.println("ts:y");
		a1_fp = alph1_fp + s_fp * (alph2_fp - a2_fp);
		P("Raw a1_fp:" + a1_fp);
		if (a1_fp < alphaTol_fp) {
			a1_fp = 0;
		}
		if (a2_fp < alphaTol_fp) {
			a2_fp = 0;
		}
		if (a1_fp > (c_fp + alphaTol_fp)) {
			System.out.println("a1_fp too large");// a1_fp = c_fp; //
			// TODO: Can this be
			// removed
		}
		if (a2_fp > (c_fp + alphaTol_fp)) {
			System.out.println("a2_fp too large");// a2_fp = c_fp; //
			// TODO: Can this be
			// removed
		}
		P("!!a1_fp=" + a1_fp);
		P("!!a2_fp=" + a2_fp);
		alpha_fp[i1] = a1_fp;
		alpha_fp[i2] = a2_fp;
		// P("!!!f1_fp="+f1_fp);
		// P("!!!f2_fp="+f2_fp);
		P("!!!bias_fp=" + bias_fp);
		P("!!!getKernelOutputFloat(i1, i1, false)="
				+ getKernelOutputFloat(i1, i1, false));
		P("!!!getKernelOutputFloat(i1, i2, false)="
				+ getKernelOutputFloat(i1, i2, false));

		float bias = E1 + y_fp[i1] * (a1_fp - alph1_fp)
				* getKernelOutputFloat(i1, i1, false);
		// P("!1bias="+bias);
		bias += y_fp[i2] * (a2_fp - alph2_fp)
				* getKernelOutputFloat(i1, i2, false) + bias_fp;
		// P("!2bias="+bias);

		bias_fp = -bias;// FloatUtil.div((b_low_fp + b_up_fp), FloatUtil.TWO);
		P("==bias_fp:" + bias_fp);

		P("f(i 0):" + getFunctionOutputFloat(0,false));
		P("f(i 1):" + getFunctionOutputFloat(1,false));
		// System.out.println("tsII: calling smo 10, d");
		// smoInfo10();

		// last ok

		// if (info) {
		// int objEnd = getObjectiveFunctionFP();
		// if (objEnd < objStart)
		// System.out.println("Objectivefunction not descreasing. Diff="
		// + ABC.fpToStr(objStart - objEnd));
		// int alphaCheck = 0;
		// for (int i = 0; i < m; i++)
		// alphaCheck = ABC.add(alphaCheck, ABC.mul(y_fp[i], alpha_fp[i]));
		// if (ABC.abs(alphaCheck) > alphaTol_fp)
		// System.out.println("Exit(+" + takeStepCount
		// + "): Alphas*y does not add up to 0="
		// + ABC.fpToStr(alphaCheck));
		// }
		// System.out.println("ts II ok");
		// smoInfo();
		// if(gohome) // and do nothing (will exit the smo after a copuple of
		// iterations if it worked)
		// return false;
		// System.out.print("TAKESTEP END:");
		// System.out.println(takeStepCount);
		takeStepCount++;
		takeStepResult = true;
		takeStepFlag = false;
		return true;
	}

	static int changed;
	static boolean inner_loop_success;

	/**
	 * Method innerLoop, which is the inner loop that iterates until the
	 * examples in the inner loop are self consistent.
	 * 
	 * @return the number of changed examples
	 */
	static int innerLoop() {
		changed = 0;
		inner_loop_success = true;
		do {
			i1 = i_up;
			i2 = i_low;
			// System.out.print("INNERLOOP, i1=");
			// System.out.print(i1);
			// System.out.print(", i2=");
			// System.out.println(i2);
			// Async
			// Dbg.wr("SP SM:",Native.getSP());
			takeStep();
			takeStepFlag = true;
			// Wait until takeStep() is done

			// while (takeStepFlag) { // TODO:reenable
			// smo.waitForNextPeriod();
			// }
			inner_loop_success = takeStepResult;

			// inner_loop_success = takeStep();

			if (inner_loop_success) {
				changed++;
			}
			if (!inner_loop_success) {
				break;
			}
			if (b_up_fp > FloatUtil.sub(b_low_fp, FloatUtil.mul(FloatUtil.TWO,
					tol_fp))) {
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
		alpha_fp = new float[m];
		alpha_index_sorted = new int[m];
		findexset = new int[m];
		findexset0 = new int[m];
		findexset0Size = 0;
		findexset0pos = new int[m];
		// System.out.println("A initParams()");
		c_fp = FloatUtil.mul(FloatUtil.ONE, FloatUtil.intToFp(1));
		// System.out.println("Bd initParams()");
		bias_fp = 0;
		// System.out.println("Ca initParams()");
		eps_fp = FloatUtil.div(FloatUtil.ONE, FloatUtil.intToFp(100));
		// System.out.println("Cb initParams()");
		tol_fp = FloatUtil.div(FloatUtil.ONE, FloatUtil.intToFp(10));
		// System.out.println("Cc initParams()");
		alphaTol_fp = FloatUtil.div(FloatUtil.ONE, FloatUtil.intToFp(100));

		KFloat.setSigma2(FloatUtil.mul(FloatUtil.ONE, FloatUtil.ONE));
		KFloat.setKernelType(KFloat.DOTKERNEL);// GAUSSIANKERNEL or DOTKERNEL

		// KABC.setSigma2(ABC.ONE);

		// Kernel type must be set first
		KFloat.setData(data_fp);

		for (int i = 0; i < m; i++) {
			findexset0pos[i] = -1;
			findexset0[i] = -1;
			findexset[i] = calculatefindex(i);
			alpha_index_sorted[i] = i;
		}
		fcache_fp = new float[m];
		// Kernel cache init
		kernelCache_fp = new float[m];
		// Dbg.wr("SP: ",Native.getSP());
		for (int i = 0; i < m; i++) {
			// Dbg.wr("SP1: ",Native.getSP());
			kernelCache_fp[i] = getKernelOutputFloat(i, i, false);
		}
		// Dbg.wr("SP: ",Native.getSP());
		// System.out.println("C initParams()");
		// _low initialization, class 2 = -1 (keerthi99)
		// _up initialization, class 1 = +1
		b_up_fp = FloatUtil.intToFp(-1);
		b_low_fp = FloatUtil.intToFp(1);
		for (int i = 0; i < m; i++) {
			if (y_fp[i] == FloatUtil.ONE) {// Classs 1
				i_up = i;
				fcache_fp[i] = FloatUtil.intToFp(-1);
			} else if (y_fp[i] == -FloatUtil.ONE) // Class 2
			{
				i_low = i;
				fcache_fp[i] = FloatUtil.intToFp(1);
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
	 * Method fcacheDeltaFP, which returns the delta update for the fcache for a
	 * given point.
	 * 
	 * @param i
	 *            - the point
	 * @return fcache delta
	 */
	// int fcacheDeltaFP(int i) {
	// return ABC.add((ABC.mul(ABC.mul(y_fp[i1], ABC.sub(a1_fp, alph1_fp)),
	// getKernelOutputFP(i1, i, true))), (ABC.mul(ABC.mul(y_fp[i2], ABC.sub(
	// a2_fp, alph2_fp)), getKernelOutputFP(i, i2, true))));
	// }
	/**
	 * Method calculatefindex, which return which set the point belongs to based
	 * on the alpha_fp value and the target.
	 * 
	 * @param alpha_fp
	 *            - the alpha_fp value
	 * @return target - the target value
	 */
	static int calculatefindex(int i) {
		// System.out.print("SP:");
		// System.out.println(Native.getSP());

		int retVal = -1;
		if (alpha_fp[i] > alphaTol_fp) {
			if (alpha_fp[i] < FloatUtil.sub(c_fp, alphaTol_fp)) {
				retVal = 0;
			} else if (y_fp[i] == FloatUtil.ONE) {
				retVal = 3;
			} else {
				retVal = 2;
			}
		} else if (y_fp[i] == FloatUtil.ONE) {
			retVal = 1;
		} else {
			retVal = 4;
		}
		// TODO: remove when checked it works
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
	 * @param i
	 *            - the point
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
				 * newfindexset0 = new int[findexset0.length + 1]; // Assign the
				 * i'th element to the new position
				 * newfindexset0[findexset0.length] = i; // Update the position
				 * array findexset0pos[i] = findexset0.length; // Copy the old
				 * array into the new System.arraycopy(findexset0, 0,
				 * newfindexset0, 0, findexset0.length); findexset0 = null;
				 * findexset0 = newfindexset0;
				 */
			}
			// The point has left I_0
			if (findexset[i] == 0) {
				if (findexset0pos[i] != findexset0Size - 1) {
					findexset0[findexset0pos[i]] = findexset0[findexset0Size - 1];
					findexset0pos[findexset0[findexset0Size - 1]] = findexset0pos[i];
				}
				findexset0Size--;

				/*
				 * // The new reduced array int[] newfindexset0 = new
				 * int[findexset0.length - 1]; // First part copy
				 * System.arraycopy(findexset0, 0, newfindexset0, 0,
				 * findexset0pos[i]); // Second part copy if (findexset0pos[i] <
				 * (findexset0.length - 1)) { System.arraycopy(findexset0,
				 * findexset0pos[i] + 1, newfindexset0, findexset0pos[i],
				 * findexset0.length - findexset0pos[i] - 1); } // Update the
				 * position array by decrementing the // indices that are in the
				 * "upper" part of the findex= array for (int j =
				 * findexset0pos[i] + 1; j < findexset0.length; j++) {
				 * findexset0pos[findexset0[j]]--; } findexset0 = null;
				 * findexset0 = newfindexset0;
				 */
			}
			// Update findexset
			findexset[i] = index;
		}
	}

	/**
	 * Method getf, which calculates and returns the functional output without
	 * using a bias_fp. see keerti99
	 * 
	 * @param i
	 *            - index
	 * @return the non-biased functional output.
	 */
	// TODO: fix fcache. Test set kernel resolution 12:12 problem
	static float getfFP(int i) {
		// First check if i is in I_0 or i_low or i_up
		// System.out.println("g:"); // +(cnt++));
		if (findexset[i] == 0 || i == i_low || i == i_up) {
			return fcache_fp[i];
		} else {
			// Calculate f using I_0
			float f_fp = 0;
			float f_fp_tmp = 0;
			for (int j = 0; j < m; j++) {
				f_fp_tmp = FloatUtil.mul(y_fp[j], alpha_fp[j]);
				f_fp_tmp = FloatUtil.mul(f_fp_tmp, getKernelOutputFloat(i, j,
						true));
				f_fp = FloatUtil.add(f_fp, f_fp_tmp);
			}
			f_fp = FloatUtil.sub(f_fp, y_fp[i]);
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
	 * Method updateiandbfully, which will update b_low_fp, b_up_fp, i_low,
	 * i_up. The update is a partial one using only the point previously in I0
	 * or i_low i_up itself. The method loops over all examples.
	 */
	static void updateiandbfully() {
		// Compute i_low, b_low_fp and i_up, b_up_fp
		// System.out.println("updatefully A");
		// printScalar("b_low_fp",b_low_fp);
		b_low_fp = FloatUtil.intToFp(-10000);
		// b_low_fp = ABC.MIN;
		b_up_fp = FloatUtil.intToFp(10000);// ABC.MAX;
		i_low = -1;
		i_up = -1;
		// System.out.println("updatefully B");
		// printScalar("b_low_fp",b_low_fp);
		// printSMOInfo = true;
		// while(printSMOInfo)
		// smo.waitForNextPeriod();

		for (int i = 0; i < m; i++) {
			// I0
			if (findexset[i] == 0) {
				if (y_fp[i] == FloatUtil.ONE) {
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

		// printSMOInfo = true;
		// while(printSMOInfo)
		// smo.waitForNextPeriod();

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
	 * update is a partial one using only the point previously in I0 or i_low
	 * i_up itself. b_low is max(I0,I3,I4)<br>
	 * b_max is min(I0,I1,I2)
	 */
	static void updateiandbpartially() {
		// System.out.println("UPDATEIANDBPARTIALLY");
		// System.out.println("upd -1");
		// printSMOInfo = true;
		// while(printSMOInfo)
		// smo.waitForNextPeriod();

		// Compute i_low, b_low_fp and i_up, b_up_fp
		b_low_fp = FloatUtil.intToFp(-10000);// ABC.MIN;
		// System.out.println("upd -1 A");
		// printSMOInfo = true;
		// while(printSMOInfo)
		// smo.waitForNextPeriod();
		b_up_fp = FloatUtil.intToFp(10000);// ABC.MAX;
		// System.out.println("upd -1 B");
		// printSMOInfo = true;
		// while(printSMOInfo)
		// smo.waitForNextPeriod();
		i_low = -1;
		// System.out.println("upd -1 C");
		// printSMOInfo = true;
		// while(printSMOInfo)
		// smo.waitForNextPeriod();
		i_up = -1;// TODO: check below
		// System.out.println("upd -1 D");
		// printSMOInfo = true;
		// while(printSMOInfo)
		// smo.waitForNextPeriod();
		// TODO: Error when findexset0size == 0
		// System.out.println("upd 0");
		// printSMOInfo = true;
		// while(printSMOInfo)
		// smo.waitForNextPeriod();
		// System.out.println("start loop");
		for (int i = 0; i < findexset0Size; i++) {
			// System.out.print("upd pre loop:");
			// System.out.println(i);

			// printSMOInfo = true;
			// while(printSMOInfo)
			// smo.waitForNextPeriod();
			if (fcache_fp[findexset0[i]] >= b_low_fp) {
				b_low_fp = fcache_fp[findexset0[i]];
				i_low = findexset0[i];
			}
			if (fcache_fp[findexset0[i]] < b_up_fp) {
				b_up_fp = fcache_fp[findexset0[i]];
				i_up = findexset0[i];
			}

			// System.out.print("upd post loop:");
			// System.out.println(i);

		}
		// System.out.println("upd 1");
		// printSMOInfo = true;
		// while(printSMOInfo)
		// smo.waitForNextPeriod();

		// Now check i1 if not in I_0
		if (findexset[i1] != 0) {
			if (findexset[i1] == 1 || findexset[i1] == 2) { // I_1 or I_2
				if (b_up_fp > fcache_fp[i1] || findexset0Size == 1) { // TODO:
					// Check
					// the
					// ">"
					// if
					// should
					// be >=
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
		// System.out.println("upd 2");
		// printSMOInfo = true;
		// while(printSMOInfo)
		// smo.waitForNextPeriod();

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
		// System.out.println("upd 3");
		// printSMOInfo = true;
		// while(printSMOInfo)
		// smo.waitForNextPeriod();

		if (i_low == -1 || i_up == -1) {
			System.out
					.println("updateiandbpartially() problem: i_low or i_up is not set");
			System.out.println("findexset0Size:" + findexset0Size);
			System.out.println("i1:" + i1);
			System.out.println("i2:" + i2);
			System.out.println("i_low:" + i_low);
			System.out.println("i_up:" + i_up);
			System.out.println("findexset[i1]:" + findexset[i1]);
			System.out.println("findexset[i2]:" + findexset[i2]);
			for (int i = 0; i < findexset0Size; i++) {
				System.out.print("fcache_fp[findexset0[");
				System.out.print(i);
				System.out.print("]]:");
				System.out.print(fcache_fp[findexset0[i]]);
				System.out.print(" ");
				// System.out.println(ABC.fpToInt(fcache_fp[findexset0[i]]));
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
	 *            the example to check
	 * @return true if example violates KKT
	 */
	static boolean isKktViolated(int p) {
		boolean violation = true;
		float f_fp = getFunctionOutputFloat(p,false);
		// Is alpha_fp on lower bound?
		if (alpha_fp[p] == 0) {
			if (FloatUtil.mul(y_fp[p], f_fp) >= FloatUtil.sub(1, eps_fp)) {
				violation = false;
			}
		} // or is alpha_fp in non-bound, NB, set?
		else if (alpha_fp[p] > 0 && alpha_fp[p] < c_fp) {
			if (FloatUtil.mul(y_fp[p], f_fp) > FloatUtil.sub(1, eps_fp)
					&& FloatUtil.mul(y_fp[p], f_fp) < FloatUtil.add(1, eps_fp)) {
				violation = false;
			}
		} // alpha_fp is on upper bound
		else {
			if (FloatUtil.mul(y_fp[p], f_fp) <= FloatUtil.add(1, eps_fp)) {
				violation = false;
			}
		}
		return violation;
	}

	/**
	 * Method getObjectiveFunction, which calculates and returns the value of
	 * the objective function based on the values in the alpha_fp array.
	 * 
	 * @return the objective function (6.1 in Christianini).
	 */
	static float getObjectiveFunctionFP() {
		// TODO: Check how often this is called and tune if possible
		float objfunc_fp = 0;
		for (int i = 0; i < m; i++) {
			// Don't do the calculation for zero alphas
			if (alpha_fp[i] > 0) {
				objfunc_fp = objfunc_fp + alpha_fp[i];
				for (int j = 0; j < m; j++) {
					if (alpha_fp[j] > 0) {
						objfunc_fp = FloatUtil.sub(objfunc_fp, FloatUtil.mul(
								FloatUtil.mul(FloatUtil.mul(FloatUtil.mul(
										FloatUtil.mul(FloatUtil.HALF, y_fp[i]),
										y_fp[j]), alpha_fp[i]), alpha_fp[j]),
								getKernelOutputFloat(i, j, true)));
					}
				}
			}
		}
		return objfunc_fp;
	}

	/**
	 * Method calculatedError, which calculates the error from from scratch.
	 * 
	 * @param p
	 *            - point to calculte error for
	 * @return calculated error
	 */
	static float getCalculatedErrorFP(int p) {
		return FloatUtil.sub(getFunctionOutputFloat(p,false), y_fp[p]);
	}

	/**
	 * Method getFunctionOutput, which will return the functional output for
	 * point p.
	 * 
	 * @param p
	 *            - the point index
	 * @param par
	 *            - true if to be done in parallel
	 * @return the functinal output
	 */
	static float getFunctionOutputFloat(int p, boolean par) {
		float functionalOutput_fp = 0;
		if (par) {
			ParallelExecutor pe = new ParallelExecutor();
			System.out.print("m:");
			System.out.println(m);
			pe.executeParallel(new SVMHelp(), m);
			functionalOutput_fp = SVMHelp.functionalOutput_fp;
			// Original version
			/*
			 * float functionalOutput_fp = 0; float kernelOutput_fp = 0; for
			 * (int i = 0; i < m; i++) { // Don't do the kernel if it is
			 * epsequal if (alpha_fp[i] > 0) { kernelOutput_fp =
			 * getKernelOutputFloat(i, p, true); functionalOutput_fp =
			 * FloatUtil.add(functionalOutput_fp, FloatUtil.mul(FloatUtil.mul(
			 * alpha_fp[i], y_fp[i]), kernelOutput_fp)); } } // Make a check
			 * here to see any alphas has been modified after
			 * functionalOutput_fp = FloatUtil.sub(functionalOutput_fp,
			 * bias_fp); return functionalOutput_fp;
			 */
		} else {
			for (int i = 0; i < m; i++) {
				// Don't do the kernel if it is epsequal
				if (alpha_fp[i] > 0) {
					functionalOutput_fp += y_fp[i] * alpha_fp[i]
							* getKernelOutputFloat(i, p, false);
				}
			} // Make a check here to see any alphas has been modified after
			functionalOutput_fp -= bias_fp;
		}
		return functionalOutput_fp;
	}

	/**
	 * Method getKernelOutput, which returns the kernel of two points.
	 * 
	 * @param i1
	 *            - index of alpha_fp 1
	 * @param i2
	 *            - index of alpha_fp 2
	 * @param useCache
	 *            TODO
	 * @param useCache
	 *            - will use the cache if possible
	 * @return kernel output
	 */
	static float getKernelOutputFloat(int i1, int i2, boolean useCache) {
		if (i1 == i2 && useCache) {
			return kernelCache_fp[i1];
		}
		return KFloat.kernel(i1, i2);
	}

	/**
	 * Method getEta, which returns eta_fp = 2*k12-k11-k22
	 * 
	 * @param i1
	 *            - index of first point
	 * @param i2
	 *            -index of second point
	 * @return double - eta_fp
	 */
	static float getEtaFP(int i1, int i2) {
		float eta_fp;
		float eta_fp_tmp;
		float kernel11_fp, kernel22_fp, kernel12_fp;
		kernel11_fp = getKernelOutputFloat(i1, i1, true);
		kernel22_fp = getKernelOutputFloat(i2, i2, true);
		kernel12_fp = getKernelOutputFloat(i1, i2, true);
		eta_fp = FloatUtil.sub(FloatUtil.sub(FloatUtil.mul(FloatUtil.TWO,
				kernel12_fp), kernel11_fp), kernel22_fp);
		return eta_fp;
	}

	/**
	 * Method getLowerClip, which returns the lower clip value for some pair of
	 * Lagrange multipliers. Pls. refer to Nello's book for more info.
	 * 
	 * @param i1
	 *            - first point
	 * @param i2
	 *            - second point
	 * @return the lower clip value
	 */
	static float getLowerClipFP(int i1, int i2) {
		float u_fp = 0;
		if (y_fp[i1] == y_fp[i2]) {
			u_fp = FloatUtil.sub(FloatUtil.add(alpha_fp[i1], alpha_fp[i2]),
					c_fp);
			if (u_fp < 0) {
				u_fp = 0;
			}
		} else {
			u_fp = FloatUtil.sub(alpha_fp[i2], alpha_fp[i1]);
			if (u_fp < 0) {
				u_fp = 0;
			}
		}
		return u_fp;
	}

	/**
	 * Method getUpperClip, which will return the upper clip based on two
	 * Lagrange multipliers.
	 * 
	 * @param i1
	 *            - first point
	 * @param i2
	 *            - second point
	 * @return the upper clip
	 */
	static float getUpperClipFP(int i1, int i2) {
		float v_fp = 0;
		if (y_fp[i1] == y_fp[i2]) {
			v_fp = FloatUtil.add(alpha_fp[i1], alpha_fp[i2]);
			if (v_fp > c_fp) {
				v_fp = c_fp;
			}
		} else {
			v_fp = FloatUtil.add(c_fp, FloatUtil
					.sub(alpha_fp[i2], alpha_fp[i1]));
			if (v_fp > c_fp) {
				v_fp = c_fp;
			}
		}
		return v_fp;
	}

	static public int getTrainingErrorCountFP() {
		P("getTrainingErrorCountFP");
		int errorCount = 0;
		for (int i = 0; i < m; i++) {
			float fout_fp = getFunctionOutputFloat(i,false);
			System.out.print("Tr ");
			System.out.print(i);
			System.out.print(" fn ");
			System.out.print(fout_fp);
			System.out.print(" y_fp ");
			System.out.println(y_fp[i]);
			if (fout_fp > 0 && y_fp[i] < 0) {
				errorCount++;
				System.out.println(" e 1 ");
			} else if (fout_fp < 0 && y_fp[i] > 0) {
				errorCount++;
				System.out.println(" e 0 ");
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
	static float[] calculateWFP() {
		float[] w_fp;
		w_fp = new float[n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				w_fp[j] = FloatUtil.add(w_fp[j], FloatUtil.mul(FloatUtil.mul(
						y_fp[i], alpha_fp[i]), data_fp[i][j]));
			}
		}
		return w_fp;
	}

	/**
	 * Method isExampleBound, which will return true if the point p is on the
	 * bound as defined as less then (0+tol_fp) or greater than (C-tol_fp).
	 * 
	 * @param p
	 *            - index of point
	 * @return true if p is on bound
	 */
	static boolean isExampleOnBound(int p) {
		return alpha_fp[p] < tol_fp
				|| alpha_fp[p] > FloatUtil.sub(c_fp, tol_fp);
	}

	/**
	 * Method getFunctionOutput, which will return the functional output for
	 * point represented by a input vector only.
	 * 
	 * @param xtest
	 *            - the input vector
	 * @return the functinal output
	 */
	static public float getFunctionOutputTestPointFP(float[] xtest) {
		float functionalOutput_fp = 0;
		float[][] data_fp_local = data_fp;
		int m = data_fp_local.length;
		float func_out = 0;
		// System.out.println("---ALIVE1m---" + m);
		int n = xtest.length;
		int n2 = data_fp_local[0].length;
		// System.out.println("---ALIVE1n2---" + n2);
		// System.out.println("---ALIVE1n---" + n);
		// System.out.println("---ALIVE11---");
		// RT bound it to ALPHA_RT
		for (int i = 0; i < ALPHA_RT; i++) { // @WCA loop=5
			// System.out.println("---ALIVE1111---" + i);

			n = xtest.length;
			// MS: is the following bound correct?
			while (n != 0) { // @WCA loop=2
				n = n - 1;
				// System.out.println("---ALIVEnin---" + n);
				// System.out.println("---ALIVEnim---" + m);
				// functionalOutput_fp += KABC.kernelX(i);
				func_out += (data_fp_local[alpha_index_sorted[i]][n])
						* (xtest[n]);
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
	 * Method getFunctionOutput, which will return the functional output for
	 * point represented by a input vector only.
	 * 
	 * @param xtest
	 *            - the input vector
	 * @return the functinal output
	 */
	static public float getFunctionOutputTestPointFP_OOAD(float[] xtest) {
		float functionalOutput_fp = 0;
		float kernelOutput_fp = 0;
		KFloat.setX(xtest);
		for (int i = 0; i < m; i++) {
			// Don't do the kernel if it is epsequal
			if (alpha_fp[i] > 0) {
				kernelOutput_fp = KFloat.kernelX(i);
				functionalOutput_fp = FloatUtil.add(functionalOutput_fp,
						FloatUtil.mul(FloatUtil.mul(alpha_fp[i], y_fp[i]),
								kernelOutput_fp));
			}
		} // Make a check here to see any alphas has been modified after
		functionalOutput_fp = FloatUtil.sub(functionalOutput_fp, bias_fp);
		return functionalOutput_fp;
	}

	static public void setData_fp(float[][] data_fp) {
		SMOBinaryClassifierFloat.data_fp = data_fp;
	}

	static public void setY_fp(float[] y_fp) {
		SMOBinaryClassifierFloat.y_fp = y_fp;
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
			// while (printSMOInfo)
			// smo.waitForNextPeriod();
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
		for (int i = 0; i < m; i++) {
			if (alpha_fp[i] > alphaTol_fp)
				svs++;
		}
		printScalar("#sv", svs);
		printScalar("training err cnt", getTrainingErrorCountFP());

		// printScalar("GC free words",GC.free());
		// printScalar("ABC.MAX",ABC.MAX);
		// printScalar("ABC.MIN",ABC.MIN);
		// printScalar("sp",Native.rd(com.jopdesign.sys.Const.IO_WD));
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

	static void printScalar(String str, float sca) {
		System.out.print(str);
		System.out.print(':');
		System.out.println(sca);
		for (int i = 0; i < 100; i++)
			;
	}

	static void printVector(String str, float[] ve) {
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

	static void printMatrix(String str, float[][] ma) {
		for (int i = 0; i < ma.length; i++) {
			System.out.print(str);
			System.out.print("[");
			System.out.print(i);
			System.out.print("]");
			System.out.print(":");
			printVector("", ma[i]);
		}
	}

	static public int getSV() {
		int svs = 0;
		for (int i = 0; i < m; i++) {
			if (alpha_fp[i] > alphaTol_fp)
				svs++;
		}
		return svs;
	}

	// sorts the indeces of the alphas
	static void sortAlphaIndex() {

		int changed;
		if (PRINT)
			System.out.println("SMO.sortalphaindex");
		do {
			changed = 0;
			if (PRINT)
				System.out.println("SMO.sort1");
			for (int i = 0; i < (m - 1); i++) {
				if (alpha_fp[alpha_index_sorted[i]] < alpha_fp[alpha_index_sorted[i + 1]]) {
					int tmp = alpha_index_sorted[i];
					alpha_index_sorted[i] = alpha_index_sorted[i + 1];
					alpha_index_sorted[i + 1] = tmp;
					changed++;
				}
			}
			if (PRINT)
				System.out.println("SMO.sort2");
			if (PRINT) {
				System.out.println("Sorting...");
			}

		} while (changed > 0);
	}

	static void P(String s) {
		if (PRINT)
			System.out.println(s);
	}

	/**
	 * Do not instanciate me.
	 */
	private SMOBinaryClassifierFloat() {
	}

	private static class SVMHelp implements Execute {

		static Object lock;

		static int p; // the test point index

		static float functionalOutput_fp;

		public void execute(int nr) {
			System.out.println("Parallel in core 0:nr=" + nr);
			if (alpha_fp[nr] > 0) {

				float kernelOutput_fp = getKernelOutputFloat(nr, p, true);
				float tmp = ((alpha_fp[nr] * y_fp[nr]) * kernelOutput_fp);
				synchronized (lock) {
					functionalOutput_fp += tmp;
					functionalOutput_fp += bias_fp;
				}
			}
		}

		void setP(int p) {
			SVMHelp.p = p;
			functionalOutput_fp = 0;
		}

		float getFuncOut() {
			return functionalOutput_fp;
		}
	}

}
