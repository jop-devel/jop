package com.jopdesign.jopeclipse.internal.ui.launchConfigurations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchShortcut;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

import com.jopdesign.jopeclipse.JOPUIPlugin;
import com.jopdesign.jopeclipse.internal.JOPUtils;

/**
 * Provides shortcuts to launch a/the JOP application from fast access menus.
 * 
 * If a launch configuration for the given entity can't be found, a new 
 * configuration will not be generated. Instead, the launch configuration dialog
 * is opened.  
 * 
 * @author johan
 *
 */
public class JOPLaunchShortcut extends JavaLaunchShortcut {

    @Override
    public void launch(ISelection selection, String mode) {
        System.err.printf("%s%n", getConfigurationType());
    }

    @Override
    public void launch(IEditorPart editor, String mode) {

    }

    @Override
    protected ILaunchConfiguration createConfiguration(IType type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IType[] findTypes(Object[] elements, IRunnableContext context)
            throws InterruptedException, CoreException {
        return null;
    }

    @Override
    protected ILaunchConfigurationType getConfigurationType() {
        return JOPUtils.getLaunchConfigurationType();
    }

    @Override
    protected String getEditorEmptyMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getSelectionEmptyMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getTypeSelectionTitle() {
        // TODO Auto-generated method stub
        return null;
    }
}