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

import joptimizer.actions.MethodTransformAction;
import joptimizer.config.ArgOption;
import joptimizer.config.ConfigurationException;
import joptimizer.config.IntOption;
import joptimizer.config.JopConfig;
import joptimizer.framework.actions.Action;
import joptimizer.framework.actions.ActionCreator;
import joptimizer.framework.actions.GraphAction;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generate (known) actions and configure actions by configuration.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ActionFactory {

    /**
     * Configuration option for optimization level. <br>
     * <br>
     * Levels are: <br>
     * <ul>
     * <li>0: no optimization.</li>
     * <li>1: basic optimizations, only optimizations which don't require any assumtions.</li>
     * <li>2: all standard and stable optimizations.</li>
     * <li>3: all optizations including some more or less experimental or maybe incorrect optimizations.</li>
     * </ul>
     */
    public static final String CONF_OPTIMIZE_LEVEL = "O";

    private JOPtimizer joptimizer;
    private ActionConfigurator configurator;

    /**
     * a map of actioncreators for actions stored by action-name.
     * The same action may be stored more than once in this map with different names
     * which allows to create actions with different settings.
     */
    private Map actions;

    private Map actionIds;

    private static final Logger logger = Logger.getLogger(ActionFactory.class);

    public ActionFactory(JOPtimizer joptimizer) {
        this.joptimizer = joptimizer;
        configurator = new ActionConfigurator(joptimizer);
        initActions();
        configurator.setDefaultOptions();
    }

    public JOPtimizer getJoptimizer() {
        return joptimizer;
    }

    public JopConfig getConfig() {
        return joptimizer.getJopConfig();
    }

    /**
     * Get a map of all known actions with descriptions.
     * @return a list of all actions, with the name as key and the description as value.
     */
    public Map getActionNames() {
        Map names = new LinkedHashMap(actions.size());

        for (Iterator it = actions.entrySet().iterator(); it.hasNext();) {
            Map.Entry action = (Map.Entry) it.next();
            ActionCreator creator = (ActionCreator) action.getValue();
            String key = action.getKey().toString();

            names.put(key, creator.getActionDescription());
        }

        return names;
    }

    /**
     * create a list for all known options for all actions.
     * @return a list with all known options concerning actions.
     */
    public List createActionArguments() {

        List options = new LinkedList();

        options.add(new IntOption(null, CONF_OPTIMIZE_LEVEL,
                "Set optimization level as number from 0 to 3, default is 1.", "level"));

        configurator.addArguments(options);

        for (Iterator it = actions.values().iterator(); it.hasNext();) {
            ActionCreator creator = (ActionCreator) it.next();

            if ( creator.doShowArguments() == ActionCreator.SHOW_ACTION ) {
                configurator.createEnableOptions(options, creator);
            }
            if ( creator.doShowArguments() == ActionCreator.SHOW_OPTIONS ||
                 creator.doShowArguments() == ActionCreator.SHOW_ACTION )
            {
                creator.createArguments(options);

                List tmp = new LinkedList();

                // add options for all ids, but set to invisible
                Set ids = configurator.getActionIds(creator.getActionName());
                for (Iterator it2 = ids.iterator(); it2.hasNext();) {
                    String id = (String) it2.next();
                    if ( id.equals(creator.getActionName()) ) continue;
                    creator.createArguments(id, tmp);
                }

                for (Iterator it2 = tmp.iterator(); it2.hasNext();) {
                    ArgOption option = (ArgOption) it2.next();
                    option.setVisible(false);
                }

                options.addAll(tmp);
            }
        }

        return options;
    }

    public Set getActionIds(String actionName) {
        return configurator.getActionIds(actionName);
    }

    /**
     * Get a list of all options for an action.
     * @param action the id of the action.
     * @return a list of Options for the action, or null if action not found.
     */
    public List createActionArguments(String action) {

        ActionCreator ac = getActionCreator(action);
        if ( ac == null ) {
            return null;
        }

        List options = new LinkedList();
        ac.createArguments(options);
        return options;
    }

    /**
     * Create a new action by id.
     * The created action is not configured, so use {@link Action#configure(joptimizer.config.JopConfig)}
     * before executing the action.
     *
     * @param actionId the id of the action.
     * @return a new, unconfigured action or null if not found.
     */
    public Action createAction(String actionId) {
        ActionCreator actionCreator = getActionCreator(actionId);
        return actionCreator != null ? actionCreator.createAction(actionId) : null;
    }

    /**
     * configure an action with the current configuration and its prefix.
     * @param action the action to configure.
     * @throws ConfigurationException if a configuration error occurs or the actionname is not known.
     */
    public void configureAction(Action action) throws ConfigurationException {
        action.configure(joptimizer.getJopConfig());
    }

    /**
     * Create a list of all configured actions which should be executed in the correct order.
     * This does not configure the actions, therefore {@link Action#configure(joptimizer.config.JopConfig)} must
     * be called before execution.
     *
     * @return a list of Actions to be executed.
     */
    public List createConfiguredActions() {

        // check if any action modifies the classes, if so, add classwriter
        boolean modified = false;

        List confActions = new LinkedList();
        List actionList = configurator.getConfiguredActionIds(getOptimizationLevel());

        for (Iterator it = actionList.iterator(); it.hasNext();) {
            Object entry = it.next();

            // some extrawürscht for graph actions, to transform code in a single step
            // and therefore there is no need to keep all CFGs in memory of all methods.
            if ( entry instanceof List[] ) {

                List[] subActions = (List[]) entry;

                MethodTransformAction mta = (MethodTransformAction) createAction(MethodTransformAction.ACTION_NAME);
                confActions.add(mta);

                for (int i = 0; i < subActions.length; i++) {
                    for (Iterator it2 = subActions[i].iterator(); it.hasNext();) {
                        String id = (String) it2.next();

                        GraphAction graphAction = (GraphAction) createAction(id);
                        modified |= graphAction.doModifyClasses();

                        mta.addAction(i, graphAction);
                    }
                }

            } else {
                String id = entry.toString();

                Action action = createAction(id);
                modified |= action.doModifyClasses();

                confActions.add(action);
            }
        }

        if ( modified ) {
            List writeActions = configurator.getWriteActions();
            for (Iterator it = writeActions.iterator(); it.hasNext();) {
                String id = (String) it.next();
                confActions.add(createAction(id));
            }
        }

        return confActions;
    }

    /**
     * get the configured optimization level.
     * @see #CONF_OPTIMIZE_LEVEL
     * @return the current optimization level.
     */
    public int getOptimizationLevel() {
        String optimizeLevel = joptimizer.getJopConfig().getOption(CONF_OPTIMIZE_LEVEL, "1");
        int level = 0;
        try {
            level = Integer.valueOf(optimizeLevel).intValue();
        } catch (NumberFormatException e) {
            logger.error("Invalid optimization level {"+optimizeLevel+
                    "}, setting level to 0.");
        }
        return level;
    }


    private ActionCreator getActionCreator(String action) {
        return (ActionCreator) actionIds.get(action);
    }

    private void initActions() {
        actions = new LinkedHashMap();
        actionIds = new HashMap();

        Collection creators = configurator.getActionCreators();
        for (Iterator it = creators.iterator(); it.hasNext();) {
            ActionCreator creator = (ActionCreator) it.next();
            actions.put(creator.getActionName(), creator);

            Set ids = configurator.getActionIds(creator.getActionName());
            for (Iterator it2 = ids.iterator(); it2.hasNext();) {
                String id = (String) it2.next();
                actionIds.put(id, creator);
            }
        }
    }
}
