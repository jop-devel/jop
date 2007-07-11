package com.jopdesign.jopeclipse.internal.ui.launchConfigurations;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;


public class JOPTabGroup extends AbstractLaunchConfigurationTabGroup {    
    /**
     * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.ILaunchConfigurationDialog, java.lang.String)
     */
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
                // new AntMainTab(),
                new JavaMainTab(),
                new JOPDownloadTab(),
                // new JOPToolsTab(),
                new JOPBoardConfigurationTab(),
                new JavaArgumentsTab(),
                new JavaJRETab(),
                new JavaClasspathTab(),
                // new JOPWCETTab(),
                // new ProGuardOptimizationTab(),
                new CommonTab()
        };
        
        setTabs(tabs);
    }
}
