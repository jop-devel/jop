package com.jopdesign.jopeclipse.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * 
 * 
 * @author johan
 */
public class JOPNaturePropertyTester extends PropertyTester {
    /* (non-Javadoc)
     * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
     */
    // @Override
    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        return "hasJOPNature".equals(property) && hasJOPNature(receiver);
    }

    /**
     * @return <code>true</code> if the target resource is member of a JOP
     *         application project, <code>false</code> otherwise.
     */
    private boolean hasJOPNature(Object receiver) {
        if (receiver instanceof IAdaptable) {
            IResource res = (IResource) ((IAdaptable) receiver)
                    .getAdapter(IResource.class);

            if (res != null) {
                try {
                    return res.getProject().hasNature(JOPNature.NATURE_ID);
                } catch (CoreException e) {
                    return false;
                }
            }
        }

        return false;
    }
}
