package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;
import static javax.safetycritical.annotate.Level.INFRASTRUCTURE;

;

//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;

/**
 * How much is needed?
 */
@SCJAllowed
public interface AllocationContext {

	 /**
	 * Execute some logic with this memory area as the default allocation
	 * context. The effect on the scope stack is specified in the implementing
	 * classes.
	 *
	 * @param logic
	 * is the runnable to execute in this memory area.
	 */
	 @SCJAllowed
	 public void executeInArea(Runnable logic);

	/**
	 * Get the amount of allocated memory in this memory area.
	 * 
	 * @return the amount of memory in bytes consumed so far in this memory
	 *         area.
	 */
	@SCJAllowed
	public long memoryConsumed();

	/**
	 * Get the amount of memory available for allocation in this memory area.
	 * 
	 * @return the amount of memory in bytes remaining in this memory area.
	 */
	@SCJAllowed
	public long memoryRemaining();

	/**
	 * Create an array object of the given type and length number in this memory
	 * area. This method may be concurrently used by multiple threads.
	 * 
	 * @param type
	 *            is the class of object this memory area should hold. An array
	 *            of a primitive type can be created using a type such as
	 *            Integer.TYPE, which would create an array of the int type.
	 * 
	 * @param number
	 *            is the number of elements the array should have.
	 * 
	 * @return the new array of type <code>type</code> and size
	 *         <code>number</code>.
	 * 
	 * @throws IllegalArgumentException
	 *             when <code>number</code> is less than zero.
	 */
	@SCJAllowed
	public Object newArray(Class type, int number)
			throws IllegalArgumentException;

	/**
	 * Create a new instance of a class in this memory area using its default
	 * constructor.
	 * 
	 * @param type
	 *            is the class of the object to be created
	 * 
	 * @return a new instance of the given class.
	 * 
	 * @throws ExceptionInInitializerError
	 *             when an unexpected exception has occurred in a static
	 *             initializer.
	 * 
	 * @throws IllegalAccessException
	 *             when the class or initializer is inaccessible under Java
	 *             access control.
	 * 
	 * @throws InstantiationException
	 *             when the specified class object could not be instantiated.
	 *             Possible causes are the class is an interface, abstract
	 *             class, or array.
	 * 
	 * @throws InvocationTargetException
	 *             when the underlying constructor throws an exception.
	 */
	@SCJAllowed
	public Object newInstance(Class type)
	// throws ExceptionInInitializerError,
	// // IllegalAccessException,
	// InstantiationException,
	// InvocationTargetException;
	;

	/**
	 * Create a new instance of a class in this memory area using the chosen
	 * constructor.
	 * 
	 * @param constructor
	 *            to use.
	 * 
	 * @param arguments
	 *            the arguments required by the chosen constructor.
	 * 
	 * @return the new object.
	 * 
	 * @throws ExceptionInInitializerError
	 *             when an unexpected exception has occurred in a static
	 *             initializer.
	 * 
	 * @throws IllegalAccessException
	 *             when the class or initializer is inaccessible under Java
	 *             access control.
	 * 
	 * @throws IllegalArgumentException
	 *             when constructor is null, or the argument array does not
	 *             contain the number of arguments required by constructor. A
	 *             null value for arguments is treated like an array of length
	 *             0.
	 * 
	 * @throws InstantiationException
	 *             when the specified class object could not be instantiated.
	 *             Possible causes are the class is an interface, abstract
	 *             class, or array.
	 * 
	 * @throws InvocationTargetException
	 *             when the underlying constructor throws an exception.
	 * 
	 *             public Object newInstance(Constructor constructor, Object[]
	 *             arguments) throws ExceptionInInitializerError,
	 *             IllegalAccessException, IllegalArgumentException,
	 *             InstantiationException, InvocationTargetException;
	 */

	/**
	 * Get the size of this memory area.
	 * 
	 * @return the current size of this memory area.
	 */
	@SCJAllowed
	public long size();

	/*
	 * Perform an action on all children scopes of this memory area, so long as
	 * the {@link ChildScopeVisitor#visit(ScopedAllocationContext)} method
	 * returns null. When that method returns an object, the visit is terminated
	 * and that object is returned by this method, <p> The set of children may
	 * be concurrently modified by other tasks, but the view seen by the visitor
	 * might not be updated to reflect those changes. The guarantees when the
	 * set is disturbed by other tasks are <ul> <li> the visitor shall visit no
	 * member more than once,</li> <li> it shall visit only scopes that were a
	 * member of the set at some time during the enumeration of the set, and
	 * <li> it shall visit all the scopes that are not deleted during the
	 * execution of the visitor. </ul>
	 * 
	 * @param visitor determines the action to be performed on each of the
	 * children scopes.
	 * 
	 * @return null when all elements where visited and some object when the
	 * visit is forced to terminate at the end of visiting some element.
	 * 
	 * @throws IllegalArgumentException when visitor is null.
	 * 
	 * @throws RuntimeException when the visitor method throws such an exception
	 * or any runtime error, it is thrown here; thereby terminating the visit.
	 * 
	 * public Object visitScopedChildren(ChildScopeVisitor visitor) throws
	 * IllegalArgumentException, RuntimeException;
	 */
}
