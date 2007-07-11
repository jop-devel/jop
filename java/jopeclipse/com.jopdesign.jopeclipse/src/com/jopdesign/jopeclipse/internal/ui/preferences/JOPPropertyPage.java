package com.jopdesign.jopeclipse.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.osgi.service.prefs.Preferences;

import com.jopdesign.jopeclipse.JOPUIPlugin;
import com.jopdesign.jopeclipse.internal.ui.IJOPUIConstants;
import com.jopdesign.jopeclipse.internal.ui.launchConfigurations.IJOPLaunchConfigurationConstants;

/**
 * @author johan
 *
 */
public class JOPPropertyPage extends FieldEditorPreferencePage implements
        IWorkbenchPropertyPage {
    private DirectoryFieldEditor jopDirectoryEditor;

    private FileFieldEditor quartusProjectFileEditor;

    /** Element that owns the properties */
    private IAdaptable element;

    /**
     * 
     */
    public JOPPropertyPage() {
        super(FieldEditorPreferencePage.GRID);

        setPreferenceStore(JOPUIPlugin.getDefault().getPreferenceStore());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        jopDirectoryEditor = new DirectoryFieldEditor(
                IJOPLaunchConfigurationConstants.ATTR_JOP_HOME,
                "JOP Directory", getFieldEditorParent());

        jopDirectoryEditor.setEmptyStringAllowed(false);
        jopDirectoryEditor.setErrorMessage("Not a valid JOP directory");

        addField(jopDirectoryEditor);

        quartusProjectFileEditor = new FileFieldEditor(
                IJOPLaunchConfigurationConstants.ATTR_QUARTUS_PROJECT,
                "Quartus project file", getFieldEditorParent());

        quartusProjectFileEditor.setEmptyStringAllowed(false);
        quartusProjectFileEditor.setFileExtensions(new String[] { "*.qpf" });
        quartusProjectFileEditor
                .setErrorMessage("Not a valid Quartus project file");

        addField(quartusProjectFileEditor);

        System.err.printf("Created field editors%n");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        IScopeContext scopeContext = new ProjectScope(getProject());
        Preferences projectPrefs = scopeContext
                .getNode(IJOPUIConstants.PLUGIN_ID);

        if (isValid()) {
            projectPrefs.put(IJOPLaunchConfigurationConstants.ATTR_JOP_HOME,
                    jopDirectoryEditor.getStringValue());
            projectPrefs.put(
                    IJOPLaunchConfigurationConstants.ATTR_QUARTUS_PROJECT,
                    quartusProjectFileEditor.getStringValue());
        }

        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performDefaults()
     */
    @Override
    public void performDefaults() {
        super.performDefaults();

        IProject project = getProject();
        IPath projectRoot = project.getLocation();
        IScopeContext scopeContext = new ProjectScope(project);
        Preferences projectPrefs = scopeContext
                .getNode(IJOPUIConstants.PLUGIN_ID);

        jopDirectoryEditor.setStringValue(projectPrefs.get(
                IJOPLaunchConfigurationConstants.ATTR_JOP_HOME, projectRoot
                        .toOSString()));

        IPath defaultQuartusProjectFile = projectRoot.append("quartus").append(
                "cycmin").append("jop").addFileExtension("qpf");

        quartusProjectFileEditor.setStringValue(projectPrefs.get(
                IJOPLaunchConfigurationConstants.ATTR_QUARTUS_PROJECT,
                defaultQuartusProjectFile.toOSString()));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
     */
    @Override
    public IAdaptable getElement() {
        return element;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPropertyPage#setElement(org.eclipse.core.runtime.IAdaptable)
     */
    @Override
    public void setElement(IAdaptable element) {
        this.element = element;
    }

    /**
     * @return project
     */
    private IProject getProject() {
        if (getElement() != null) {
            return (IProject) getElement().getAdapter(IProject.class);
        }

        return null;
    }
}
