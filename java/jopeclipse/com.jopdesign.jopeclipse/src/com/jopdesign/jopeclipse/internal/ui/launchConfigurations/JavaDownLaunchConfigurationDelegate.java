package com.jopdesign.jopeclipse.internal.ui.launchConfigurations;

import gnu.io.SerialPort;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.JavaRuntime;

import com.jopdesign.jopeclipse.JOPUIPlugin;
import com.jopdesign.jopeclipse.internal.JOPUtils;
import com.jopdesign.jopeclipse.internal.core.JavaDown;

public class JavaDownLaunchConfigurationDelegate extends JavaLaunchDelegate {
    /** Serial port timeout (in ms) */
    protected static final int SERIAL_PORT_TIMEOUT = 2000;

    protected static int SERIAL_PORT_BAUDRATE = 115200;

    protected static int SERIAL_PORT_DATABITS = SerialPort.DATABITS_8;

    protected static int SERIAL_PORT_STOPBITS = SerialPort.STOPBITS_1;

    protected static int SERIAL_PORT_PARITY = SerialPort.PARITY_NONE;

    @Override
    public void launch(ILaunchConfiguration configuration, String mode,
            ILaunch launch, IProgressMonitor monitor) throws CoreException {
        if (CommonTab.isLaunchInBackground(configuration)) {
            System.err.println("Launch in background");
        }

        try {
            // JOPize
            int jopizerExitValue = jopize(configuration, mode, launch, monitor);
            IPath jopizedFile = getJopizedFile(configuration);

            if (configuration.getAttribute(
                    IJOPLaunchConfigurationConstants.ATTR_SIMULATE, true)) { // configuration.getAttribute("SIMULATE", false)) {
                simulate(configuration, mode, launch, monitor);
            } else {
                JavaDown downloader = new JavaDown();
                boolean usb = useUsbDownload(configuration);

                String portName = configuration.getAttribute(
                        IJOPLaunchConfigurationConstants.ATTR_COM_PORT, "");

                downloader.setCommPortId(portName);
                downloader.useUSB(usb);
                downloader.setJopFile(jopizedFile);

                downloader.run(monitor);
            }
        } catch (Exception e) {
            JOPUtils.abort(e.getLocalizedMessage(), e, 0);
        }
    }

    /**
     * Returns the project's (absolute) output path, e.g., where the .class-files are located.
     * @param configuration
     * @return
     * @throws CoreException
     */
    private IPath getAbsoluteProjectOutputPath(
            ILaunchConfiguration configuration) throws CoreException {
        IJavaProject javaProject = JavaRuntime.getJavaProject(configuration);
        IPath workspacePath = javaProject.getProject().getWorkspace().getRoot()
                .getLocation();
        IPath relativeOutputPath = javaProject.getOutputLocation();
        IPath projectOutputPath = workspacePath.append(relativeOutputPath);

        return projectOutputPath;
    }

    /**
     * Returns the location of the JOPized class-file
     * @param configuration
     * @return
     * @throws CoreException
     */
    private IPath getJopizedFile(ILaunchConfiguration configuration)
            throws CoreException {
        IPath outputPath = getAbsoluteProjectOutputPath(configuration);
        IPath jopOutFile = outputPath.append(getMainTypeName(configuration))
                .addFileExtension("jop");

        return jopOutFile;
    }

    /**
     * 
     * @param configuration
     * @param mode
     * @param launch
     * @param monitor
     * @return
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     */
    private int jopize(ILaunchConfiguration configuration, String mode,
            ILaunch launch, IProgressMonitor monitor) throws CoreException,
            IOException, InterruptedException {
        IPath workingDir = getAbsoluteProjectOutputPath(configuration);
        IPath jopizedFile = getJopizedFile(configuration);

        IPath jopHome = Path.fromOSString(JOPUIPlugin.getDefault()
                .getPreferenceStore().getString(
                        IJOPLaunchConfigurationConstants.ATTR_JOP_HOME));

        String[] args = new String[] {
                "java",
                "-classpath",
                String.format("%s;%s;%s", jopHome
                        .append("java/lib/bcel-5.1.jar"), jopHome
                        .append("java/lib/jakarta-regexp-1.3.jar"), jopHome
                        .append("java/tools/dist/lib/jop-tools.jar")),
                "com.jopdesign.build.JOPizer",
                "-cp",
                jopHome.append("java/target/dist/lib/classes.zip") + ";"
                        + workingDir.toOSString(), "-o",
                jopizedFile.toOSString(), getMainTypeName(configuration) };

        Process process = DebugPlugin.exec(args, workingDir.toFile());
        IProcess p = DebugPlugin.newProcess(launch, process, "JOPizer");

        p.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, "true");

        return process.waitFor();
    }

    /**
     * "Hi-jacks" the Java launch and replaces main-class and parameters to run 
     * a simulation of the JOPized file
     *  
     * @param configuration
     * @param mode
     * @param launch
     * @param monitor
     * @throws CoreException
     */
    public void simulate(ILaunchConfiguration configuration, String mode,
            ILaunch launch, IProgressMonitor monitor) throws CoreException {
        IPath jopizedFile = getJopizedFile(configuration);
        Map attributes = configuration.getAttributes();

        attributes.put(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
                "com.jopdesign.tools.JopSim");
        attributes.put(
                IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
                jopizedFile.toOSString());

        ILaunchConfigurationWorkingCopy workingCopy = configuration
                .getWorkingCopy();

        workingCopy.setAttributes(attributes);

        super.launch(workingCopy, mode, launch, monitor);
    }

    /**
     * Returns the appropriate flag for the selected Java download method
     * 
     * @param configuration
     * @return
     */
    public static String getCommFlag(ILaunchConfiguration configuration) {
        return useUsbDownload(configuration) ? "-e -usb" : "-e";
    }

    /**
     * 
     * @param configuration
     * @return
     */
    public static String getByteBlaster(ILaunchConfiguration configuration) {
        // TODO are these the only options?
        return useUsbDownload(configuration) ? "USB-Blaster" : "ByteBlasterMV";
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
        
        }
        
        return false;
    }
}
