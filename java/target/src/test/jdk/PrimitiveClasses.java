package jdk;
import jvm.TestCase;
//import JOPlibrary.lang.Boolean;
//import JOPlibrary.lang.Byte;

public class PrimitiveClasses extends TestCase {

	public String getName() {
		return "PrimitiveClasses";
	}
	public boolean test() {
	boolean Ok=true;	
	
	//BOOLEAN
	Boolean myBoolean1=new Boolean(true);
	////possible issue when parameter is the string "TRuE"
	Boolean myBoolean2=new Boolean("true");
	//possible issue when parameter is null
	Boolean myBoolean3=new Boolean(false);
	Boolean myBoolean4=new Boolean("fAlSe");
	//test constructors
	Ok=Ok&& myBoolean1.booleanValue();
	Ok=Ok&& myBoolean2.booleanValue();
	Ok=Ok&& ! myBoolean3.booleanValue();
	
	//equals()
	Ok=Ok &&  myBoolean1.equals(myBoolean2);
	Ok=Ok &&  myBoolean3.equals(myBoolean4);
	Ok=Ok &&  !myBoolean1.equals(myBoolean3);
	
	//hashCode
	Ok=Ok && myBoolean1.hashCode()==myBoolean1.hashCode();
	Ok=Ok && myBoolean1.hashCode()==myBoolean2.hashCode();
	Ok=Ok && myBoolean3.hashCode()==myBoolean4.hashCode();
	Ok=Ok && myBoolean1.hashCode()!=myBoolean3.hashCode();

	//toString
	Ok=Ok&&(myBoolean1.toString()).equalsIgnoreCase("true");
	Ok=Ok&&(myBoolean3.toString()).equalsIgnoreCase("false");
	Ok=Ok&&!(myBoolean1.toString()).equalsIgnoreCase("false");
	Ok=Ok&&!(myBoolean3.toString()).equalsIgnoreCase("true");
	
	//valueOf(String)
	//Ok=Ok && (Boolean.valueOf("true")).equalsIgnoreCase("true");
	
	//BYTE
	Byte myByte1=new Byte((byte)122);
	//The constructor Byte(string) is not defined
	//Byte myByte2=new Byte("22");
	Ok=Ok && ((byte)122)==myByte1.byteValue();
	//Ok=Ok && ((byte)22)==myByte1.byteValue();
	//check if NumberFormatException is thrown 
	//new Byte("myWrongString");
	
	//parseByte(String)
	Ok=Ok && 123==Byte.parseByte("123");
	Ok=Ok && 127==Byte.parseByte("127");
	Ok=Ok && -128==Byte.parseByte("-128");
	Ok=Ok && !(123==Byte.parseByte("121"));
	
	
	return Ok;
	}
	
	
}
