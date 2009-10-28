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

public class AnnotationReader implements AttributeReader
{
	public Attribute createAttribute (int name_index, int length, DataInputStream in, ConstantPool cp)
	{
		try {
			short numAnnotations = in.readShort();

			Map[] a = new Map[numAnnotations];
			for (int i = 0; i < numAnnotations; i++) {
				a[i] = readAnnotation(in, cp);
			}

			return new AnnotationsAttribute(Constants.ATTR_UNKNOWN, name_index, length, cp, a);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public Map readAnnotation (DataInputStream in, ConstantPool cp)
		throws IOException
	{
		short typeIndex = in.readShort();
		String type = cp.constantToString(cp.getConstant(typeIndex));

		Map map = new HashMap();

		short numMVPairs = in.readShort();
		for (int i = 0; i < numMVPairs; i++) {
			short nameIndex = in.readShort();

			String name = cp.constantToString(cp.getConstant(nameIndex));
			map.put(name, readMemberValue(in, cp));
		}

		map.put("_type", type);
		return map;
	}

	public static Annotation getAnnotation (Map map)
	{
		String type = (String)map.get("_type");

		Class typeClass;
		try {
			typeClass = Class.forName(type);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("cannot find class " + type, ex);
		}

  		ClassLoader cl = AnnotationReader.class.getClassLoader();
  		return (Annotation)Proxy.newProxyInstance(cl, new Class[] { typeClass }, new MyInvocationHandler(type, map));
	}

	public Object readMemberValue (DataInputStream in, ConstantPool cp)
		throws IOException
	{
		byte tag = in.readByte();
		switch(tag) {
			case 'B': case 'C': case 'D': case 'J':
			case 'F': case 'I': case 'S':
				return ((ConstantObject)cp.getConstant(in.readShort())).getConstantValue(cp);

			case 's':
				return cp.constantToString(cp.getConstant(in.readShort()));

			default:
				throw new UnsupportedOperationException("tag = " + tag);
		}
	}

	public static class MyInvocationHandler implements InvocationHandler
	{
		private String type;
		private Map map;

		public MyInvocationHandler (String type, Map map)
		{
			this.type = type;
			this.map = map;
		}

		public Object invoke (Object proxy, java.lang.reflect.Method method, Object[] args)
		{
			String methodName = method.getName();

			if (methodName.equals("toString")) {
				return "@" + type + map;
			} else {
				return map.get(method.getName());
			}
		}
	}
}

