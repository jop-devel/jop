package jvm;

public class Logic2 extends TestCase {
	public String getName() {
		return "Logic2";
	}

	public boolean test() {
		boolean Ok=true;
		int number1,number2;
		number1=-32767;
		number2=32767;
	
		//check ineg
		Ok=Ok && number1==-number2;
		//check the negation of the maximum negative number
		number1=-2147483648;
		number1=-number1;
		Ok=Ok && number1==-2147483648;
		
		//check ior
		number1=0x56565656;
		number2=0x65656565;
		Ok=Ok && 0x77777777==(number1|number2);
		
		//check irem
		number1=38485;
		number2=324;
		Ok=Ok && number1==(number1/number2)*number2 + number1%number2;
		
		number1=-2147483648;
		number2=-1;
		Ok=Ok && number1==(number1/number2)*number2 + number1%number2;
		
		//used to check for the throw of ArithmeticException
		/*number1=38485;
		number2=0;
		number1= number1%number2;*/
		
		//check isub, cause an overflow
		number1=-2047483648;
		number2=2047483648;
		number1=number1-number2;
		
		//check ixor
		number1=0x55555555;
		number2=0x66666666;
		Ok=Ok && 0x33333333==(number1^number2);
		
		//check iand
		number1=0x55555555;
		number2=0x66666666;
		Ok=Ok && 0x44444444==(number1&number2);
		
		return Ok;
	}



}
