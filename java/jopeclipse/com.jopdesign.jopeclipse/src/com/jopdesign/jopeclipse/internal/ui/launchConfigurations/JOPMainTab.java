package com.jopdesign.jopeclipse.internal.ui.launchConfigurations;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.jopdesign.jopeclipse.internal.VariableManagerUtils;
import com.jopdesign.jopeclipse.internal.ui.IJOPUIConstants;
import com.jopdesign.jopeclipse.internal.ui.JOPUIUtils;

/**
 * @author johan
 *
 */
public class JOPMainTab extends JavaMainTab {
    /**
     * A listener which handles widget change events for the controls in
     * this tab.
     */
    private class WidgetListener implements ModifyListener, SelectionListener {
        public void modifyText(ModifyEvent e) {
            if (!fInitializing) {
                setDirty(true);

                updateLaunchConfigurationDialog();
            }
        }

        public void widgetDefaultSelected(SelectionEvent e) {
        }

        public void widgetSelected(SelectionEvent e) {
            setDirty(true);

            Object source = e.getSource();

            if (source == fWorkspaceOutputDirectoryButton) {
                handleWorkspaceOutputDirectoryButtonSelected();
            } else if (source == fFileOutputDirectoryButton) {
                handleFileOutputDirectoryButtonSelected();
            } else if (source == fVariablesOutputDirectoryButton) {
                handleVariablesOutputDirectoryButtonSelected();
            }
        }
    }

    private Text fJOPizedOutputText;

    private Button fWorkspaceOutputDirectoryButton;

    private Button fFileOutputDirectoryButton;

    private Button fVariablesOutputDirectoryButton;

    private boolean fInitializing;

    // Default widget listener
    private WidgetListener fListener = new WidgetListener();

    /* (non-Javadoc)
     * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        Composite comp = (Composite) getControl();

        createVerticalSpacer(comp, 1);
        createOutputFolderEditor(comp);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
     */
    @Override
    public void initializeFrom(ILaunchConfiguration config) {
        fInitializing = true;

        updateOutputDirectoryFromConfig(config);
        super.initializeFrom(config);

        fInitializing = false;

        setDirty(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
     */
    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        String outputDir = fJOPizedOutputText.getText().trim();

        if (outputDir.length() == 0) {
            configuration.setAttribute(
                    IJOPLaunchConfigurationConstants.ATTR_JOPIZED_DIR,
                    (String) null);
        } else {
            configuration.setAttribute(
                    IJOPLaunchConfigurationConstants.ATTR_JOPIZED_DIR,
                    outputDir);
        }

        super.performApply(configuration);
    }

    /**
     * Prompts the user to choose and configure a variable and returns
     * the resulting string, suitable to be used as an attribute.
     */
    private String getStringVariable() {
        StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(
                getShell());
        dialog.open();

        return dialog.getVariableExpression();
    }

    /**
     * @param parent
     */
    protected void createOutputFolderEditor(Composite parent) {
        Group group = JOPUIUtils.createHorizontalGrabGroup(parent);
        group.setText("JOPized files output folder:");

        GridLayout layout = new GridLayout(1, false);

        group.setLayout(layout);

        fJOPizedOutputText = new Text(group, SWT.BORDER);

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;

        fJOPizedOutputText.setLayoutData(gd);
        fJOPizedOutputText.addModifyListener(fListener);

        Composite buttonComposite = new Composite(group, SWT.NONE);

        layout = new GridLayout(3, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;

        buttonComposite.setLayout(layout);

        gd = new GridData(GridData.HORIZONTAL_ALIGN_END);

        buttonComposite.setLayoutData(gd);

        // Browse workspace button
        fWorkspaceOutputDirectoryButton = createPushButton(buttonComposite,
                "Browse Workspace...", null);
        fWorkspaceOutputDirectoryButton.addSelectionListener(fListener);

        // Browse file system button
        fFileOutputDirectoryButton = createPushButton(buttonComposite,
                "Browse File System...", null);
        fFileOutputDirectoryButton.addSelectionListener(fListener);

        // Variables button
        fVariablesOutputDirectoryButton = createPushButton(buttonComposite,
                "Variables...", null);
        fVariablesOutputDirectoryButton.addSelectionListener(fListener);
    }

    /**
     * @return
     */
    protected String getDefaultOutputDirectory() {
        return "${project_loc:/bin/jop}";
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
     */
    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(
                IJOPLaunchConfigurationConstants.ATTR_JOPIZED_DIR,
                getDefaultOutputDirectory());

        super.setDefaults(configuration);
    }

    /**
     * Prompts the user to choose a location from the filesystem and
     * sets the location as the full path of the selected file.
     */
    protected void handleFileOutputDirectoryButtonSelected() {
        DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);

        dialog.setMessage("Select a directory to place JOPized files");
        dialog.setFilterPath(fJOPizedOutputText.getText());

        String directory = dialog.open();

        if (directory != null) {
            fJOPizedOutputText.setText(directory);
        }
    }

    /**
     * A variable entry button has been pressed for the given text
     * field. Prompt the user for a variable and enter the result
     * in the given field.
     */
    protected void handleVariablesOutputDirectoryButtonSelected() {
        String variable = getStringVariable();

        if (variable != null) {
            fJOPizedOutputText.append(variable);
        }
    }

    /**
     * Prompts the user for a working directory location within the workspace
     * and sets the working directory as a String containing the workspace_loc
     * variable or <code>null</code> if no location was obtained from the user.
     */
    protected void handleWorkspaceOutputDirectoryButtonSelected() {
        // ContainerSelectionDialog dialog;
    }

    /**
     * Updates the working directory widgets to match the state of the given launch
     * configuration.
     */
    protected void updateOutputDirectoryFromConfig(
            ILaunchConfiguration configuration) {
        String out = IJOPUIConstants.EMPTY_STRING; // lolcat in ur empty string

        try {
            out = configuration.getAttribute(
                    IJOPLaunchConfigurationConstants.ATTR_JOPIZED_DIR,
                    IJOPUIConstants.EMPTY_STRING);
        } catch (CoreException e) {
            setErrorMessage(e.getLocalizedMessage());
        }

        fJOPizedOutputText.setText(out);
    }

    /**
     * @return
     */
    private boolean validateOutputDirectory() {
        String dir = fJOPizedOutputText.getText().trim();

        if (dir.length() == 0) {
            setErrorMessage(null);
            setMessage("Output location can not be empty");

            return false;
        }

        String expandedDir = null;

        try {
            expandedDir = VariableManagerUtils.resolveValue(dir);

            if (expandedDir == null) {
                // A variable needs to be resolved at run-time
                return true;
            }
        } catch (CoreException e) {
            setErrorMessage(e.getStatus().getMessage());

            return false;
        }

        File file = new File(expandedDir);

        if (!file.exists()) { // The file does not exist.
            setErrorMessage("Location_does_not_exist");

            return false;
        }

        if (!file.isDirectory()) {
            setErrorMessage("Location_specified_is_not_a_directory_20");

            return false;
        }

        return true;
    }

    @Override
    public boolean isValid(ILaunchConfiguration config) {
        return super.isValid(config) && validateOutputDirectory();
    }
}
