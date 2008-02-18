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
package joptimizer.framework;

import joptimizer.actions.CallGraphPrinter;
import joptimizer.actions.ClassInfoLoader;
import joptimizer.actions.ClassInfoPrinter;
import joptimizer.actions.ClassWriter;
import joptimizer.actions.ControlFlowGraphPrinter;
import joptimizer.actions.GraphHelper;
import joptimizer.actions.MethodTransformAction;
import joptimizer.actions.TransitiveHullGenerator;
import joptimizer.config.BoolOption;
import joptimizer.framework.actions.Action;
import joptimizer.framework.actions.ActionCreator;
import joptimizer.optimizer.CodeStripper;
import joptimizer.optimizer.InlineOptimizer;
import joptimizer.optimizer.PeepholeOptimizer;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The action configurator creates all ActionCreators, configures default values and
 * creates the current action-list depending on the current configuration.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ActionConfigurator {

    public static final String PROP_CONFIGFILE = "actionconfig";

    private List actions;
    private Map actionIds;
    private JOPtimizer joptimizer;

    private static final Logger logger = Logger.getLogger(ActionConfigurator.class);

    public ActionConfigurator(JOPtimizer joptimizer) {
        this.joptimizer = joptimizer;
        actions = new LinkedList();
        actionIds = new HashMap();
        initActionCreators();
    }

    /**
     * Create common options for this configurator.
     * @param options a list of {@link joptimizer.config.ArgOption} where options whill be added.
     */
    public void addArguments(List options) {
    }

    public void createEnableOptions(List options, ActionCreator creator) {

        Set ids = getActionIds(creator.getActionName());
        for (Iterator it2 = ids.iterator(); it2.hasNext();) {
            String id = (String) it2.next();

            options.add(new BoolOption("enable", id, creator.getActionDescription()));
        }
    }

    /**
     * Check if an action is enabled per configuration.
     * @param actionId the id of the action.
     * @return 0 if disabled, 1 if enabled, -1 if not set.
     */
    public int isActionEnabled(String actionId) {
        if ( !joptimizer.getJopConfig().isSet("enabled."+actionId) ) {
            return -1;
        }
        return joptimizer.getJopConfig().isEnabled("enabled."+actionId) ? 1 : 0;
    }

    /**
     * Set default configuration values to the config for all actions.
     * Overwrites existing values.
     */
    public void setDefaultOptions() {
    }

    public Collection getActionCreators() {
        return actions;
    }

    public Set getActionIds(String actionName) {
        // TODO don't create default ID (create empty set), configure actionIDs during init
        Set ids = (Set) actionIds.get(actionName);
        return ids != null ? ids : Collections.singleton(actionName);
    }

    /**
     * Get a list of action Ids to execute.
     *
     * @param optLevel optimization level, see {@link joptimizer.framework.ActionFactory#CONF_OPTIMIZE_LEVEL}
     * @return entries are either an action Id string or an array of lists of action ids (stage->action id list) for nested graph actions.
     */
    public List getConfiguredActionIds(int optLevel) {
        List confActions = new LinkedList();

        // TODO load action list from config, add options to add action-instances

        addOptimizeAction(confActions, InlineOptimizer.ACTION_NAME, optLevel, 2);
        addOptimizeAction(confActions, PeepholeOptimizer.ACTION_NAME, optLevel, 1);
        addOptimizeAction(confActions, CodeStripper.ACTION_NAME, optLevel, 3);
    
        addOptionalAction(confActions, CallGraphPrinter.ACTION_NAME);
        addOptionalAction(confActions, ClassInfoPrinter.ACTION_NAME);
        addOptionalAction(confActions, ControlFlowGraphPrinter.ACTION_NAME);

        return confActions;
    }

    public List getWriteActions() {
        return Collections.singletonList(ClassWriter.ACTION_NAME);
    }

    private boolean addOptimizeAction(List actions, String id, int optLevel, int minLevel) {

        int enabled = isActionEnabled(id);

        if ( enabled == 0 || (enabled == -1 && optLevel < minLevel) ) {
            return false;
        }

        actions.add(id);
        return true;
    }

    private boolean addOptionalAction(List actions, String id) {
        if ( isActionEnabled(id) != 1 ) {
            return false;
        }

        actions.add(id);
        return true;
    }

    private void initActionCreators() {

        actionIds.clear();
        actions.clear();

        // TODO load actions/IDs/actionlist from configuration file (get from resource).

        actions.add(new ActionCreator(ActionCreator.SHOW_NONE, TransitiveHullGenerator.ACTION_NAME) {
            public Action createAction(String id) {
                return new TransitiveHullGenerator(TransitiveHullGenerator.ACTION_NAME, id, joptimizer);
            }
        });

        actions.add( new ActionCreator(ActionCreator.SHOW_NONE, ClassInfoLoader.ACTION_NAME ) {
            public Action createAction(String id) {
                return new ClassInfoLoader(ClassInfoLoader.ACTION_NAME, id, joptimizer);
            }
        });

        actions.add(new ActionCreator(ActionCreator.SHOW_ACTION, CallGraphPrinter.ACTION_NAME) {
            public Action createAction(String id) {
                return new CallGraphPrinter(CallGraphPrinter.ACTION_NAME, id, joptimizer);
            }
        });

        actions.add(new ActionCreator(ActionCreator.SHOW_NONE, ClassInfoPrinter.ACTION_NAME) {
            public Action createAction( String id) {
                return new ClassInfoPrinter(ClassInfoPrinter.ACTION_NAME, id, joptimizer);
            }
        });

        actions.add(new ActionCreator(ActionCreator.SHOW_NONE, ControlFlowGraphPrinter.ACTION_NAME) {
            public Action createAction(String id) {
                return new ControlFlowGraphPrinter(ControlFlowGraphPrinter.ACTION_NAME, id, joptimizer);
            }
        });

        actions.add(new ActionCreator(ActionCreator.SHOW_NONE, GraphHelper.ACTION_NAME) {
            public Action createAction(String id) {
                return new GraphHelper(GraphHelper.ACTION_NAME, id, joptimizer);
            }
        });

        actions.add(new ActionCreator(ActionCreator.SHOW_OPTIONS, MethodTransformAction.ACTION_NAME) {
            public Action createAction(String id) {
                return new MethodTransformAction(MethodTransformAction.ACTION_NAME, id, joptimizer);
            }
        });

        actions.add(new ActionCreator(ActionCreator.SHOW_OPTIONS, InlineOptimizer.ACTION_NAME) {
            public Action createAction(String id) {
                return new InlineOptimizer(InlineOptimizer.ACTION_NAME, id, joptimizer);
            }
        });

        actions.add(new ActionCreator(ActionCreator.SHOW_OPTIONS, PeepholeOptimizer.ACTION_NAME) {
            public Action createAction(String id) {
                return new PeepholeOptimizer(PeepholeOptimizer.ACTION_NAME, id, joptimizer);
            }
        });

        actions.add(new ActionCreator(ActionCreator.SHOW_ACTION, CodeStripper.ACTION_NAME) {
            public Action createAction(String id) {
                return new CodeStripper(CodeStripper.ACTION_NAME, id, joptimizer);
            }
        });

        actions.add(new ActionCreator(ActionCreator.SHOW_OPTIONS, ClassWriter.ACTION_NAME) {
            public Action createAction(String id) {
                return new ClassWriter(ClassWriter.ACTION_NAME, id, joptimizer);
            }
        });

    }

    private URL getConfigfileUrl() throws MalformedURLException {
        String file = System.getProperty(PROP_CONFIGFILE);
        URL url;
        if ( file == null ) {
            url = this.getClass().getResource("actionconfig.xml");
        } else {
            url = new URL(file);
        }
        return url;
    }

}
