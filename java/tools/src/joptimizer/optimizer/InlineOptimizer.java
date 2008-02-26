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
package joptimizer.optimizer;

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.struct.MethodInfo;
import joptimizer.config.BoolOption;
import joptimizer.config.IntOption;
import joptimizer.config.JopConfig;
import joptimizer.config.StringOption;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.AbstractGraphAction;
import joptimizer.framework.actions.ActionException;
import joptimizer.optimizer.inline.CodeInliner;
import joptimizer.optimizer.inline.InlineChecker;
import joptimizer.optimizer.inline.InlineHelper;
import joptimizer.optimizer.inline.InlineStrategy;
import joptimizer.optimizer.inline.LocalInlineStrategy;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * An optimizer which inlines as many function calls as possible
 *
 * Option '..':
 * check for more 'unsafe' inlining criterias (only valid if complete transitive
 * hull is known, no dynamic classloading is performed. As the JOPizer may remove
 * unused functions, reflections will be even more unsafe.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class InlineOptimizer extends AbstractGraphAction {

    public static final String ACTION_NAME = "inline";

    public static final String CONF_INLINE_IGNORE = "ignore";

    public static final String CONF_INLINE_CHECK = "checkcode";

    public static final String CONF_CHANGE_ACCESS = "changeaccess";

    public static final String CONF_MAX_INLINE_SIZE = "maxsize";

    private static final Logger logger = Logger.getLogger(InlineOptimizer.class);

    private InlineStrategy strategy;

    public InlineOptimizer(String name, String id, JOPtimizer joptimizer) {
        super(name, id, joptimizer);
    }

    public void appendActionArguments(List options) {
        options.add(new StringOption(getActionId(), CONF_INLINE_IGNORE,
                "Do not inline code from the given package or class prefix. Give classes as comma-separated list.",
                "packages"));
        options.add(new BoolOption(getActionId(), CONF_INLINE_CHECK,
                "Insert check code before inlined code to ensure correct devirtualization (NYI). " +
                "Defaults to true if dynamic class loading is assumed to be disabled."));
        options.add(new BoolOption(getActionId(), CONF_CHANGE_ACCESS,
                "Allow changing of access modifiers to public access to enable inlining. " +
                "Should be used with care if dynamic class loading is used."));
        options.add(new IntOption(getActionId(), CONF_MAX_INLINE_SIZE,
                "Maximum size of methods to inline in bytes, 0 for unlimited.", "size"));

        // TODO bit of a hack here, find a nicer solution to get options from (all) strategies
        new LocalInlineStrategy().appendActionArguments(getActionId(), options);
    }


    public String getActionDescription() {
        return "Inline and devirtualize method calls.";
    }

    public boolean doModifyClasses() {
        return true;
    }

    public boolean configure(JopConfig config) {

        // NOTICE make strategy selectable by option
        strategy = new LocalInlineStrategy();
        if ( !strategy.configure(getActionName(), getActionId(), config) ) {
            return false;
        }

        InlineChecker checker = new InlineChecker(getJoptimizer().getAppStruct(), config.getArchConfig());
        CodeInliner inliner = new CodeInliner(getJoptimizer().getAppStruct());
        InlineHelper helper = new InlineHelper(checker, inliner, strategy.getInvokeResolver());

        strategy.setup(helper, getJoptimizer().getAppStruct(), getJopConfig());

        // configure checker and inliner
        String ignorepkg = getActionOption(config, CONF_INLINE_IGNORE);
        String nativeClass = config.getArchConfig().getNativeClassName();

        if ( nativeClass != null && !nativeClass.isEmpty() ) {
            if ( ignorepkg != null && !ignorepkg.isEmpty() ) {
                ignorepkg += "," + nativeClass;
            } else {
                ignorepkg = nativeClass;
            }
        }
        if ( ignorepkg != null && !ignorepkg.isEmpty() ) {
            checker.setIgnorePrefix(ignorepkg.split(","));
        }

        String maxSize = getActionOption(config, CONF_MAX_INLINE_SIZE, "0");
        try {
            checker.setMaxInlineSize( Integer.parseInt(maxSize) );
        } catch (NumberFormatException e) {
            logger.warn("Invalid "+CONF_MAX_INLINE_SIZE+" {"+maxSize+"}, ignored.");
        }

        boolean checkCode = isActionEnabled(config, CONF_INLINE_CHECK);
        
        inliner.setInsertCheckCode(checkCode);
        checker.setUseCheckCode(checkCode);
        checker.setAssumeDynamicLoading(config.doAssumeDynamicLoading());
        checker.setChangeAccess(isActionEnabled(config, CONF_CHANGE_ACCESS));
        
        return true;
    }

    public void startAction() throws ActionException {
        strategy.initialize();
    }

    public void finishAction() throws ActionException {
        if (logger.isInfoEnabled()) {
            logger.info("Inlined {" + strategy.getInlineCount() + "} methods.");
        }
    }

    public int getGraphStage() {
        return STAGE_STACK_TO_QUAD;
    }

    public int getRequiredForm() {
        return 0;
    }

    public void execute() throws ActionException {
        strategy.execute();
    }

    public void execute(MethodInfo methodInfo, ControlFlowGraph graph) throws ActionException {
        strategy.execute(methodInfo, graph);
    }

}
