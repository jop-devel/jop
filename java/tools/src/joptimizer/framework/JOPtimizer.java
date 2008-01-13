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

import joptimizer.actions.ClassInfoLoader;
import joptimizer.actions.TransitiveHullGenerator;
import joptimizer.config.ConfigurationException;
import joptimizer.config.JopConfig;
import joptimizer.framework.actions.Action;
import joptimizer.framework.actions.ActionException;
import joptimizer.framework.actions.ClassAction;
import joptimizer.framework.actions.MethodAction;
import com.jopdesign.libgraph.struct.*;
import com.jopdesign.libgraph.struct.bcel.BcelClassInfo;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * This is the main class for the optimizer.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class JOPtimizer implements AppClassLoader {

    // Just some helper classes to ease logging..
    private interface  ActionRunner {
        public Action getAction();
        public void execute() throws ActionException;
    }

    private class SimpleActionRunner implements ActionRunner {

        private Action action;

        protected SimpleActionRunner(Action action) {
            this.action = action;
        }

        public Action getAction() {
            return action;
        }

        public void execute() throws ActionException {
            action.execute();
        }
    }

    private class ClassActionRunner implements ActionRunner {

        private ClassAction action;
        private ClassInfo classInfo;

        protected ClassActionRunner(ClassAction action, ClassInfo classInfo) {
            this.action = action;
            this.classInfo = classInfo;
        }

        public Action getAction() {
            return action;
        }

        public void execute() throws ActionException {
            action.execute(classInfo);
        }
    }

    private class MethodActionRunner implements ActionRunner {

        private MethodAction action;
        private MethodInfo methodInfo;

        protected MethodActionRunner(MethodAction action, MethodInfo methodInfo) {
            this.action = action;
            this.methodInfo = methodInfo;
        }

        public Action getAction() {
            return action;
        }

        public void execute() throws ActionException {
            action.execute(methodInfo);
        }
    }

    private JopConfig jopConfig;
    private ActionFactory actionFactory;

    private AppStruct appStruct;

    /**
     * logger used for, well, logging.
     */
    static Logger logger = Logger.getLogger(JOPtimizer.class);

    public JOPtimizer(JopConfig config) {
        jopConfig = config;
        actionFactory = new ActionFactory(this);
        appStruct = new AppStruct(this, jopConfig);
    }


    public JopConfig getJopConfig() {
        return jopConfig;
    }

    public ActionFactory getActionFactory() {
        return actionFactory;
    }

    public AppStruct getAppStruct() {
        return appStruct;
    }

    /**
     * Load all linked classes, starting with a root set.
     * This clears the currently loaded classes.
     *
     * @param rootClasses a set of root class names to load.
     * @throws IOException if a class could not be read.
     */
    public void loadTransitiveHull(Set rootClasses) throws IOException, ActionException {

        Collection root = createJavaClasses(rootClasses);

        appStruct.clear();
        addClasses(root);

        Action loader = actionFactory.createAction(TransitiveHullGenerator.ACTION_NAME);
        executeAction(loader);

    }

    public ClassInfo createClassInfo(String className) throws TypeException, IOException {

        JavaClass jc = createJavaClass(className);
        if ( jc == null ) {
            return null;
        }

        return new BcelClassInfo(appStruct, jc);
    }

    /**
     * Add a set of javaclasses to the appStruct container.
     * @param jc a collection of BCEL JavaClasses to add.
     */
    public void addClasses(Collection jc) {

        // check if any 'optimistic' optimizations are enabled.
        boolean doReflectTest = !jopConfig.doAssumeReflection() && actionFactory.getOptimizationLevel() > 1;

        for (Iterator it = jc.iterator(); it.hasNext();) {
            JavaClass jclass = (JavaClass) it.next();

            String reason = jopConfig.doExcludeClassName(jclass.getClassName());
            if ( reason == null ) {

                if ( doReflectTest && jclass.getClassName().startsWith("java.lang.reflect.") ) {
                    logger.warn("Found reflection class {"+jclass.getClassName()+"}, some optimizations "+
                            "may produce invalid code.");
                }

                ClassInfo info = new BcelClassInfo(appStruct, jclass);
                appStruct.addClass(info);
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info(reason);
                }
            }
        }
    }

    /**
     * load the class infos of all known classes.
     */
    public void loadClassInfos() throws ActionException {

        Action loader = actionFactory.createAction(ClassInfoLoader.ACTION_NAME);
        executeAction(loader);

    }

    /**
     * execute all actions set in jopConfig.
     */
    public void executeActions() throws ActionException {

        List actions = actionFactory.createConfiguredActions();
        for (int i = 0; i < actions.size(); i++) {
            executeAction( (Action) actions.get(i) );
        }

    }

    /**
     * create Bcel JavaClasses from a list of classnames using the configured classpath.
     *
     * @param classNames a set of FQ-classnames to load.
     * @return a collection of Bcel-JavaClasses containing the given classes.
     * @throws IOException if reading a class fails.
     */
    public Collection createJavaClasses(Set classNames) throws IOException {
        List jc = new LinkedList();

        Iterator i = classNames.iterator();
        for (int nr=0; i.hasNext(); ++nr) {
            String clname = (String) i.next();
            jc.add( createJavaClass(clname) );
        }

        return jc;
    }

    /**
     * create a Bcel JavaClass from a classname using the configured classpath.
     *
     * @param className the name of the class to load.
     * @return the bcel class.
     * @throws IOException if reading the class fails.
     */
    public JavaClass createJavaClass(String className) throws IOException {
        InputStream is = jopConfig.getClassPath().getInputStream(className);
        return new ClassParser(is, className).parse();
    }

    /**
     * run a given action on the current appstruct.
     *
     * NOTICE method variant to run action only on a subset of classes.
     * 
     * @param action the action to execute.
     */
    public void executeAction(Action action) throws ActionException {

        if ( logger.isInfoEnabled() ) {
            logger.info("Starting action {"+action.getActionName()+"}.");
        }

        executeAction(new SimpleActionRunner(action));

        if ( logger.isInfoEnabled() ) {
            logger.info("Finished action {"+action.getActionName()+"}.");
        }
    }

    /**
     * Run an action on a single class.
     * @param action the action to run
     * @param classInfo the class on which the action should be performed.
     */
    public void executeAction(Action action, ClassInfo classInfo) throws ActionException {

        if ( ! (action instanceof ClassAction) ) {
            logger.error("Could not run action {"+action.getActionName()+"}: not a class action.");
            return;
        }

        if ( logger.isInfoEnabled() ) {
            logger.info("Starting action {"+action.getActionName()+"} on class {"+
                    classInfo.getClassName()+"}.");
        }

        executeAction(new ClassActionRunner((ClassAction) action, classInfo));

        if ( logger.isInfoEnabled() ) {
            logger.info("Finished action {"+action.getActionName()+"}.");
        }
    }

    public void executeAction(Action action, MethodInfo methodInfo) throws ActionException {

        if ( ! (action instanceof MethodAction) ) {
            logger.error("Could not run action {"+action.getActionName()+"}: not a method action.");
            return;
        }

        if ( logger.isInfoEnabled() ) {
            logger.info("Starting action {"+action.getActionName()+"} on method {"+methodInfo.getFQMethodName()+"}.");
        }

        executeAction(new MethodActionRunner((MethodAction) action, methodInfo));

        if ( logger.isInfoEnabled() ) {
            logger.info("Finished action {"+action.getActionName()+"}.");
        }
    }

    private void executeAction(ActionRunner actionRunner) throws ActionException {

        Action action = actionRunner.getAction();

        try {
            actionFactory.configureAction(action);

            action.startAction();
            actionRunner.execute();
            action.finishAction();

        } catch (ConfigurationException e) {
            logger.error("Configuration error in action {"+action.getActionName()+"}: " + e.getMessage());
            if (logger.isInfoEnabled()) {
                logger.info("Exception in action", e);
            }
            if ( !jopConfig.doIgnoreActionErrors() ) {
                throw new ActionException(e);
            }
        } catch (ActionException e) {
            logger.error("Error executing action {"+action.getActionName()+"}: " + e.getMessage());
            if ( logger.isInfoEnabled() ) {
                logger.info("Exception in action", e);
            }
            if ( !jopConfig.doIgnoreActionErrors() ) {
                throw e;
            }
        }

    }

}
