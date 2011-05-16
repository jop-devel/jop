/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006, Rasmus Ulslev Pedersen
  Copyright (C) 2006-2008, Martin Schoeberl (martin@jopdesign.com)
  Copyright (C) 2008-2010, Benedikt Huber (benedikt.huber@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.jopdesign.wcet.annotations;

import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.code.LoopBound;
import com.jopdesign.wcet.WCETTool;
import org.apache.bcel.util.ClassPath.ClassFile;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsing source annotations for WCET analysis.
 * <p/>
 * We currently support the following source code annotations:
 * <ul>
 * <li/>{@code "loop" ("<=" | "=") execution-frequency-expression}
 * This annotations asserts that the loop associated with the source code line
 * is executed at most or exactly as often as {@code execution-frequency-expression}.
 * </ul>
 * <h2>Execution Frequency Expressions</h2>
 * An execution frequency expression is currently of the form
 * {@code integer-expression [context]}.
 * <ul>
 * <li/>{@code integer-expression} is a plain integer.
 * <li/>{@code context is one of}
 * <ul>
 * <li/> No context: The default context is the execution frequency of the entry edges of the loop
 * itself({@code outer(0)})
 * <li/> {@code "outer" ["(" i ")"]}: The execution frequency of the entry edges of the i-th predecessor
 * in the loop-nest-tree. {@code i} defaults to 1 (the outer loop) if left out.
 * <li/> {@code "method" ["("name")"]}: The execution frequency of the entry edges of the method with the
 * given name. {@code name} defaults to the method containing the loop if no name is given.<br/>
 * TODO: Currently, we require that the method dominates the method containing
 * the loop. What we really want is: The constraint only applies to those call contexts, where
 * the method is in the callstring.
 * </ul>
 * </ul>
 * <p/>
 * TODO: Many more features :) Would be a nice internship.
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 */
public class SourceAnnotationReader {
    private static final Logger logger = Logger.getLogger(WCETTool.LOG_WCET_ANNOTATIONS+".SourceAnnotationReader");

    private WCETTool project;

    public SourceAnnotationReader(WCETTool p) {
        this.project = p;
    }

    /**
     * Extract loop bound annotations for one class
     * <p/>
     * All annotations start with {@code // @WCA }, and belong to the last source code line encountered.
     * A source code line is a line which has at least one token in it.
     *
     * @return a FlowFacts object encapsulating the annotations found in the source file of the given class
     * @throws IOException
     * @throws BadAnnotationException
     */
    public SourceAnnotations readAnnotations(ClassInfo ci)
            throws IOException, BadAnnotationException {

        SourceAnnotations flowFacts = new SourceAnnotations();
        File fileName = getSourceFile(ci);
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = null;
        int lineNr = 1;
        int sourceLineNr = 1;
        while ((line = reader.readLine()) != null) {
            if (!SourceAnnotationReader.isCommentLine(line)) {
                sourceLineNr = lineNr;
            }
            LoopBound loopBound = SourceAnnotationReader.extractAnnotation(line);
            if (loopBound != null) {
                logger.debug("Adding loop bound @ " + sourceLineNr + ": " + loopBound);
                flowFacts.addLoopBound(sourceLineNr, loopBound);
            }
            lineNr++;
        }
        logger.debug("Read WCA annotations for " + fileName);
        return flowFacts;
    }

    private static boolean isCommentLine(String line) {
        Pattern pattern = Pattern.compile("^\\s*(//.*)?$");
        Matcher matcher = pattern.matcher(line);
        return matcher.matches();
    }

    private File getSourceFile(ClassInfo ci) throws FileNotFoundException, BadAnnotationException {

        File src = project.getSourceFile(ci);
        try {
            ClassFile klazz = project.getAppInfo().getClassFile(ci);
            if (src.lastModified() > klazz.getTime()) {
                throw new BadAnnotationException("Timestamp error: source file modified after compilation");
            }
        } catch (IOException ex) {
            WCETTool.logger.warn("Could not find class file for " + ci, ex);
        }
        return src;
    }

    /**
     * Return the loop bound for a java source lines, if any.
     * <p>
     * Loop bounds annotations are of the form
     * <br/>
     * {@code // @WCA "loop" ("<=" | "=") execution-frequency-expression}
     * <br/>
     * Examples:
     * <ul><li/>{@code // @WCA loop <= 10}
     * <li/>{@code // @WCA loop = 45 outer}
     * <li/>{@code // @WCA loop <= 135 outer(2)}
     * <li/>{@code // @WCA loop <= 135 method}
     * <li/>{@code // @WCA loop =  423 method("main")}
     * </ul>
     * For details see the documentation of {@link SourceAnnotationReader}.
     * <br/>
     * NOTE: For backward compatibility reasons, we also support the notation:<br/>
     * {@code lower-bound "<=" "loop" "<=" upper-bound}<br/>
     * It is currently open if there will be a replacement for this rarely used feature.
     * </p>
     *
     * @param sourceLine a java source code line (possibly annotated)
     * @return the loop bound or null if no annotation was found
     * @throws BadAnnotationException if the loop bound annotation has syntax errors or is invalid
     */
    public static LoopBound extractAnnotation(String sourceLine)
            throws BadAnnotationException {

        int ai = sourceLine.indexOf("@WCA");
        if (ai != -1) {

            String annotString = sourceLine.substring(ai + "@WCA".length());
            if (annotString.indexOf("loop") < 0) return null;
            // Backward compatibility
            Pattern pattern2 = Pattern.compile(" *([0-9]+) *<= *loop *<= *([0-9]+) *");
            Matcher matcher2 = pattern2.matcher(annotString);
            if (matcher2.matches()) {
                logger.warn("Deprecated loop bound notation: X <= loop <= Y");
                LoopBoundExpr expr =
                	LoopBoundExpr.numericBound(matcher2.group(1),matcher2.group(2));
                return LoopBound.simpleBound(expr);
            }
            // New loop bound
            InputStream is = new ByteArrayInputStream(annotString.getBytes());
            Parser parser = new Parser(new Scanner(is));
            
            parser.Parse();
            if (parser.errors.count > 0) {
                throw new BadAnnotationException("Parse Error in Annotation: " + annotString);
            }
            return parser.getResult();
        }
        return null;
    }

}
