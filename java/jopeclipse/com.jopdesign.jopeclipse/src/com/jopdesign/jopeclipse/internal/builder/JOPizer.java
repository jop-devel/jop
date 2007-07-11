package com.jopdesign.jopeclipse.internal.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchShortcut;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.preference.IPreferenceStore;

import com.jopdesign.jopeclipse.JOPUIPlugin;
import com.jopdesign.jopeclipse.internal.ui.launchConfigurations.IJOPLaunchConfigurationConstants;

public class JOPizer extends IncrementalProjectBuilder {
    public static final String BUILDER_ID = JOPUIPlugin.PLUGIN_ID + ".JOPizer";

    private IJavaProject currentProject;

    private String quartusProjectPath;

    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
            throws CoreException {
        IProject project = getProject();

        if (project == null || !project.isAccessible()) {
            return new IProject[0];
        }

        currentProject = JavaCore.create(project);

        // Determine if the Quartus project has changed
        String pluginQuartusProject = JOPUIPlugin.getDefault()
                .getPreferenceStore().getString(
                        IJOPLaunchConfigurationConstants.ATTR_QUARTUS_PROJECT);

        if (quartusProjectPath == null
                || !quartusProjectPath.equalsIgnoreCase(pluginQuartusProject)) {
            kind = FULL_BUILD;
            quartusProjectPath = pluginQuartusProject;
        }

        String[] buildTypes = new String[] { "0", "1", "2", "3", "4", "5",
                "FULL_BUILD", "7", "8", "AUTO_BUILD", "INCREMENTAL_BUILD",
                "11", "12", "13", "14", "CLEAN_BUILD" };

        System.err.printf("Build kind: %s%n" + "Args: %s%n", buildTypes[kind],
                args);

        // 
        if (kind == FULL_BUILD) {
            // Build tools-chain
            buildToolChain(monitor);
        }

        Set<IResource> filesToJOPize = new HashSet<IResource>();
        IResourceDelta delta = getDelta(getProject());

        System.err.printf("Delta: '%s'%n", delta);

        if (delta == null) {
            IRegion projectRegion = JavaCore.newRegion();

            projectRegion.add(currentProject);

            filesToJOPize.addAll(Arrays.asList(JavaCore.getGeneratedResources(
                    projectRegion, false)));
        } else {
            System.err.printf("Affected children: '%s'%n", Arrays
                    .toString(delta.getAffectedChildren()));

            IPath outputLocation = currentProject.getOutputLocation();
            IPath binLocation = outputLocation.removeFirstSegments(1);
            IResourceDelta outputDelta = delta.findMember(binLocation);

            // Gather .class files to JOPize
            ClassFileVisitor classFileVisitor = new ClassFileVisitor();
            outputDelta.accept(classFileVisitor);

            Map<Integer, Set<IResource>> updatedClassFiles = classFileVisitor
                    .getClassFiles();

            System.err.printf("All: %s%n", updatedClassFiles);
            System.err.printf("Changed: %s%n", updatedClassFiles
                    .get(IResourceDelta.CHANGED));
            System.err.printf("Added  : %s%n", updatedClassFiles
                    .get(IResourceDelta.ADDED));
            System.err.printf("Removed: %s%n", updatedClassFiles
                    .get(IResourceDelta.REMOVED));

            for (int kindKey : updatedClassFiles.keySet()) {
                if (kindKey != IResourceDelta.REMOVED) {
                    filesToJOPize.addAll(updatedClassFiles.get(kindKey));
                }
            }
        }

        if (!filesToJOPize.isEmpty()) {
            Iterator<IResource> it = filesToJOPize.iterator();
            StringBuilder jopizeArgs = new StringBuilder();

            jopizeArgs.append(it.next().getLocation().toOSString());

            while (it.hasNext()) {
                jopizeArgs.append(' ');
                jopizeArgs.append(it.next().getLocation().toOSString());
            }

            IPath outputDir = currentProject.getOutputLocation();

            ILaunchConfiguration jopizeLaunchConfig = createJOPizeLaunchConfiguration("-cp "
                    + getClassesClasspathEntry().getPath().toOSString()
                    + " -o "
                    + "c:\\temp\\test.jop"
                    + " "
                    + jopizeArgs.toString());

            System.err.printf(">> %s%n", jopizeLaunchConfig);
            System.err.printf(">> %s%n", jopizeLaunchConfig.getAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
                    "---"));

            DebugUITools.launch(jopizeLaunchConfig, ILaunchManager.RUN_MODE);

            System.err.println("launch");

            return new IProject[] { getProject() };
        }

