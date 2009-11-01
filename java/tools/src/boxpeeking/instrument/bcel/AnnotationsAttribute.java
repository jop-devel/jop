package boxpeeking.instrument.bcel;

import java.io.*;
import org.apache.bcel.classfile.*;

/**
 * Reduced from http://onjava.com/pub/a/onjava/2004/06/30/insidebox1.html
 * 
 * @author Peter Hilber (peter@hilber.name)
 *
 */
public class AnnotationsAttribute extends Attribute
{
	private static final long serialVersionUID = 1L;
	
	private boolean hasAtomicAnnotation;

	public AnnotationsAttribute (byte tag, int name_index, int length, ConstantPool constant_pool, boolean hasAtomicAnnotation)
	{
		super(tag, name_index, length, constant_pool);
		this.hasAtomicAnnotation = hasAtomicAnnotation;
	}
	
	public boolean hasAtomicAnnotation() {
		return hasAtomicAnnotation;
	}

	public void accept (Visitor v)
	{
		// HACK: ignore visit
//		throw new UnsupportedOperationException();
	}

	public void dump (DataOutputStream out)
		throws IOException
	{
		throw new UnsupportedOperationException();
	}

	public Attribute copy (ConstantPool cp)
	{
		throw new UnsupportedOperationException();
	}

	public String toString ()
	{
		throw new UnsupportedOperationException();
	}
}
