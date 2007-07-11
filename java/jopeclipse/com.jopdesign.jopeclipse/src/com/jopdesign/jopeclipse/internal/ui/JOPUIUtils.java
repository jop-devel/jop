package com.jopdesign.jopeclipse.internal.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.osgi.service.prefs.Preferences;

import com.jopdesign.jopeclipse.internal.ui.launchConfigurations.IJOPLaunchConfigurationConstants;

public class JOPUIUtils {
    public static final GridData FILL_HORIZONTAL = new GridData(
            GridData.FILL_HORIZONTAL);

    public static Group createHorizontalGrabGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);

        group.setLayoutData(FILL_HORIZONTAL);

        return group;
    }

    public static String getProjectName(ILaunchConfiguration configuration) {
        try {
            return configuration.getAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                    (String) null);
        } catch (CoreException e) {

        }

        return null;
    }

    public static String getProjectSetting(ILaunchConfiguration configuration,
            String key, String def) {
        String projectName = getProjectName(configuration);

        if (projectName == null) {
            return def;
        }

        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
                projectName);
        IScopeContext scopeContext = new ProjectScope(project);
        Preferences projectPrefs = scopeContext
                .getNode(IJOPUIConstants.PLUGIN_ID);

        return projectPrefs.get(key, def);
    }

    public static void setProjectSetting(ILaunchConfiguration configuration,
            String key, String value) {
        String projectName = getProjectName(configuration);

        if (projectName == null) {
            return;
        }

        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
                projectName);
        IScopeContext scopeContext = new ProjectScope(project);
        Preferences projectPrefs = scopeContext
                .getNode(IJOPUIConstants.PLUGIN_ID);

        projectPrefs.put(key, value);
    }
}
