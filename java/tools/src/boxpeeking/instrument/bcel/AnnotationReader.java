package boxpeeking.instrument.bcel;

import java.io.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;

/**
 * Reduced from http://onjava.com/pub/a/onjava/2004/06/30/insidebox1.html
 * 
 * @author Peter Hilber (peter@hilber.name)
 *
 */
public class AnnotationReader implements AttributeReader
{
	protected static final String ATOMIC_TAG_NAME = "Lrttm/Atomic;";
	
	public Attribute createAttribute (int name_index, int length, DataInputStream in, ConstantPool cp)
	{
		try {
			short numAnnotations = in.readShort();

			boolean hasAtomicAnnotation = false;
			for (int i = 0; i < numAnnotations; i++) {
				if (isAtomicAnnotation(in, cp)) {
					hasAtomicAnnotation = true;					
				} else {
					throw new UnsupportedOperationException();
				}
			}

			return new AnnotationsAttribute(Constants.ATTR_UNKNOWN, 
					name_index, length, cp, hasAtomicAnnotation);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public boolean isAtomicAnnotation (DataInputStream in, ConstantPool cp)
		throws IOException
	{
		short typeIndex = in.readShort();
		String type = cp.constantToString(cp.getConstant(typeIndex));

		short numMVPairs = in.readShort();
		for (int i = 0; i < numMVPairs; i++) {
			in.readShort();
		}

		return type.equals(ATOMIC_TAG_NAME);
	}
}

