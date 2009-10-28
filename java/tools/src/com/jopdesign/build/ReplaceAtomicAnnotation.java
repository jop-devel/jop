package com.jopdesign.build;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import boxpeeking.instrument.bcel.AnnotationReader;
import boxpeeking.instrument.bcel.AnnotationsAttribute;

public class ReplaceAtomicAnnotation extends JOPizerVisitor {

	public ReplaceAtomicAnnotation(AppInfo jz) {
		super(jz);
	}
	
	public void visitJavaClass(JavaClass clazz) {

		super.visitJavaClass(clazz);
		
		System.out.println("Class: " + clazz.getClassName());
		
		Method[] methods = clazz.getMethods();
		
		for(Method m: methods) {
			for (Annotation a: getAnnotations(m.getAttributes())) {
				if (a instanceof rttm.misc.Atomic) {
				}
			}
		}
	}
	
	/**
	 * Borrowed from boxpeeking.instrument.ant.InstrumentTask.
	 */
	private static Collection<Annotation> getAnnotations (Attribute[] attrs)
	{
		Collection<Annotation> anns = new ArrayList<Annotation>();
		
		for (Attribute a : attrs) {
			if (a instanceof AnnotationsAttribute) {
				AnnotationsAttribute aa = (AnnotationsAttribute)a;

				for (Map m : aa.getAnnotations()) {
					anns.add(AnnotationReader.getAnnotation(m));
				}
			}
		}
		return anns;
	}

	
	protected void transform(Method m) {
		
	}

}
