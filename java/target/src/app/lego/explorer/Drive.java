package lego.explorer;

import lego.lib.Motor;

public class Drive {

	private static final int LEFT = 0, RIGHT = 1;

	private Motor motor[] = new Motor[2];
	private int motorState[] = { Motor.STATE_OFF, Motor.STATE_OFF };
	private int motorDuty[] = { 0, 0 };
	public int[][] motorEMF = { new int[2], new int[2] };

	public Drive(int motorLeft, int motorRight) {
		this.motor[LEFT] = new Motor(motorLeft);
		this.motor[RIGHT] = new Motor(motorRight);
	}
	public void write() {
		for(int i = 0; i < 2; i++) {
			motor[i].setMotorPercentage(motorState[i], true, motorDuty[i]);
		}
	}
	public void read() {
		Motor.synchronizedReadBackEMF();
		for(int i = 0; i < 2; i++) {
			motor[i].updateSynchronizedNormalizedBackEMF(motorEMF[i]);
		}
	}
	public void goForward(int duty) {
		goTogether(duty, Motor.STATE_FORWARD);
	}
	public void goBackward(int duty) {
		goTogether(duty, Motor.STATE_BACKWARD);
	}
	public void stopMotor() {
		goTogether(75, Motor.STATE_BRAKE);
	}
	private void goTogether(int duty, int state) {
		for(int i = 0; i < 2; i++) {
			motorState[i] = state;
			motorDuty[i] = duty;
		}
	}
	public void set(int stateL, int dutyL, int stateR, int dutyR) {
		motorState[LEFT] = stateL;
		motorDuty[LEFT] = dutyL;
		motorState[RIGHT] = stateR;
		motorDuty[RIGHT] = dutyR;
	}
	public void dump() {
		for(int i = 0; i < 2; i++) {
			System.out.print("Motor ");
			if(i==LEFT) System.out.print("LEFT");
			else        System.out.print("RIGHT");
		    System.out.print("(state,duty,emf[0],emf[1]): (");
			System.out.print(motorState[i]);System.out.print(", ");
			System.out.print(motorDuty[i]);System.out.print(", ");
			System.out.print(motorEMF[i][0]);System.out.print(", ");
			System.out.print(motorEMF[i][1]);System.out.println(")");
		}		
	}

}
