package hwScopeEx.sensors;

/* This class represent the calibration sensor object. 
 * 
 */

public class CSensor {
	
	int id;
	Environment E;
	Calibration C;
	
	public CSensor(int ID, Environment env){
		
		id = ID;
		E = env;
		C = new Calibration();
	};
	
	/* Data from the sensor is collected when the sensor is read. We can simulate the sensor in
	 * two ways:
	 * 		
	 * 		1. As a SW code in a Java class   
	 * 		2. Using a HW object and coding the functionality of the sensor in VHDL 
	 */
	public Calibration calibrate() {
		
		// This should be a putfield_ref since Integer (the returning type of
		// E.getCSensorData() ) is an object
		C.cal_param_1 = E.getCSensorData(); 
		return C;  

	}
	
}
