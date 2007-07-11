package com.jopdesign.jopeclipse.internal.ui.launchConfigurations;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.jopdesign.jopeclipse.internal.ui.IJOPUIConstants;
import com.jopdesign.jopeclipse.internal.ui.JOPUIImages;
import com.jopdesign.jopeclipse.internal.ui.JOPUIUtils;

/**
 * @author johan
 *
 */
public class JOPBoardConfigurationTab extends AbstractLaunchConfigurationTab {
    protected Text fQuartusProjectText;

    protected IPath fQuartusProjectLocation;

    protected Button fQuartusProjButton;

    /**
     * A listener which handles widget change events for the controls in
     * this tab.
     */
    private class WidgetListener implements ModifyListener, SelectionListener {
        public void modifyText(ModifyEvent e) {
            updateLaunchConfigurationDialog();
        }

        public void widgetSelected(SelectionEvent e) {
            Object source = e.getSource();

            if (source == fQuartusProjButton) {
                handleQuartusProjectButtonSelected();
            } else {
                updateLaunchConfigurationDialog();
            }
        }

        public void widgetDefaultSelected(SelectionEvent e) {
        }
    }

    // Default widget listener
    private WidgetListener fListener = new WidgetListener();

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);

        GridLayout topLayout = new GridLayout();
        topLayout.verticalSpacing = 0;
        comp.setLayout(topLayout);

        createQuartusProjectEditor(comp);

        Dialog.applyDialogFont(parent);
    }

    /**
     * Create Quarus file group.
     * 
     * @param parent
     */
    private void createQuartusProjectEditor(Composite parent) {
        Group group = JOPUIUtils.createHorizontalGrabGroup(parent);
        group.setLayout(new GridLayout(2, false));
        group.setText("Quartus project file:");

        fQuartusProjectText = new Text(group, SWT.SINGLE | SWT.BORDER);
        fQuartusProjectText.setLayoutData(JOPUIUtils.FILL_HORIZONTAL);

        fQuartusProjButton = createPushButton(group, "Browse...", null);
        fQuartusProjButton.addSelectionListener(fListener);
    }

    /**
     * @param configuration
     */
    protected void updateQuartusProjectLocation(
            ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(
                IJOPLaunchConfigurationConstants.ATTR_QUARTUS_PROJECT,
                fQuartusProjectLocation.toString());

        JOPUIUtils.setProjectSetting(configuration,
                IJOPLaunchConfigurationConstants.ATTR_QUARTUS_PROJECT,
                fQuartusProjectLocation.toString());
    }

    /**
     * @param configuration
     */
    protected void updateQuartusProjectLocationFromConfig(
            ILaunchConfiguration configuration) {
        String qproj = IJOPUIConstants.EMPTY_STRING;

        try {
            qproj = configuration.getAttribute(
                    IJOPLaunchConfigurationConstants.ATTR_QUARTUS_PROJECT,
                    IJOPUIConstants.EMPTY_STRING);
        } catch (CoreException e) {
            setErrorMessage(e.getStatus().getMessage());
        }

        fQuartusProjectLocation = new Path(qproj);
        fQuartusProjectText.setText(fQuartusProjectLocation.toOSString());
    }

    /**
     * Handle selection of the Quartus project file button. 
     *
     */
    protected void handleQuartusProjectButtonSelected() {
        FileDialog dialog = new FileDialog(getShell());

        IPath rootPath = fQuartusProjectLocation != null ? fQuartusProjectLocation
                .removeLastSegments(1)
                : ResourcesPlugin.getWorkspace().getRoot().getLocation();

        dialog.setFilterPath(rootPath.toString());
        dialog.setFilterExtensions(new String[] { "*.qpf" });

        try {
            dialog.open();

            if (dialog.getFileName().equals("")) {
                return;
            }

            IPath filterPath = new Path(dialog.getFilterPath());
            IPath fileName = new Path(dialog.getFileName());

            fQuartusProjectLocation = filterPath.append(fileName);
            fQuartusProjectText.setText(fQuartusProjectLocation.toString());
        } catch (SWTException e) {

        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
     */
    public String getName() {
        return JOPLaunchConfigurationMessages.JOP_BOARDCONFIGURATIONTAB_NAME;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
     */
    @Override
    public Image getImage() {
        return JOPUIImages.getImage(IJOPUIConstants.IMG_CONFIGURE_BOARD_TAB);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
     */
    public void initializeFrom(ILaunchConfiguration configuration) {
        updateQuartusProjectLocationFromConfig(configuration);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
     */
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        if (fQuartusProjectLocation != null) {
            configuration.setAttribute(
                    IJOPLaunchConfigurationConstants.ATTR_QUARTUS_PROJECT,
                    fQuartusProjectLocation.toString());
        } else {
            configuration.setAttribute(
                    IJOPLaunchConfigurationConstants.ATTR_QUARTUS_PROJECT,
                    IJOPUIConstants.EMPTY_STRING);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        if (fQuartusProjectText == null) {
            return;
        }
        
        fQuartusProjectText.setText(JOPUIUtils.getProjectSetting(configuration,
                IJOPLaunchConfigurationConstants.ATTR_QUARTUS_PROJECT, ""));
    }
}
