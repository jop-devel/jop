package com.jopdesign.jopeclipse.internal.ui.launchConfigurations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

public class JOPLaunchConfigurationDelegate extends JavaLaunchDelegate {
    @Override
    public void launch(ILaunchConfiguration configuration, String mode,
            ILaunch launch, IProgressMonitor monitor) throws CoreException {
        System.err.println("JOPLaunchConfigurationDelegate");

        if (CommonTab.isLaunchInBackground(configuration)) {
            System.err.println("Launch in background");
        }

        // FIXME do some serious error handling here, alright

        IJavaProject javaProject = getJavaProject(configuration);
        IProject project = javaProject.getProject();

        /* Can I guarantee this is a Java project?
         *   JOP runs only Java, so..?
         */

        // Find the build file (relative path)
        IFile buildFile = findAntBuildFile(project);
        IPath buildFileLocation = buildFile.getLocation();

        System.out.printf("[Build file] OS-string: %s%n", buildFileLocation
                .toOSString());

        AntRunner antRunner = new AntRunner();
        antRunner.setBuildFileLocation(buildFileLocation.toOSString());
        antRunner.addBuildLogger("org.apache.tools.ant.DefaultLogger");

        // TODO Remove when not debugging
        antRunner
                .setMessageOutputLevel(org.apache.tools.ant.Project.MSG_VERBOSE);

        setAntProperties(configuration, antRunner);

        try {
            antRunner.run(monitor);
        } catch (CoreException e) {
            // Build exception occured
            e.printStackTrace();
        }

        // TODO here we should, if everything went smoothly, run JavaDown

        if (!AntRunner.isBuildRunning()) {
            // Launch the Java build
            super.launch(configuration, mode, launch, monitor);
        }
    }

    /**
     * Adds JOP build specific parameters to the Ant build file
     * 
     * @param configuration
     * @param antRunner
     */
    private void setAntProperties(ILaunchConfiguration configuration,
            AntRunner antRunner) {
        Map<String, String> antProperties = new HashMap<String, String>();

        try {
            // Inherited values
            antProperties.put("main-class", configuration.getAttribute(
                    getMainTypeName(configuration), ""));

            // JOP stuff
            antProperties.put("com-port", configuration.getAttribute(
                    IJOPLaunchConfigurationConstants.ATTR_COM_PORT, ""));
            antProperties.put("com-flag", getCommFlag(configuration));
            antProperties.put("qproj", configuration.getAttribute(
                    IJOPLaunchConfigurationConstants.ATTR_QUARTUS_PROJECT, ""));
            antProperties.put("jopbin", getOutputDirectory(configuration)
                    .append(getMainTypeName(configuration)).addFileExtension(
                            "jop").toOSString());
        } catch (CoreException e) {
            // But why?
            e.printStackTrace();
        }

        antRunner.addUserProperties(antProperties);
    }

    /**
     * Returns the appropriate flag for the selected Java download method
     * 
     * @param configuration
     * @return
     */
    public static String getCommFlag(ILaunchConfiguration configuration) {
        return useUsbDownload(configuration) ? "-usb" : "-e";
    }

    /**
     * 
     * @param configuration
     * @return
     * @throws CoreException
     */
    private IPath getOutputDirectory(ILaunchConfiguration configuration)
            throws CoreException {
        return getJavaProject(configuration).getOutputLocation();
    }

    /**
     * 
     * @param configuration
     * @return
     */
    private static boolean useUsbDownload(ILaunchConfiguration configuration) {
        try {
            return configuration.getAttribute(
                    IJOPLaunchConfigurationConstants.ATTR_USB_FLAG, false);
        } catch (CoreException e) {
            return false;
        }
    }

    /**
     * Walks the file hierarchy looking for an Ant build file. Returns the first build file found.
     * 
     * 
     * @param parent
     * @return
     */
    /* TODO
     * Bubble down from container (selected file in most cases) and select the
     * closest build.xml instead of bubbling up from the project root
     */
    public static IFile findAntBuildFile(IContainer parent) {
        String[] buildFileNames = new String[] { "build.xml" };

        if (buildFileNames == null || buildFileNames.length == 0) {
            return null;
        }

        IResource file = null;

        while (file == null || file.getType() != IResource.FILE) {
            for (String buildFileName : buildFileNames) {
                file = parent.findMember(buildFileName);

                if (file != null && file.getType() == IResource.FILE) {
                    break;
                }
            }

            parent = parent.getParent();

            if (parent == null) { // A file must have a parent
                return null;
            }
        }

        return (IFile) file;
    }
}