        return new IProject[0];
    }

    private ILaunchConfiguration createJOPizeLaunchConfiguration(
            String programArguments) throws CoreException {
        String configName = "JOPize";

        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type = manager
                .getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
        ILaunchConfiguration[] configs = manager.getLaunchConfigurations(type);

        for (ILaunchConfiguration config : configs) {
            if (config.getName().equals(configName)) {
                config.delete();

                break;
            }
        }

        ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null,
                configName);
        IVMInstall defaultVMInstall = JavaRuntime.getDefaultVMInstall();

        Map attributes = workingCopy.getAttributes();

        attributes.put(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
                "com.jopdesign.build.JOPizer");

        attributes.put(
                IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
                programArguments);

        IPreferenceStore prefs = JOPUIPlugin.getDefault().getPreferenceStore();
        String jopHome = prefs.getString(IJOPLaunchConfigurationConstants.ATTR_JOP_HOME);
        
        IPath jopTools = new Path(jopHome).append(new Path(
                "java/tools/dist/lib/jop-tools.jar"));
        IRuntimeClasspathEntry jopToolsEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(jopTools);
        jopToolsEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
        
        IPath jopClasses = new Path(jopHome).append(new Path(
                "java/target/dist/lib/classes.zip"));
        IRuntimeClasspathEntry jopClassesEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(jopClasses);
        jopClassesEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);

        // IClasspathEntry jreEntry = JavaRuntime.getDefaultJREContainerEntry();
        
        List<String> classpath = new ArrayList<String>();

        classpath.add(jopToolsEntry.getMemento());
        classpath.add(jopClassesEntry.getMemento());
        // classpath.add(jreEntry.get)

        attributes.put(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH,
                classpath);
        attributes
                .put(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
                        false);
        
        attributes.put(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
                "-Dmgci=false");

        workingCopy.setAttributes(attributes);
        
        

        System.err.printf("> %s%n", workingCopy.toString());

        return workingCopy;
    }

    private IClasspathEntry getToolsClasspathEntry() throws JavaModelException {
        IPreferenceStore prefs = JOPUIPlugin.getDefault().getPreferenceStore();
        String jopHome = prefs
                .getString(IJOPLaunchConfigurationConstants.ATTR_JOP_HOME);
        IPath jopTools = new Path(jopHome).append(new Path(
                "java/tools/dist/lib/jop-tools.jar"));
        IClasspathEntry[] entries = currentProject.getRawClasspath();

        for (IClasspathEntry entry : entries) {
            if (entry.getPath().equals(jopTools)) {
                return entry;
            }
        }

        return null;
    }

    private IClasspathEntry getClassesClasspathEntry()
            throws JavaModelException {
        IPreferenceStore prefs = JOPUIPlugin.getDefault().getPreferenceStore();
        String jopHome = prefs
                .getString(IJOPLaunchConfigurationConstants.ATTR_JOP_HOME);
        IPath jopClasses = new Path(jopHome).append(new Path(
                "java/target/dist/lib/classes.zip"));
        IClasspathEntry[] entries = currentProject.getRawClasspath();

        for (IClasspathEntry entry : entries) {
            if (entry.getPath().equals(jopClasses)) {
                return entry;
            }
        }

        return null;
    }

    private void buildToolChain(IProgressMonitor monitor) throws CoreException {
        IPreferenceStore prefs = JOPUIPlugin.getDefault().getPreferenceStore();
        String jopHome = prefs
                .getString(IJOPLaunchConfigurationConstants.ATTR_JOP_HOME);

        AntRunner antRunner = new AntRunner();

        antRunner.setBuildFileLocation(jopHome + IPath.SEPARATOR + "build.xml");
        antRunner.setExecutionTargets(new String[] { "directories", "tools",
                "classes" });

        try {
            antRunner.run(monitor);
        } catch (CoreException e) {
            // TODO only for demo since i don't have quartus etc installed
            e.printStackTrace();
        }
    }

    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        System.err.printf("%s%n", "Kleenin', yeah");
    }
}
