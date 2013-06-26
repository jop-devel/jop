package libcsp.csp.interfaces;

import com.jopdesign.io.I2CFactory;

public class InterfaceI2C_B extends InterfaceI2C {

	private static InterfaceI2C_B instance;

	public static InterfaceI2C_B getInterface() {
		if (instance == null) {
			instance = new InterfaceI2C_B();
		}

		return instance;
	}

	@Override
	public void initialize(int MACAddress) {
		frameByteIndex = 0;
		i2cPort = I2CFactory.getFactory().getI2CportB();

		/* I2C port should be initialized as slave so it can receive data */
		i2cPort.initialize(MACAddress, false);
	}

}