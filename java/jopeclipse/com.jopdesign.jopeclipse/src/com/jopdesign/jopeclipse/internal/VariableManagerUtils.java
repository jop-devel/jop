package com.jopdesign.jopeclipse.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;

public class VariableManagerUtils {
    /**
     * Returns the default string variable manager.
     * 
     * @return the default string variable manager
     */
    public static IStringVariableManager getDefaultStringVariableManager() {
        return VariablesPlugin.getDefault().getStringVariableManager();
    }

    /**
     * Validates the variables of the given string to determine if all variables
     * are valid
     * 
     * @param expression expression with variables
     * @exception CoreException if a variable is specified that does not exist
     */
    public static void validateVaribles(String expression) throws CoreException {
        getDefaultStringVariableManager().validateStringVariables(expression);
    }

    public static String resolveValue(String expression) throws CoreException {
        String expanded = null;

        try {
            expanded = getValue(expression);
        } catch (CoreException e) {
            // Possibly just a variable that needs to be resolved at runtime
            validateVaribles(expression);

            return null;
        }

        return expanded;
    }

    /**
     * Validates the value of the given string to determine if any/all variables
     * are valid
     * 
     * @param expression expression with variables
     * @return whether the expression contained any variable values
     * @exception CoreException if variable resolution fails
     */
    private static String getValue(String expression) throws CoreException {
        return getDefaultStringVariableManager().performStringSubstitution(
                expression);
    }

    /**
     * Returns a new variable expression with the given variable and the given 
     * argument.
     * 
     * @see IStringVariableManager#generateVariableExpression(String, String)
     */
    public static String newVariableExpression(String varName, String arg) {
        return getDefaultStringVariableManager().generateVariableExpression(
                varName, arg);
    }
}
