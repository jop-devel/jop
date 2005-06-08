package com.jopdesign.build;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.Visitor;
/*
 * Created on Dec 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author root
 *
 * Built Dec 14, 2004, 5:28:01 PM
 */
public class NativeInstruction extends Instruction {

	/**
	 * @param arg0
	 * @param arg1
	 */
	public NativeInstruction(short arg0, short arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.generic.Instruction#accept(org.apache.bcel.generic.Visitor)
	 */
	public void accept(Visitor arg0) {
		// TODO Auto-generated method stub

	}

}
