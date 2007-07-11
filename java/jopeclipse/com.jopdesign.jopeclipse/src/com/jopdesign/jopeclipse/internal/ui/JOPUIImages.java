package com.jopdesign.jopeclipse.internal.ui;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;

import com.jopdesign.jopeclipse.JOPUIPlugin;

/**
 * Provider class for image resources.
 * 
 * @author johan
 */
public class JOPUIImages {
    private final static String ICONS_PATH = "/icons/";

    /**
     * 
     * @return
     */
    public static ImageRegistry initializeImageRegistry() {
        declareImages();

        return JOPUIPlugin.getDefault().getImageRegistry();
    }

    private static void declareImages() {
        declareRegistryImage(IJOPUIConstants.IMG_JAVA_TAB, ICONS_PATH
                + "cog.gif");
        declareRegistryImage(IJOPUIConstants.IMG_DOWNLOAD_TAB, ICONS_PATH
                + "control_play.gif");
        declareRegistryImage(IJOPUIConstants.IMG_CONFIGURE_BOARD_TAB,
                ICONS_PATH + "wrench_orange.gif");
        declareRegistryImage(IJOPUIConstants.IMG_WCET_TAB, ICONS_PATH
                + "clock.gif");
    }

    /**
     * Declare an Image in the registry table.
     * @param key   The key to use when registering the image
     * @param path  The path where the image can be found. This path is relative to where
     *              this plugin class is found (i.e. typically the packages directory)
     */
    private final static void declareRegistryImage(String key, String path) {
        ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
        Bundle bundle = Platform.getBundle(JOPUIPlugin.PLUGIN_ID);
        URL url = null;

        if (bundle != null) {
            url = FileLocator.find(bundle, new Path(path), null);
            desc = ImageDescriptor.createFromURL(url);
        }

        JOPUIPlugin.getDefault().getImageRegistry().put(key, desc);
    }

    /**
     * 
     * @param key
     * @return
     */
    public static Image getImage(String key) {
        return JOPUIPlugin.getDefault().getImageRegistry().get(key);
    }

    /**
     * 
     * @param key
     * @return
     */
    public static ImageDescriptor getImageDescriptor(String key) {
        return JOPUIPlugin.getDefault().getImageRegistry().getDescriptor(key);
    }
}
