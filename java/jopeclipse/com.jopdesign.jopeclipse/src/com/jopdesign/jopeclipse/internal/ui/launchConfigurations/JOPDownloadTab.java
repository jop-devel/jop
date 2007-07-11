package com.jopdesign.jopeclipse.internal.ui.launchConfigurations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.jopdesign.jopeclipse.internal.RXTXCommUtils;
import com.jopdesign.jopeclipse.internal.ui.CommPortIdentifierContentProvider;
import com.jopdesign.jopeclipse.internal.ui.CommPortIdentifierLabelProvider;
import com.jopdesign.jopeclipse.internal.ui.IJOPUIConstants;
import com.jopdesign.jopeclipse.internal.ui.JOPUIImages;
import com.jopdesign.jopeclipse.internal.ui.JOPUIUtils;

public class JOPDownloadTab extends AbstractLaunchConfigurationTab {
    /**
     * A listener which handles widget change events for the controls in
     * this tab.
     */
    private class WidgetListener implements ModifyListener, SelectionListener {
        public void modifyText(ModifyEvent e) {
            setDirty(true);

            updateLaunchConfigurationDialog();
        }

        public void widgetDefaultSelected(SelectionEvent e) {
        }

        public void widgetSelected(SelectionEvent e) {
            setDirty(true);
            updateLaunchConfigurationDialog();
        }
    }

    private ComboViewer commPortViewer;

    private Button useUsbButton;

    private Button testConnectionButton;

    // Default widget listener
    private WidgetListener listener = new WidgetListener();

    private Button simulateButton;

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);

        GridLayout topLayout = new GridLayout();
        topLayout.verticalSpacing = 0;
        comp.setLayout(topLayout);

        createSimulateCheckbox(comp);

        createCommPortViewer(comp);
        createVerticalSpacer(comp, 1);
        createBlasterTypeViewer(comp);

        Dialog.applyDialogFont(parent);
    }

    /**
     * 
     * @param parent
     */
    private void createSimulateCheckbox(Composite parent) {
        Group g = JOPUIUtils.createHorizontalGrabGroup(parent);
        simulateButton = createCheckButton(g,
                "Simulate with JopSim (overrides other settings)");
        simulateButton.addSelectionListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
     */
    @Override
    public Image getImage() {
        return JOPUIImages.getImage(IJOPUIConstants.IMG_DOWNLOAD_TAB);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
     */
    public String getName() {
        return "JOP Download";
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
     */
    public void initializeFrom(ILaunchConfiguration configuration) {
        // Update COM port
        String commPortId = "";

        try {
            commPortId = configuration.getAttribute(
                    IJOPLaunchConfigurationConstants.ATTR_COM_PORT, "");
        } catch (CoreException e) {

        }

        Combo combo = commPortViewer.getCombo();
        combo.select(combo.indexOf(commPortId));
        commPortViewer.refresh();

        // Update USB button
        try {
            useUsbButton.setSelection(configuration.getAttribute(
                    IJOPLaunchConfigurationConstants.ATTR_USB_FLAG, false));
        } catch (CoreException e) {
            useUsbButton.setSelection(false);
        }

        // Update simulate checkbox
        try {
            simulateButton.setSelection(configuration.getAttribute(
                    IJOPLaunchConfigurationConstants.ATTR_SIMULATE, true));
        } catch (CoreException e) {
            simulateButton.setSelection(true);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
     */
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        // Set COM port identifier
        if (!commPortViewer.getSelection().isEmpty()) {
            String commPortId = commPortViewer.getCombo().getText();

            configuration.setAttribute(
                    IJOPLaunchConfigurationConstants.ATTR_COM_PORT, commPortId);
        }

        // Set USB flag
        configuration.setAttribute(
                IJOPLaunchConfigurationConstants.ATTR_USB_FLAG, useUsbButton
                        .getSelection());

        configuration.setAttribute(
                IJOPLaunchConfigurationConstants.ATTR_SIMULATE, simulateButton
                        .getSelection());

        commPortViewer.getCombo().setEnabled(!simulateButton.getSelection());
        useUsbButton.setEnabled(!simulateButton.getSelection());
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        // USB downloading off by default
        configuration.setAttribute(
                IJOPLaunchConfigurationConstants.ATTR_USB_FLAG, false);

        // Simulation on
        configuration.setAttribute(
                IJOPLaunchConfigurationConstants.ATTR_SIMULATE, true);
    }

    private void createBlasterTypeViewer(Composite parent) {
        Group group = JOPUIUtils.createHorizontalGrabGroup(parent);
        group.setText("Blaster Type");

        // TODO: fill blaster type list
    }

    private void createCommPortViewer(Composite parent) {
        Group group = JOPUIUtils.createHorizontalGrabGroup(parent);
        group.setLayout(new GridLayout(2, false));
        group.setText("COM Port:");

        commPortViewer = new ComboViewer(group, SWT.SINGLE | SWT.READ_ONLY);
        commPortViewer
                .setContentProvider(new CommPortIdentifierContentProvider());
        commPortViewer.setInput(RXTXCommUtils.getAvailableSerialPorts());
        commPortViewer.setLabelProvider(new CommPortIdentifierLabelProvider());

        createVerticalSpacer(group, 1);

        useUsbButton = createCheckButton(group, "Download with USB");
        useUsbButton.addSelectionListener(listener);

        testConnectionButton = createPushButton(group, "Test connection", null);

        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
        testConnectionButton.setLayoutData(gd);
    }
}
