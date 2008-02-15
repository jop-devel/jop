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

import joptimizer.actions.CallTreePrinter;
import joptimizer.actions.ClassInfoLoader;
import joptimizer.actions.ClassInfoPrinter;
import joptimizer.actions.ClassWriter;
import joptimizer.actions.GraphHelper;
import joptimizer.actions.GraphPrinter;
import joptimizer.actions.MethodTransformAction;
import joptimizer.actions.TransitiveHullGenerator;
import joptimizer.config.BoolOption;
import joptimizer.config.ConfigurationException;
import joptimizer.config.IntOption;
import joptimizer.config.JopConfig;
import joptimizer.framework.actions.Action;
import joptimizer.framework.actions.GraphAction;
import joptimizer.optimizer.CodeStripper;
import joptimizer.optimizer.InlineOptimizer;
import joptimizer.optimizer.PeepholeOptimizer;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
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
     * small helper class to make action creation and setup more generic..
     */
    private abstract class ActionCreator {

        /**
         * do not display this action in usage msg.
         */
        public static final int SHOW_NONE = 0;

        /**
         * show action in usage message and show action-flag.
         */
        public static final int SHOW_ACTION = 1;

        /**
         * show action in usage message but show options only.
         */
        public static final int SHOW_OPTIONS = 2;

        private int showArgs;
        private String prefix;
        private Action defAction;

        public ActionCreator(int showArgs, String prefix) {
            this.showArgs = showArgs;
            this.prefix = prefix;
        }

        public abstract Action createAction();

        public void createArguments(List options) {
            getDefaultAction().appendActionArguments(prefix, options);
        }

        public String getPrefix() {
            return prefix;
        }

        public String getActionDescription() {
            return getDefaultAction().getActionDescription();
        }

        public int doShowArguments() {
            return showArgs;
        }

        public Action getDefaultAction() {
            if ( defAction == null ) {
                defAction = createAction();
            }
            return defAction;
        }
    }

    private JOPtimizer joptimizer;

    /**
     * a map of actioncreators for actions stored by action-name.
     * The same action may be stored more than once in this map with different names
     * which allows to create actions with different settings.
     */
    private Map actions;

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

    private static final Logger logger = Logger.getLogger(ActionFactory.class);

    public ActionFactory(JOPtimizer joptimizer) {
        this.joptimizer = joptimizer;
        initActions();
    }

    public JOPtimizer getJoptimizer() {
        return joptimizer;
    }

    public JopConfig getConfig() {
        return joptimizer.getJopConfig();
    }

    /**
     * get a map of all known actions with descriptions.
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
     * create a list for all known options for (almost) all actions.
     * @return a list with all known options concerning actions.
     */
    public List createActionArguments() {

        List options = new LinkedList();

        // NOTICE create IntOption, allow '-O2' without space?
        options.add(new IntOption(CONF_OPTIMIZE_LEVEL,
                "Set optimization level as number from 0 to 3, default is 1.", "level"));

        for (Iterator it = actions.entrySet().iterator(); it.hasNext();) {
            Map.Entry action = (Map.Entry) it.next();
            ActionCreator creator = (ActionCreator) action.getValue();
            String key = action.getKey().toString();

            if ( creator.doShowArguments() == ActionCreator.SHOW_ACTION ) {
                options.add(new BoolOption(key, creator.getActionDescription()));
            }
            if ( creator.doShowArguments() == ActionCreator.SHOW_OPTIONS ||
                 creator.doShowArguments() == ActionCreator.SHOW_ACTION ) {
                creator.createArguments(options);
            }
        }

        return options;
    }

    /**
     * Get a list of all options for an action.
     * @param action the name of the action.
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
     * create a new action by name.
     * The created action is not configured, so use {@link Action#configure(String, joptimizer.config.JopConfig)}
     * before executing the action.
     *
     * @param action the name of the action.
     * @return a new, unconfigured action or null if not found.
     */
    public Action createAction(String action) {
        ActionFactory.ActionCreator actionCreator = getActionCreator(action);
        return actionCreator != null ? actionCreator.createAction() : null;
    }

    /**
     * get the configuration prefix for this action name.
     * @param actionName the name of the action.
     * @return the prefix or null if action is not found. 
     */
    public String getActionPrefix(String actionName) {
        ActionCreator ac = (ActionCreator) actions.get(actionName);
        return ac == null ? null : ac.getPrefix();
    }

    /**
     * configure an action with the current configuration and its prefix.
     * @param action the action to configure.
     * @throws ConfigurationException if a configuration error occurs or the actionname is not known.
     */
    public void configureAction(Action action) throws ConfigurationException {

        String prefix = getActionPrefix(action.getActionName());
        if ( prefix == null ) {
            throw new ConfigurationException("Could not find action {"+action.getActionName()+"} in configured actions.");
        }

        action.configure(prefix, joptimizer.getJopConfig());
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

    /**
     * create a list of all configured actions which should be executed in the correct order.
     * This does not configure the actions, therefore {@link Action#configure(String, joptimizer.config.JopConfig)} must
     * be called before execution.
     *
     * @return a list of Actions to be executed.
     */
    public List createConfiguredActions() {

        // check if any action modifies the classes, if so, add classwriter
        boolean modified = false;

        int optimizeLevel = getOptimizationLevel();
        Set optimizeActions = getOptimizerActions(optimizeLevel);

        List confActions = new ArrayList();

        MethodTransformAction mta = null;

        // check all actions (in the order defined in initActions)
        for (Iterator it = actions.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String actionName = entry.getKey().toString();
            ActionCreator creator = (ActionCreator) entry.getValue();

            // check if action is either requested by arguments or by optimization-level
            if ( (creator.doShowArguments() == ActionCreator.SHOW_ACTION &&
                    joptimizer.getJopConfig().isEnabled(actionName)) ||
                  optimizeActions.contains(actionName) )
            {
                
                Action action = createAction(actionName);
                modified |= action.doModifyClasses();

                // some extrawürscht for graph actions, to transform code in a single step
                // and therefore there is no need to keep all CFGs in memory of all methods.
                if ( action instanceof GraphAction && false ) {

                    // on first graph action, create container action
                    // NOTICE this does not keep the correct order for non-graph actions
                    if ( mta == null ) {
                        mta = (MethodTransformAction) createAction(MethodTransformAction.ACTION_NAME);
                        confActions.add(mta);
                    }

                    GraphAction graphAction = (GraphAction) action;
                    mta.addAction(graphAction.getDefaultStage(), creator.getPrefix(), graphAction);

                } else {
                    confActions.add(action);
                }

            }
        }

        if ( modified ) {
            confActions.add(createAction(ClassWriter.ACTION_NAME));
        }

        return confActions;
    }

    /**
     * Initialize all actions. <br>
     * <br>
     * TODO load actions from configuration xml file (get from resource).
     */
    private void initActions() {

        // create sorted map here to show actions in the correct order in the usage message.
        actions = new LinkedHashMap();

        actions.put(TransitiveHullGenerator.ACTION_NAME, new ActionCreator(ActionCreator.SHOW_NONE, "th") {
            public Action createAction() {
                return new TransitiveHullGenerator(TransitiveHullGenerator.ACTION_NAME, joptimizer);
            }
        });

        actions.put(ClassInfoLoader.ACTION_NAME, new ActionCreator(ActionCreator.SHOW_NONE, "cil") {
            public Action createAction() {
                return new ClassInfoLoader(ClassInfoLoader.ACTION_NAME, joptimizer);
            }
        });

        actions.put(CallTreePrinter.ACTION_NAME, new ActionCreator(ActionCreator.SHOW_ACTION, "ct") {
            public Action createAction() {
                return new CallTreePrinter(CallTreePrinter.ACTION_NAME, joptimizer);
            }
        });

        actions.put(ClassInfoPrinter.ACTION_NAME, new ActionCreator(ActionCreator.SHOW_NONE, "cip") {
            public Action createAction() {
                return new ClassInfoPrinter(ClassInfoPrinter.ACTION_NAME, joptimizer);
            }
        });

        actions.put(GraphPrinter.ACTION_NAME, new ActionCreator(ActionCreator.SHOW_NONE, "cfp") {
            public Action createAction() {
                return new GraphPrinter(GraphPrinter.ACTION_NAME, joptimizer);
            }
        });

        actions.put(GraphHelper.ACTION_NAME, new ActionCreator(ActionCreator.SHOW_NONE, "gh") {
            public Action createAction() {
                return new GraphHelper(GraphHelper.ACTION_NAME, joptimizer);
            }
        });

        actions.put(MethodTransformAction.ACTION_NAME, new ActionCreator(ActionCreator.SHOW_OPTIONS, "mta") {
            public Action createAction() {
                return new MethodTransformAction(MethodTransformAction.ACTION_NAME, joptimizer);
            }
        });

        actions.put(InlineOptimizer.ACTION_NAME, new ActionCreator(ActionCreator.SHOW_OPTIONS, "inline") {
            public Action createAction() {
                return new InlineOptimizer(InlineOptimizer.ACTION_NAME, joptimizer);
            }
        });

        actions.put(PeepholeOptimizer.ACTION_NAME, new ActionCreator(ActionCreator.SHOW_OPTIONS, "peep") {
            public Action createAction() {
                return new PeepholeOptimizer(PeepholeOptimizer.ACTION_NAME, joptimizer);
            }
        });

        actions.put(CodeStripper.ACTION_NAME, new ActionCreator(ActionCreator.SHOW_ACTION, "cs") {
            public Action createAction() {
                return new CodeStripper(CodeStripper.ACTION_NAME, joptimizer);
            }
        });

        actions.put(ClassWriter.ACTION_NAME, new ActionCreator(ActionCreator.SHOW_OPTIONS, "cw") {
            public Action createAction() {
                return new ClassWriter(ClassWriter.ACTION_NAME, joptimizer);
            }
        });
    }

    private ActionCreator getActionCreator(String action) {
        return (ActionCreator) actions.get(action);
    }

    /**
     * Get a list of all optimizer action names for an optimization level. <br>
     * <br>
     * NOTICE get from configuration xml file or something.
     *
     * @param level the level or zero for no optimization at all.
     * @return a set of actionnames which should be executed.
     */
    private Set getOptimizerActions(int level) {

        Set actionNames = new HashSet();

        if ( level >= 1 ) {
            actionNames.add(PeepholeOptimizer.ACTION_NAME);
        }
        if ( level >= 2 ) {
            actionNames.add(InlineOptimizer.ACTION_NAME);
        }
        if ( level >= 3 ) {
            actionNames.add(CodeStripper.ACTION_NAME);
        }

        return actionNames;
    }

}
