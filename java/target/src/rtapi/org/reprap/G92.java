package org.reprap;

//Set Position
public class G92 extends Command
{
	private static G92 instance = new G92();//Unbuffered command so only single instance
	
	private Parameter parameters = new Parameter();
	
	public static boolean enqueue(Parameter parameters)
	{
		instance.parameters.X = parameters.X;
		instance.parameters.Y = parameters.Y;
		instance.parameters.Z = parameters.Z;
		instance.parameters.E = parameters.E;
		Command.enqueue(instance);
		return true;
	}
	
	@Override
	public boolean execute() 
	{
		RepRapController instance = RepRapController.getInstance();
		instance.setPosition(parameters);
		return true;
	}
}
