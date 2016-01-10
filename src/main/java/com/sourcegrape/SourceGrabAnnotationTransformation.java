package com.sourcegrape;

import groovy.transform.CompilationUnitAware;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.ASTTransformationVisitor;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * Transformation for declarative source dependency management.
 */
@GroovyASTTransformation(phase=CompilePhase.CONVERSION)
public class SourceGrabAnnotationTransformation  extends ClassCodeVisitorSupport implements ASTTransformation, CompilationUnitAware {
    private static final String GRAB_CLASS_NAME = SourceGrab.class.getName();
    private static final String GRAB_DOT_NAME = GRAB_CLASS_NAME.substring(GRAB_CLASS_NAME.lastIndexOf("."));
    private static final String GRAB_SHORT_NAME = GRAB_DOT_NAME.substring(1);
    
    private boolean allowShortGrab;
    private Set<String> grabAliases;
    private List<AnnotationNode> grabAnnotations;

    private CompilationUnit compilationUnit;
    private SourceUnit sourceUnit;
    
    /**
     * Adds the annotation to the internal target list if a match is found.
     *
     * @param node the AST node we are processing
     */
    @Override
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        for (AnnotationNode an : node.getAnnotations()) {
            String name = an.getClassNode().getName();
            if ((GRAB_CLASS_NAME.equals(name))
                    || (allowShortGrab && GRAB_SHORT_NAME.equals(name))
                    || (grabAliases.contains(name))) {
                grabAnnotations.add(an);
            }
        }
    }

    @Override
    public SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    //@Override
    public void setCompilationUnit(final CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    //@Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        
        sourceUnit = source;
        ModuleNode moduleNode = (ModuleNode) nodes[0];

        configureAnnotationAliasesFromImports(moduleNode);
        List<SourceGrabNode> grabNodes = extractGrabNodes();
        runGrab(grabNodes);
    }

    private List<SourceGrabNode> extractGrabNodes() {
        List<SourceGrabNode> grabNodes = new ArrayList<SourceGrabNode>();

        for (ClassNode classNode : sourceUnit.getAST().getClasses()) {
            grabAnnotations = new ArrayList<AnnotationNode>();
            List<SourceGrabNode> grabNodesInit = new ArrayList<SourceGrabNode>();
            visitClass(classNode);

            if (!grabAnnotations.isEmpty()) {
                for (AnnotationNode node : grabAnnotations) {
                    SourceGrabNode grabNode = new SourceGrabNode(node);
                    grabNodes.add(grabNode);
                    if (grabNode.isInitClass()) {
                        grabNodesInit.add(grabNode);
                    }
                }
                generateStaticGrabCalls(classNode, grabNodesInit);
            }
        }
        return grabNodes;
    }
    
    private void configureAnnotationAliasesFromImports(ModuleNode moduleNode) {
        allowShortGrab = true;
        grabAliases = new HashSet<String>();
        for (ImportNode im : moduleNode.getImports()) {
            String alias = im.getAlias();
            String className = im.getClassName();
            if ((className.endsWith(GRAB_DOT_NAME) && ((alias == null) || (alias.length() == 0)))
                || (GRAB_CLASS_NAME.equals(alias)))
            {
                allowShortGrab = false;
            } else if (GRAB_CLASS_NAME.equals(className)) {
                grabAliases.add(im.getAlias());
            }
        }
    }

    private void generateStaticGrabCalls(ClassNode classNode, List<SourceGrabNode> grabNodes) {
        List<Statement> grabInitializers = new ArrayList<Statement>();

        List<Expression> argList = new ArrayList<Expression>();
        if (grabNodes.size() == 0) return;
        for (SourceGrabNode grabNode : grabNodes) {
            ConstantExpression urlExpression = new ConstantExpression(grabNode.getUri());
            argList.add(urlExpression);
        }
        ArgumentListExpression grabArgs = new ArgumentListExpression(argList);
        ClassNode grapeClassNode = ClassHelper.make(SourceGrape.class);
        grabInitializers.add(new ExpressionStatement(new StaticMethodCallExpression(grapeClassNode, "grab", grabArgs)));

        // insert at beginning so we have the classloader set up before the class is called
        classNode.addStaticInitializerStatements(grabInitializers, true);
    }

    private void runGrab(List<SourceGrabNode> grabNodes) {

        if (!grabNodes.isEmpty()) {
            try {
                List<String> urls = new ArrayList<String>(grabNodes.size());
                for (SourceGrabNode grabNode : grabNodes) {
                    urls.add(grabNode.getUri());
                }
                SourceGrape.grab(sourceUnit.getClassLoader(), urls.toArray(new String[0]));
                
                // SourceGrab may have added more transformations through new URLs added to classpath, so do one more scan
                if (compilationUnit!=null) {
                    ASTTransformationVisitor.addGlobalTransformsAfterGrab(compilationUnit.getASTTransformationsContext());
                }
                
            } catch (RuntimeException re) {
                // Decided against syntax exception since this is not a syntax error.
                // The down side is we lose line number information for the offending
                // @SourceGrab annotation.
                sourceUnit.addException(re);
            }
        }
    }

    private static class SourceGrabNode {
        private AnnotationNode node;
        
        public SourceGrabNode(AnnotationNode node) {
            this.node = node;
        }

        public boolean isInitClass() {
            return ((node.getMember("initClass") == null) || (node.getMember("initClass") == ConstantExpression.TRUE));
        }
        
        public String getUri() {
            String uri = getMemberStringValue("uri");
            if (uri == null) {
                uri = getMemberStringValue("value");
            }
            return uri;
        }

        private String getMemberStringValue(String name) {
            return getMemberStringValue(name, null);
        }
        
        private String getMemberStringValue(String name, String defaultValue) {
            final Expression member = node.getMember(name);
            if (member != null && member instanceof ConstantExpression) {
                Object result = ((ConstantExpression) member).getValue();
                if (result != null && result instanceof String && isUndefined((String) result)) result = null;
                if (result != null) return result.toString();
            }
            return defaultValue;
        }

        private static final String STRING = "<DummyUndefinedMarkerString-DoNotUse>";
        private boolean isUndefined(String other) { return STRING.equals(other); }
    }
}
