package boxpeeking.instrument.bcel;

import java.io.*;
import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.annotation.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.*;

public class AnnotationsAttribute extends Attribute
{
	private static final String TYPE = "_type";

	private Map[] annotations;

	public AnnotationsAttribute (byte tag, int name_index, int length, ConstantPool constant_pool, Map[] annotations)
	{
		super(tag, name_index, length, constant_pool);
		this.annotations = annotations;
	}

	public Map[] getAnnotations ()
	{
		return annotations;
	}

	public void accept (Visitor v)
	{
		throw new UnsupportedOperationException();
	}

	public void dump (DataOutputStream out)
		throws IOException
	{
		ConstantPoolGen cpg = new ConstantPoolGen(getConstantPool());

		out.writeShort(getNameIndex());
		out.writeInt(getLength());
		out.writeShort(annotations.length);
		for (Map m : annotations) {
			out.writeShort(cpg.lookupClass((String)m.get(TYPE)));

			out.writeShort(m.size() - 1);
			for (Object name : m.keySet()) {
				if (!name.equals(TYPE)) {
					out.writeShort(cpg.lookupUtf8((String)name));

					Object value = m.get(name);

					if (value instanceof String) {
						out.writeByte('s');
						out.writeShort(cpg.lookupUtf8((String)value));
					} else {
						throw new UnsupportedOperationException("writing " + value + " (" + value.getClass() + ")");
					}
				}
			}
		}
	}

	public Attribute copy (ConstantPool cp)
	{
		return this;
	}

	public String toString ()
	{
		return Arrays.asList(annotations).toString();
	}
}
