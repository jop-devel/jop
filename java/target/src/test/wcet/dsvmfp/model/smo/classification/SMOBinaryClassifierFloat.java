package wcet.dsvmfp.model.smo.classification;

import cmp.Execute;
import cmp.ParallelExecutor;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

import wcet.dsvmfp.model.smo.kernel.FloatUtil;
import wcet.dsvmfp.model.smo.kernel.KFloat;

/**
 * Class SMOBinaryClassifier with float.
 */
public class SMOBinaryClassifierFloat {

    // make false to get reproduceable results
    // or make true to get different stochastic index generation
    boolean RANDOMLOOP = false;

	// final boolean PRINT = true;

	// enum TRACELEVELS {TR0, TR1, TR2, TR3, TR4};
	final int TR0 = 0;// TR0: none
	final int TR1 = 1;// TR1: errors and real time
	final int TR2 = 2;// TR2: debug
	final int TR3 = 3;// TR3: normal info
	final int TR4 = 4;// TR4: verbose

	// Set this as needed
	final int TRACELEVEL = TR1;

	/** Number of lagrange multipliers in deployed RT model */
	public final int ALPHA_RT = 2;

	/** The [m] Lagrange multipliers. */
	public float[] alph;
	static float alph1, alph2;
	static float y1, y2;

	/** The target vector of {-1,+1}. */
	static public float[] target;

	/** The data vector [rows][columns]. One observation is one row */
	static public float[][] point;
	/** The high bound. */
	static public float C;

	/** The error tolerance. */
	static public float tol;

	/** The error tolerance, that is used for KKT violation checks. */
	static public float eps;

	// E1 and E1 as used in takestep
	static public float E1, E2;

	/** The bias_fp. */
	static public float bias;

	static public int i1, i2;

	float WTemp;

	/**
	 * The number of training points. It is declared final to avoid
	 * synchronization problems.
	 */
	static public int m;

	/** The input space dimensionality. */
	static public int n;

	// ////////////Performance Variables////////////////////
	static public int takeStepCount;

	static public int numChanged;

	static public boolean examineAll;

	static public int loop;

