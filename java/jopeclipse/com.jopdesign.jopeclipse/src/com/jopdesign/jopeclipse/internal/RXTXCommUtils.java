package com.jopdesign.jopeclipse.internal;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for easier access to the RXTX library
 * 
 * @author johan
 */
public class RXTXCommUtils {
    /**
     * @return A HashSet containing the CommPortIdentifier for all ports
     *         of type <i>commPortIdentifier</i> that are not currently
     *         being used.
     */
    public static Set<CommPortIdentifier> getAvailablePorts(
            int commPortIdentifier) {
        Set<CommPortIdentifier> identifiers = new HashSet<CommPortIdentifier>();
        Enumeration<CommPortIdentifier> portIdentifiers = CommPortIdentifier.getPortIdentifiers();

        while (portIdentifiers.hasMoreElements()) {
            CommPortIdentifier com = portIdentifiers.nextElement();

            if (com.getPortType() == commPortIdentifier) {
                try {
                    com.open("CommUtil", 50).close();

                    identifiers.add(com);
                } catch (PortInUseException e) {

                } catch (Exception e) {

                }
            }
        }

        return identifiers;
    }

    /**
     * Returns all available serial ports.
     * 
     * @return all available serial ports
     */
    public static Set<CommPortIdentifier> getAvailableSerialPorts() {
        return getAvailablePorts(CommPortIdentifier.PORT_SERIAL);
    }

    /**
     * Returns all available parallel ports.
     * 
     * @return all available parallel ports
     */
    public static Set<CommPortIdentifier> getAvailableParallelPorts() {
        return getAvailablePorts(CommPortIdentifier.PORT_PARALLEL);
    }
}
