package com.jopdesign.jopeclipse.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;

import com.jopdesign.jopeclipse.JOPUIPlugin;
import com.jopdesign.jopeclipse.internal.ui.launchConfigurations.IJOPLaunchConfigurationConstants;

public class JOPUtils {
    /**
     * Throws a core exception with an error status object built from
     * the given message, lower level exception, and error code.
     * 
     * @param message the status message
     * @param exception lower level exception associated with the
     *  error, or <code>null</code> if none
     * @param code error code
     */
    public static void abort(String message, Throwable exception, int code)
            throws CoreException {
        throw new CoreException(new Status(IStatus.ERROR,
                JOPUIPlugin.PLUGIN_ID, code, message, exception));
    }

    public static ILaunchConfigurationType getLaunchConfigurationType() {
        return DebugPlugin
                .getDefault()
                .getLaunchManager()
                .getLaunchConfigurationType(
                        IJOPLaunchConfigurationConstants.LAUNCH_CONFIGURATION_TYPE_ID);
    }
}