	/**
	 * Method mainRoutine, which estimates the SVM parameters. The parameters
	 * are intialized before the training of the SVM is conducted.
	 *
	 * @return true if the training went well
	 */
	public boolean mainRoutine() {

		// The number of updated that significantly changed the
		// Lagrange multipliers
		numChanged = 0;
		// Indicates if all examples has been tested
		examineAll = true;
		// Reset alpha_fp array , errorarray , b, and C
		takeStepCount = 0;
		initParams();
		System.out.println("After init params");
        //P(1.005f, TR1);
        //smoInfo();
		loop = 0;

float W = getObjectiveFunctionFP();
P("*********************", TR1);
P("W initial:", TR1);
P(W, TR1);
P("*********************", TR1);
// use this to just get one round of training, which is fine sometimes
boolean quickstop = false;

        resetKernelCalls(); // set the counter to zero

		do { // @WCA loop=2
			W = getObjectiveFunctionFP();
			P("*********************", TR1);
			P("loop:", TR1);
			P(loop, TR1);
			if(TRACELEVEL == TR2)
			  smoInfo();
			loop++;

			numChanged = 0;
			if (examineAll) {
				// loop I over all training examples
				for (i2 = 0; i2 < m; i2++) { // @WCA loop=14
					if (examineExample()) {
						numChanged++;
					}
					P("i2:", TR2);
					P(i2, TR2);
				}
				P("W all (after): ", TR2);
				P(getObjectiveFunctionFP(), TR2);
				P("numChanged:", TR2);
				P(numChanged, TR2);
			} else {
				// Inner loop success
				for (i2 = 0; i2 < m; i2++) { // @WCA loop=14
					if (alph[i2] > 0 && alph[i2] < C) {
						if (examineExample()) {
							numChanged++;
						}
					}
					P("i2:", TR2);
					P(i2, TR2);
				}
				P("W inner (after): ", TR2);
				P(getObjectiveFunctionFP(), TR2);
				P("numChanged:", TR2);
				P(numChanged, TR2);
			}
			if (examineAll) {
				examineAll = false;
			} else if (numChanged == 0) {
				examineAll = true;
			}
			// break;


			P("*********************", TR1);
			P("Delta W:", TR1);
			P(getObjectiveFunctionFP()-W, TR1);
			P("*********************", TR1);
			P("", TR1);

		} // stop if improvement is less than 10%
		while (((numChanged > 0 || examineAll) && (getObjectiveFunctionFP()-W)>(0.1*W)) && !quickstop);
		P("SMO.mainroutine.trained", TR4);

		measure();
		smoInfo();
		return true;
	}

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
	public boolean takeStep() {

		float k11, k12, k22;

		P("takeStep(),takeStepCount:", TR4);
		P(takeStepCount, TR4);
		P("i1:", TR4);
		P(i1, TR4);
		P("i2:", TR4);
		P(i2, TR4);

		WTemp = getObjectiveFunctionFP();

		// If the first and second point is the same then return false
		if (i1 == i2) {
			return false;
		}

		alph1 = alph[i1];
		y1 = target[i1];

		P("alph1:", TR4);
		P(alph1, TR4);

		E1 = getfFP(i1) - y1;
		P("f1_fp:", TR4);
		P(getfFP(i1), TR4);

		float s_fp = y1 * y2;

		// Compute L, H
		float L = getLowerClipFP(i1, i2);
		P("L=", TR2);
		P(L, TR2);
		float H = getUpperClipFP(i1, i2);
		P("H=", TR2);
		P(H, TR2);
		if (L == H)
			return false;

		k11 = getKernelOutputFloat(i1, i1);
		k12 = getKernelOutputFloat(i1, i2);
		k22 = getKernelOutputFloat(i2, i2);

		float eta = 2 * k12 - k11 - k22;
		P("eta:", TR4);
		P(eta, TR4);

		// on
		float a2, a1;

		if (eta < 0) {
			a2 = alph2 - (y2 * (E1 - E2)) / eta;
			P("eta < 0: a2:", TR4);
			P(a2, TR4);

			if (a2 < L) {
				a2 = L;
				P("eta < 0: L:", TR4);
				P(L, TR4);
				P("a2:", TR4);
				P(a2, TR4);
			} else if (a2 > H) {
				a2 = H;
				P("eta < 0: H:", TR4);
				P(H, TR4);
				P("a2:", TR4);
				P(a2, TR4);
			}
		} else {

			float tempAlpha2_fp = alph[i2];
			alph[i2] = L;
			float Lobj = getObjectiveFunctionFP();
			alph[i2] = H;
			float Hobj = getObjectiveFunctionFP();
			alph[i2] = tempAlpha2_fp;
			if (Lobj > (Hobj + eps)) {
				a2 = L;
			} else if (Lobj < Hobj - eps) {
				a2 = H;
			} else {
				a2 = alph2;
			}
			P("eta > 0: a2:", TR4);
			P(a2, TR4);

		}

		if (a2 < 1e-8f)
			a2 = 0;
		else if (a2 > C - 1e-8f)
			a2 = C;

		if (Math.abs(a2 - alph2) < eps * (a2 + alph2 + eps))
			return false;

		a1 = alph1 + s_fp * (alph2 - a2);

		// Update threshold to reflect change in Lagrange multipliers
		float bias_a1 = E1 + target[i1] * (a1 - alph1)
				* getKernelOutputFloat(i1, i1) + target[i2] * (a2 - alph2)
				* k12 + bias;

		// Update threshold to reflect change in Lagrange multipliers
		float bias_a2 = E2 + target[i1] * (a1 - alph1)
				* getKernelOutputFloat(i1, i2) + target[i2] * (a2 - alph2)
				* k22 + bias;

		if (!isExampleOnBound(a1)) {
			bias = bias_a1;
			P("bias a1:", TR2);
			P(bias, TR2);
		} else if (!isExampleOnBound(a2)) {
			bias = bias_a2;
			P("bias a2:", TR2);
			P(bias, TR2);
		} else {
			bias = (bias_a1 + bias_a2) / 2;
			P("bias (a1+a2)/2:", TR2);
			P(bias, TR2);
		}

		// Update weight vector to reflect change in a1 & a2, if linear SVM

		// Update error cache using new Lagrange multipliers

		// Store a1 in the alpha array
		alph[i1] = a1;
		// Store a2 in the alpha array
		alph[i2] = a2;

		// Checking (can be removed later)
		P("f(i 0):", TR4);
		P(getFunctionOutputFloat(0, false), TR4);
		P("f(i 1):", TR4);
		P(getFunctionOutputFloat(1, false), TR4);


		if(WTemp> getObjectiveFunctionFP()){
			P("Objectivefunction error!!!!", TR1);
			P(WTemp, TR1);
			P(getObjectiveFunctionFP(),TR1);
			float sumcheck = 0;
			for(int i=0;i<m;i++){
				sumcheck += target[i]*alph[i];
			}
			P("Check:", TR1);
			P(sumcheck, TR1);
			smoInfo();
			while(true){}
		}
		P("W:", TR1);
		P(getObjectiveFunctionFP(),TR1);
		P("kernelCalls:",TR1);
		P(getKernelCalls(), TR1);

		takeStepCount++;
		// BUGTEST 12
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
	boolean examineExample() {

		// System.gc();
		P("-------ea--------", TR2);
		y2 = target[i2];
		alph2 = alph[i2];
		P("S: alph2:", TR4);
		P(alph2, TR4);
		E2 = getfFP(i2) - target[i2];
		P("f2:", TR4);
		P(getfFP(i2), TR4);
		float r2 = E2 * y2;
		if ((r2 < -tol && alph2 < C) || (r2 > tol && alph2 > 0)) {
			int nonBounds = 0;
			for (int i = 0; i < m; i++) {
				if (!isExampleOnBound(i)) {
					nonBounds++;
					if (nonBounds > 1)
						break;
				}
			}

			// if(number of non-zero & non-C alpha > 1)
			if (nonBounds > 1) {
				// i1 = result of second choice heuristic
				secondChoiceHeuristic();
				P("ea:i2", TR2);
				P(i2, TR2);
				P("ea:i1", TR2);
				P(i1, TR2);
				P("W (ea:second heu): ", TR2);
				P(getObjectiveFunctionFP(), TR2);
				P("numChanged:", TR2);
				P(numChanged, TR2);
				if (takeStep()) {

					return true;
				}
			}
			// loop over all non-zero and non-C alpha, starting at random point
			boolean firstTime = true;
			int i;
			while ((i = randomLoop(firstTime)) != -1) {
				firstTime = false;
				if (!isExampleOnBound(i)) {
					i1 = i;
					P("ea:i2", TR2);
					P(i2, TR2);
					P("ea:i1", TR2);
					P(i1, TR2);
					P("W (ea:all non bound): ", TR2);
					P(getObjectiveFunctionFP(), TR2);
					P("numChanged:", TR2);
					P(numChanged, TR2);
					if (takeStep()) {

						return true;
					}
				}
			}
			// loop over all possible i1, starting at random point
			firstTime = true;
			while ((i = randomLoop(firstTime)) != -1) {
				firstTime = false;
				i1 = i;
				P("ea:i2", TR2);
				P(i2, TR2);
				P("ea:i1", TR2);
				P(i1, TR2);
				P("W (ea: all): ", TR2);
				P(getObjectiveFunctionFP(), TR2);
				P("numChanged:", TR2);
				P(numChanged, TR2);
				if (takeStep()) {

					return true;
				}
			}

		}

		return false;
	}

	void secondChoiceHeuristic() {
		float maxabs = 0f;
		i1 = 0;
		float abs = 0f;
		for (int i = 0; i < m; i++) {
			if (!isExampleOnBound(i)) {
				if ((abs = Math.abs(getError(i) - E2)) >= maxabs) {
					maxabs = abs;
					i1 = i;
				}
			}
		}
	}

	int firstIndex = -1;
	int nextIndex = -1;

	/**
	 * Will generate a random point if lastIndex == -1. It will return -1 when
	 * full loop is done.
	 *
	 * @param firstTime
	 *            true for the first call;
	 * @return -1 when done
	 */
	int randomLoop(boolean firstTime) {
		// random index init for first call
		if (firstTime) {
			if(RANDOMLOOP)
			  firstIndex = nextIndex = (int) (System.currentTimeMillis() % m);
            else
               firstIndex = nextIndex = 0;

			if (firstIndex < 0) {
				firstIndex *= -1;
				nextIndex *= -1;
			}
			P("firstIndex=", TR3);
			P(firstIndex, TR3);
			return firstIndex;
		}

		// next index
		nextIndex = nextIndex + 1;
		// start from 0 if past last index
		if (nextIndex > (m - 1)) {
			nextIndex = 0;
		}
		if (nextIndex != firstIndex) {
			return nextIndex;
		}

		// stop: it has looped
		firstIndex = -1;
		nextIndex = -1;
		return -1;
	}

	/**
	 * Method initParams, which will init the parameters of the SMO algorithm.
	 * This method should only be called once, which would be just before the
	 * mainRoutine().
	 */
	void initParams() {

		m = target.length;
		n = point[0].length;
		// initialize alpha array to zero
		alph = new float[m];

		C = 10f;
		bias = 0f;
		eps = 0.01f;

		tol = 0.01f;

		KFloat.setSigma2(FloatUtil.mul(FloatUtil.ONE, FloatUtil.ONE));
		KFloat.setKernelType(KFloat.DOTKERNEL);// GAUSSIANKERNEL or DOTKERNEL

		// Kernel type must be set first
		KFloat.setData(point);

		int loopStartInit = 1;
	}

	/**
	 * Method getf, which calculates and returns the functional output without
	 * using a bias_fp. see keerti99
	 *
	 * @param i
	 *            - index
	 * @return the non-biased functional output.
	 */
	float getfFP(int i) {
		float f_fp = 0;
		for (int j = 0; j < m; j++) {
			if (alph[j] > 0) {
				f_fp += target[j] * alph[j] * getKernelOutputFloat(i, j);
			}
		}
		f_fp -= bias;
		return f_fp;
	}


	/**
	 * The error of the training example i.
	 *
	 * @param i
	 * @return error
	 */
	float getError(int i) {
		return getfFP(i) - target[i];
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
	boolean isKktViolated(int p) {
		boolean violation = true;
		float f_fp = getFunctionOutputFloat(p, false);
		// Is alpha_fp on lower bound?
		if (alph[p] == 0) {
			if (target[p] * f_fp >= 1 - eps) {
				violation = false;
			}
		} // or is alpha_fp in non-bound, NB, set?
		else if (alph[p] > 0 && alph[p] < C) {
			if (target[p] * f_fp > 1 - eps && target[p] * f_fp < 1 + eps) {
				violation = false;
			}
		} // alpha_fp is on upper bound
		else {
			if (target[p] * f_fp <= 1 + eps) {
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
	float getObjectiveFunctionFP() {
		// TODO: Check how often this is called and tune if possible
		float objfunc_fp = 0;
		for (int i = 0; i < m; i++) {
			// Don't do the calculation for zero alphas
			if (alph[i] > 0) {
				objfunc_fp = objfunc_fp + alph[i];
				for (int j = 0; j < m; j++) {
					if (alph[j] > 0) {
						//objfunc_fp -= objfunc_fp - 0.5 * target[i] * target[j]
						objfunc_fp -= 0.5 * target[i] * target[j]
								* getKernelOutputFloat(i, j) * alph[i]
								* alph[j];
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
	float getCalculatedErrorFP(int p) {
		return FloatUtil.sub(getFunctionOutputFloat(p, false), target[p]);
	}

	/**
	 * Only one executor allowed.
	 */
	ParallelExecutor pe = new ParallelExecutor();

	SVMHelp svmHelp = new SVMHelp();

	/**
	 * Method getFunctionOutput, which will return the functional output for
	 * point p.
	 *
	 * @param p
	 *            - the point index
	 * @param parallel
	 *            - true if to be done in parallel
	 * @return the functinal output
	 */
	public float getFunctionOutputFloat(int p, boolean parallel) {
		float functionalOutput_fp = 0;
		svmHelp.p = p;
		if (parallel) {
			svmHelp.functionalOutput_fp = 0.0f;
			//System.out.print("m:");
			//System.out.println(m);
			pe.executeParallel(new SVMHelp(), m);
			svmHelp.functionalOutput_fp -= bias;
			functionalOutput_fp = svmHelp.functionalOutput_fp;
		} else {
			for (int i = 0; i < m; i++) { // @WCA loop=14
				// Don't do the kernel if it is epsequal
				if (alph[i] > 0) {
					functionalOutput_fp += target[i] * alph[i]
							* getKernelOutputFloat(i, p);
				}
			} // Make a check here to see any alphas has been modified after
			functionalOutput_fp -= bias;
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
	 * @return kernel output
	 */
	float getKernelOutputFloat(int i1, int i2) {

		kernelCalls++;

		return KFloat.kernel(i1, i2);
	}

	// keeps track of how many times the kernel function has been called
	int kernelCalls = 0;

	/**
	 * Return the kernel calls;
	 */
	public int getKernelCalls(){
	  return kernelCalls;
	}

    /**
     * Set the kernelCalls to zero.
     */
	public void resetKernelCalls(){
		kernelCalls = 0;
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
	float getEtaFP(int i1, int i2) {
		float eta_fp;
		float eta_fp_tmp;
		float kernel11_fp, kernel22_fp, kernel12_fp;
		kernel11_fp = getKernelOutputFloat(i1, i1);
		kernel22_fp = getKernelOutputFloat(i2, i2);
		kernel12_fp = getKernelOutputFloat(i1, i2);
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
	float getLowerClipFP(int i1, int i2) {
		float u_fp = 0;
		if (target[i1] == target[i2]) {
			u_fp = FloatUtil.sub(FloatUtil.add(alph[i1], alph[i2]), C);
			if (u_fp < 0) {
				u_fp = 0;
			}
		} else {
			u_fp = FloatUtil.sub(alph[i2], alph[i1]);
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
	float getUpperClipFP(int i1, int i2) {
		float v_fp = 0;
		if (target[i1] == target[i2]) {
			v_fp = FloatUtil.add(alph[i1], alph[i2]);
			if (v_fp > C) {
				v_fp = C;
			}
		} else {
			v_fp = FloatUtil.add(C, FloatUtil.sub(alph[i2], alph[i1]));
			if (v_fp > C) {
				v_fp = C;
			}
		}
		return v_fp;
	}

	public int getTrainingErrorCountFP() {
		P("getTrainingErrorCountFP", TR4);
		int errorCount = 0;
		for (int i = 0; i < m; i++) {
			float fout_fp = getFunctionOutputFloat(i, false);
			P("Tr:", TR4);
			P(i, TR4);
			P("fn:", TR4);
			P(fout_fp, TR4);
			P("y_fp:", TR4);
			P(target[i], TR4);
			if (fout_fp > 0 && target[i] < 0) {
				errorCount++;
				P("1:errorCount++", TR4);
			} else if (fout_fp < 0 && target[i] > 0) {
				errorCount++;
				System.out.println("0:errorCount++");
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
	float[] calculateWFP() {
		float[] w_fp;
		w_fp = new float[n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				w_fp[j] = FloatUtil.add(w_fp[j], FloatUtil.mul(FloatUtil.mul(
						target[i], alph[i]), point[i][j]));
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
	boolean isExampleOnBound(int p) {
		return alph[p] < tol || alph[p] > (C - tol);
	}

	boolean isExampleOnBound(float aTest) {
		return aTest < tol || aTest > (C - tol);
	}


	/**
	 * Method getFunctionOutput, which will return the functional output for
	 * point represented by a input vector only.
	 *
	 * @param xtest
	 *            - the input vector
	 * @return the functinal output
	 */
	public float getFunctionOutputTestPointFP(float[] xtest) {
		float functionalOutput_fp = 0;
		float[][] data_fp_local = point;
		int m = data_fp_local.length;
		float func_out = 0;
		int n = xtest.length;
		int n2 = data_fp_local[0].length;
		// RT bound it to ALPHA_RT
		for (int i = 0; i < ALPHA_RT; i++) { // @WCA loop=14
			n = xtest.length;
			while (n != 0) { // @WCA loop=14
				n = n - 1;
				// func_out += (data_fp_local[alpha_index_sorted[i]][n])
				// * (xtest[n]);
			}
			// if (alpha_fp[alpha_index_sorted[i]] > 0) {
			//   functionalOutput_fp += func_out;
			// }
			func_out = 0;
		}
		functionalOutput_fp -= bias;
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
	public float getFunctionOutputTestPointFP_OOAD(float[] xtest) {
		float functionalOutput_fp = 0;
		float kernelOutput_fp = 0;
		KFloat.setX(xtest);
		for (int i = 0; i < m; i++) {
			// Don't do the kernel if it is epsequal
			if (alph[i] > 0) {
				kernelOutput_fp = KFloat.kernelX(i);
				functionalOutput_fp = FloatUtil.add(functionalOutput_fp,
						FloatUtil.mul(FloatUtil.mul(alph[i], target[i]),
								kernelOutput_fp));
			}
		} // Make a check here to see any alphas has been modified after
		functionalOutput_fp = FloatUtil.sub(functionalOutput_fp, bias);
		return functionalOutput_fp;
	}

	public void setData_fp(float[][] data_fp) {
		point = data_fp;
	}

	public void setY_fp(float[] y_fp) {
		target = y_fp;
	}

	void gccheck() {
		P("GC free words ", TR4);
		// free() not public
		P(GC.freeMemory(), TR4);
	}

	void sp() {
		P("sp=", TR4);
		P(Native.rd(com.jopdesign.sys.Const.IO_WD), TR4);
	}

	public void smoInfo() {
		// printScalar("wd",Native.rd(Const.IO_WD)); //TODO: Can it be read?
		P("======SMO INFO START======", TR4);
		// printScalar("sp",Native.rd(com.jopdesign.sys.Const.IO_WD));
		printScalar("W", getObjectiveFunctionFP());
		printScalar("i1", i1);
		printScalar("i2", i2);
		printScalar("bias_fp", bias);
		printScalar("m", m);
		printScalar("n", n);
		printMatrix("data_fp", point);
		printVector("y_fp", target);
		printVector("alpha_fp", alph);
		printScalar("C", C);
		printScalar("tol", tol);
		printScalar("eps", eps);
		printScalar("takeStepCount", takeStepCount);
		int svs = 0;
		for (int i = 0; i < m; i++) {
			if (alph[i] > tol)
				svs++;
		}
		printScalar("#sv", svs);
		printScalar("training err cnt", getTrainingErrorCountFP());

		for (int i = 0; i < 100000; i++)
			;
		System.out.println("======SMO INFO END======");
	}

	void printBoolean(String str, boolean b) {
		System.out.print(str);
		System.out.print(':');
		if (b)
			System.out.println("true");
		else
			System.out.println("false");
	}

	void printScalar(String str, int sca) {
		System.out.print(str);
		System.out.print(':');
		System.out.println(sca);
		for (int i = 0; i < 100; i++)
			;
	}

	void printScalar(String str, float sca) {
		System.out.print(str);
		System.out.print(':');
		System.out.println(sca);
		for (int i = 0; i < 100; i++)
			;
	}


	void printVector(String str, float[] ve) {
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

	void printVector(String str, int[] ve) {
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


	void printMatrix(String str, float[][] ma) {
		for (int i = 0; i < ma.length; i++) {
			System.out.print(str);
			System.out.print("[");
			System.out.print(i);
			System.out.print("]");
			System.out.print(":");
			printVector("", ma[i]);
		}
	}

	public int getSV() {
		int svs = 0;
		for (int i = 0; i < m; i++) {
			if (alph[i] > tol)
				svs++;
		}
		return svs;
	}

	void P(String s, int traceLevel) {
		if (traceLevel <= TRACELEVEL)
			System.out.println(s);
	}

	StringBuffer sb = new StringBuffer();

	void P(float f, int traceLevel) {
		if (traceLevel <= TRACELEVEL) {
			int i = (int) f;
			int j = (int) (f * 100 - (float)i * 100+1);
			System.out.print(i);
			System.out.print(".");
			if(j<10)
				System.out.print("0");
			System.out.println(j);
		}
	}

	void P(int i, int traceLevel) {
		if (traceLevel <= TRACELEVEL)
			System.out.println(i);
	}

	void P(boolean b, int traceLevel) {
		if (traceLevel <= TRACELEVEL)
			System.out.println(b);
	}

	public SMOBinaryClassifierFloat() {
		System.out.println("SMOBinaryClassifier object constructed.");
	}

	private class SVMHelp implements Execute {

		Object lock;

		int p; // the test point index

		float functionalOutput_fp;

		public void execute(int nr) {
			// System.out.println("Parallel in core 0:nr=" + nr);
			if (alph[nr] > 0) {

				float kernelOutput_fp = getKernelOutputFloat(nr, p);
				float tmp = ((alph[nr] * target[nr]) * kernelOutput_fp);
				synchronized (lock) {
					functionalOutput_fp += tmp;
					// functionalOutput_fp += bias_fp;
				}
			}
		}

		void setP(int p) {
			this.p = p;
			functionalOutput_fp = 0;
		}

		float getFuncOut() {
			return functionalOutput_fp;
		}
	}

	// First measure
	public void measure() {

		int time = 0;
		float ser0, ser1, par0, par1;

		// serial
		time = Native.rd(Const.IO_US_CNT);
		ser0 = getFunctionOutputFloat(0, false);
		ser1 = getFunctionOutputFloat(1, false);
		time = Native.rd(Const.IO_US_CNT) - time;

		P("Serial time=" + time, TR4);
		P("f(i 0):" + ser0, TR4);
		P("f(i 1):" + ser1, TR4);

		// parallel
		time = Native.rd(Const.IO_US_CNT);
		par0 = getFunctionOutputFloat(0, true);
		par1 = getFunctionOutputFloat(1, true);
		time = Native.rd(Const.IO_US_CNT) - time;

		P("Parrallel time=" + time, TR4);
		P("f(i 0):" + par0, TR4);
		P("f(i 1):" + par1, TR4);

	}

}