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

import java.util.List;

/**
 * This is a class which is used to create and configure new actions.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class ActionCreator {

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
    private String actionName;
    private Action defAction;

    public ActionCreator(int showArgs, String actionName) {
        this.showArgs = showArgs;
        this.actionName = actionName;
    }

    /**
     * Create a new, unconfigured instance of this action.
     * @param id the id of the action.
     * @return a new action.
     */
    public abstract Action createAction(String id);

    public void createArguments(List options) {
        getDefaultAction().appendActionArguments(options);
    }

    public void createArguments(String id, List options) {
        createAction(id).appendActionArguments(options);
    }

    public String getActionName() {
        return actionName;
    }

    public String getActionDescription() {
        return getDefaultAction().getActionDescription();
    }

    public int doShowArguments() {
        return showArgs;
    }

    public Action getDefaultAction() {
        if ( defAction == null ) {
            defAction = createAction(actionName);
        }
        return defAction;
    }

}
