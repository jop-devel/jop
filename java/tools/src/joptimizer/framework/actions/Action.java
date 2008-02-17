/*
 * Copyright (c) 2007,2008, Stefan Hepp
 *
 * This file is part of JOPtimizer.
 *
 * JOPtimizer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JOPtimizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package joptimizer.framework.actions;

import joptimizer.config.ConfigurationException;
import joptimizer.config.JopConfig;
import joptimizer.framework.JOPtimizer;

import java.util.List;

/**
 * Base interface for all actions.
 *
 * NOTICE register actions to factory somehow, define order/deps of actions?
 * NOTICE more 'generic' way (through AppStruct or some 'walker' class) to run actions only on subsets of classes.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface Action {

    /**
     * Get the name by which this action can be created by the factory.
     *
     * @return the name of this action.
     */
    String getActionName();

    /**
     * Get the id by which multiple instances of the same action can be distinguised.
     * @return the id of this action used for configuration.
     */
    String getActionId();

    /**
     * Get the Joptimizer instance which this action uses.
     *
     * @return the Joptimizer instance.
     */
    JOPtimizer getJoptimizer();

    /**
     * Get the configuration from the Joptimizer.
     *
     * @return the config from the joptimizer instance.
     */
    JopConfig getJopConfig();

    /**
     * append arguments for this action to the given map.
     * This may be called before {@link #configure(joptimizer.config.JopConfig)} is called.
     *
     * @param options argument-map with option-name as key and an ArgOption as value.
     */
    void appendActionArguments(List options);

    /**
     * get a descriptive short text about what this action does.
     * This may be called before {@link #configure(joptimizer.config.JopConfig)} is called.
     *
     * @return a description showed in the usage message.
     */
    String getActionDescription();

    /**
     * returns true if this action may modifiy the classes anyhow.
     * This may be called before {@link #configure(joptimizer.config.JopConfig)} is called.
     *
     * @return true if this modifies the classes.
     */
    boolean doModifyClasses();

    /**
     * configure an action by configuration and check if this action can be executed.
     *
     * @param config the configuration to use.
     * @return true if this action is now fully configured and ready to use, else false.
     * @throws joptimizer.config.ConfigurationException
     */
    boolean configure(JopConfig config) throws ConfigurationException;

    /**
     * Start an action. <br>
     *
     * This method must be called before any execute() method is called.
     *
     * @throws ActionException
     */
    void startAction() throws ActionException;

    /**
     * Finish the current action run. <br>
     *
     * This method must be called after all execute() methods are called for a set of classes.
     *
     * @throws joptimizer.framework.actions.ActionException
     */
    void finishAction() throws ActionException;

    /**
     * Run this action on all classes of the AppStruct container of the Joptimizer. <br>
     *
     * @throws ActionException
     */
    void execute() throws ActionException;

}
