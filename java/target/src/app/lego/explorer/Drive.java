package lego.explorer;

import lego.lib.Motor;

public class Drive {

	public int[] motorLeftEMF = new int[2], motorRightEMF = new int[2];

	private Motor motorLeft, motorRight;
	private int motorStateLeft = Motor.STATE_OFF, motorDutyLeft = 0;
	private int motorStateRight = Motor.STATE_OFF, motorDutyRight = 0;

	public Drive(int motorLeft, int motorRight) {
		this.motorLeft = new Motor(motorLeft);
		this.motorRight = new Motor(motorRight);
	}
	public void write() {
		motorLeft.setMotorPercentage(motorStateLeft, true, motorDutyLeft);
		motorRight.setMotorPercentage(motorStateRight, true, motorDutyRight);
	}
	public void read() {
		Motor.synchronizedReadBackEMF();
		motorLeft.updateSynchronizedNormalizedBackEMF(motorLeftEMF);
		motorRight.updateSynchronizedNormalizedBackEMF(motorRightEMF);
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
		motorStateLeft = motorStateRight = state;
		motorDutyLeft = motorDutyRight = duty;
	}
	public void set(int stateL, int dutyL, int stateR, int dutyR) {
		motorStateLeft = stateL; motorStateRight = stateR;
		motorDutyLeft = dutyL; motorDutyRight = dutyR;		
	}

}
