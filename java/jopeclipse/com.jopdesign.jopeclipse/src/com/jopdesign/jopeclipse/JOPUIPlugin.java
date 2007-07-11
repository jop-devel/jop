package com.jopdesign.jopeclipse;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.LegacyResourceSupport;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.jopdesign.jopeclipse.internal.ui.JOPUIImages;

/**
 * The activator class controls the plug-in life cycle
 */
public class JOPUIPlugin extends AbstractUIPlugin {
    // The plug-in ID
    public static final String PLUGIN_ID = "com.jopdesign.jopeclipse";

    // The shared instance
    private static JOPUIPlugin plugin;

    /**
     * The constructor
     */
    public JOPUIPlugin() {
        super();
        
        plugin = this;
    }

    /**
     * Returns the standard display to be used. The method first checks, if
     * the thread calling this method has an associated display. If so, this
     * display is returned. Otherwise the method returns the default display.
     */
    public static Display getStandardDisplay() {
        Display display = Display.getCurrent();

        if (display == null) {
            display = Display.getDefault();
        }

        return display;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static JOPUIPlugin getDefault() {
        return plugin;
    }
    
    /**
     * @return
     */
    public String getUniqueIdentifier() {
        return PLUGIN_ID;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
     */
    @Override
    protected void initializeImageRegistry(ImageRegistry imageRegistry) {
        JOPUIImages.initializeImageRegistry();
    }
}
