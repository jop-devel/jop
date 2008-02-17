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

import joptimizer.config.JopConfig;
import joptimizer.framework.JOPtimizer;

/**
 * A simple abstract implementation of an Action.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class AbstractAction implements Action {

    private String name;
    private String id;
    private JOPtimizer joptimizer;

    public AbstractAction(String name, String id, JOPtimizer joptimizer) {
        this.name = name;
        this.id = id;
        this.joptimizer = joptimizer;
    }

    public String getActionName() {
        return name;
    }

    public String getActionId() {
        return id != null ? id : name;
    }

    public JOPtimizer getJoptimizer() {
        return joptimizer;
    }

    public JopConfig getJopConfig() {
        return joptimizer.getJopConfig();
    }

    /**
     * Does nothing.
     *
     * @throws ActionException
     *
     * @see Action#startAction()
     */
    public void startAction() throws ActionException {
    }

    /**
     * Does nothing.
     *
     * @throws ActionException
     *
     * @see Action#finishAction()
     */
    public void finishAction() throws ActionException {
    }

    protected String getActionOption(JopConfig config, String option) {
        return getActionOption(config, option, null);
    }

    protected String getActionOption(JopConfig config, String option, String defaultValue) {
        return config.getActionOption(getActionName(), getActionId(), option, defaultValue);
    }

    protected boolean isActionEnabled(JopConfig config, String option) {
        return config.isEnabled(getActionName(), getActionId(), option);
    }
}
