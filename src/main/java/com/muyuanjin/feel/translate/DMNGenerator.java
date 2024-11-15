package com.muyuanjin.feel.translate;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.muyuanjin.feel.dmn.*;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.type.FAny;
import com.muyuanjin.feel.lang.type.FContext;
import com.muyuanjin.feel.util.FeelUtil;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;

import java.util.*;

/**
 * @author muyuanjin
 */
public class DMNGenerator {
    private static final Context.Key<DMNGenerator> dmnGeneratorKey = new Context.Key<>();
    private final CompilerTask task;
    private final ClassManager manager;
    private final Context context;
    private final Type inputType;

    private DMNGenerator(Context context) {
        context.put(dmnGeneratorKey, this);
        this.manager = ClassManager.instance(context);
        this.task = CompilerTask.instance(context);
        this.context = context;
        this.inputType = manager.getType(this.task.rootInputJavaType());
    }

    public static DMNGenerator instance(Context context) {
        DMNGenerator instance = context.get(dmnGeneratorKey);
        if (instance == null) {
            instance = new DMNGenerator(context);
        }
        return instance;
    }

    /**
     * 生成决策表源码
     */
    public String generate(DecisionTableDefinition definition) {
        var classDef = manager.getClassDeclaration();
        classDef.addAnnotation(new SingleMemberAnnotationExpr(new Name("SuppressWarnings"), new StringLiteralExpr("SpellCheckingInspection")));
        classDef.addImplementedType(manager.getClassType(DecisionTable.class)
                .setTypeArguments(manager.getClassType(task.rootInputJavaType())));
        appendEvaluate(definition);
        appendGetDefinition(definition);
        appendGetSource();
        return manager.generate();
    }

    /**
     * 实现 获取决策表定义 方法
     */
    private void appendGetDefinition(DecisionTableDefinition definition) {
        // 实现 com.muyuanjin.feel.dmn.DecisionTable#getDefinition 方法
        ClassOrInterfaceType classType = manager.getClassType(DecisionTableDefinition.class);
        ObjectCreationExpr creationExpr = new ObjectCreationExpr()
                .setType(classType)
                .addArgument(definition.name() == null ? new NullLiteralExpr() : CodeGens.stringLiteral(definition.name()))
                .addArgument(getInputsCreator(definition.inputs()))
                .addArgument(getOutputsCreator(definition.outputs()))
                .addArgument(getStringsCreator(definition.annotations()))
                .addArgument(getDecisionRulesCreator(definition.rules()))
                .addArgument(getEnumCreator(definition.hitPolicy()))
                .addArgument(getEnumCreator(definition.aggregation()));

        String defFieldName = "DEFINITION";
        manager.getConstants().put(defFieldName, CodeGens.staticField(classType, defFieldName, creationExpr));
        MethodDeclaration getDefinition = new MethodDeclaration(NodeList.nodeList(Modifier.publicModifier()), classType, "getDefinition");
        getDefinition.setBody(new BlockStmt().addStatement(new ReturnStmt(new NameExpr(defFieldName))));
        getDefinition.addAnnotation(new MarkerAnnotationExpr("Override"));
        manager.getMethods().add(0, getDefinition);
    }

    /**
     * 实现 评估决策表 方法
     */
    private void appendEvaluate(DecisionTableDefinition definition) {
        ClassOrInterfaceType evalResultClass = manager.getClassType(EvalResult.class);
        MethodDeclaration evaluate = new MethodDeclaration(NodeList.nodeList(Modifier.publicModifier()), evalResultClass, "evaluate");
        NameExpr rootInput = new NameExpr(task.rootInputParam());
        evaluate.addParameter(new Parameter(manager.getClassType(task.rootInputJavaType()), rootInput.getName()));
        evaluate.addAnnotation(new MarkerAnnotationExpr("Override"));
        BlockStmt evalBody = new BlockStmt();
        evaluate.setBody(evalBody);
        processingRules(definition, evalBody, rootInput);
        manager.getMethods().add(0, evaluate);
    }

    /**
     * 实现返回源码方法
     */
    private void appendGetSource() {
        MethodDeclaration getSource = new MethodDeclaration(NodeList.nodeList(Modifier.publicModifier()), manager.getClassType(String.class), "getSource");
        manager.getClassDeclaration().getMembers().add(getSource);
        getSource.addAnnotation(new MarkerAnnotationExpr("Override"));
        BlockStmt blockStmt = new BlockStmt();
        getSource.setBody(blockStmt);
        blockStmt.addStatement(new ReturnStmt(new NullLiteralExpr()));
        ReturnStmt returnStmt = new ReturnStmt(CodeGens.stringLiteral(manager.generate()));
        blockStmt.setStatements(NodeList.nodeList(returnStmt));
    }

    private void processingRules(DecisionTableDefinition definition, BlockStmt body, NameExpr rootInput) {
        List<DecisionRule> rules = definition.rules();
        if (rules == null || rules.isEmpty()) {
            return;
        }
        List<InputMethod> inputMethods = new ArrayList<>(definition.inputs().size());
        for (int i = 0; i < definition.inputs().size(); i++) {
            inputMethods.add(new InputMethod(definition.inputs().get(i), i));
        }
        List<OutPutMethod> outPutMethods = new ArrayList<>(definition.outputs().size());
        for (int i = 0; i < definition.outputs().size(); i++) {
            outPutMethods.add(new OutPutMethod(i, definition.outputs().get(i)));
        }
        for (int i = 0; i < rules.size(); i++) {
            DecisionRule decisionRule = rules.get(i);
            List<String> inputEntry = decisionRule.inputEntry();
            List<String> outputEntry = decisionRule.outputEntry();
            //TODO hitPolicy
            IfStmt ifStmt = new IfStmt();
            ifStmt.setCondition(getTestRule(i, inputMethods, inputEntry).addArgument(rootInput));
            BlockStmt thenBlock = new BlockStmt();
            ifStmt.setThenStmt(thenBlock);
            thenBlock.addStatement(new ReturnStmt(getOutputRule(i, outPutMethods, outputEntry).addArgument(rootInput)));
            body.addStatement(ifStmt);
        }
        body.addStatement(new ReturnStmt(getDefaultOutputRule(outPutMethods).addArgument(rootInput)));
    }

    private MethodCallExpr getOutputRule(int ruleIndex, List<OutPutMethod> outPutMethods, List<String> outputEntry) {
        return createOutputRule(ruleIndex, "outputRule" + ruleIndex, outPutMethods, outputEntry);
    }

    private MethodCallExpr getDefaultOutputRule(List<OutPutMethod> outPutMethods) {
        return createOutputRule(-1, "defaultOutput", outPutMethods, null);
    }

    private MethodCallExpr createOutputRule(int ruleIndex, String methodName, List<OutPutMethod> outPutMethods, @Nullable List<String> outputEntry) {
        int outPutSize = outPutMethods.size();
        if (outputEntry != null && outPutSize != outputEntry.size()) {
            throw new IllegalArgumentException("The size of outputEntry must be equal to the size of outputs");
        }

        MethodDeclaration methodDeclaration = new MethodDeclaration();
        manager.getClassDeclaration().getMembers().add(methodDeclaration);
        methodDeclaration.setName(methodName);
        methodDeclaration.setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
        methodDeclaration.setType(manager.getClassType(EvalResult.class));
        NameExpr input = new NameExpr("input");
        methodDeclaration.addParameter(new Parameter(inputType, input.getName()));
        BlockStmt body = new BlockStmt();
        methodDeclaration.setBody(body);

        if (outputEntry == null && outPutSize == 1 && !outPutMethods.get(0).hasDefaultOutput()) {
            body.addStatement(new ReturnStmt(manager.getStaticField(EvalResult.class, "NULL")));
            return new MethodCallExpr(null, methodDeclaration.getName());
        }

        List<NameExpr> outVars = new ArrayList<>(outPutSize);
        for (int i = 0; i < outPutSize; i++) {
            OutPutMethod outPutMethod = outPutMethods.get(i);
            NameExpr outVarName = new NameExpr("out" + i);
            VariableDeclarator outVar;

            if (outputEntry == null) {
                outVar = new VariableDeclarator(manager.getType(FContext.ANY.getJavaType()), outVarName.getName());
                if (outPutMethod.hasDefaultOutput()) {
                    outVar.setInitializer(outPutMethod.defaultOutput(input));
                } else {
                    outVar.setInitializer(new NullLiteralExpr());
                }
            } else {
                String rule = outputEntry.get(i);
                SimpleName ruleMethodName = new SimpleName(methodName + "Index" + i);
                task.asFeelExpr(true).methodName(ruleMethodName.getIdentifier());
                ASTNode astNode = FeelUtil.parseExpr2AST(rule, task.inputTypes());
                ASTCompilerVisitor.instance(context).visit(astNode);
                outVar = new VariableDeclarator(manager.getType(astNode.getType().getJavaType()), outVarName.getName());
                outVar.setInitializer(new MethodCallExpr(null, ruleMethodName).addArgument(input));
            }
            body.addStatement(new VariableDeclarationExpr(outVar));

            outVars.add(outVarName);
            if (outPutMethod.hasCheckOutputValues()) {
                IfStmt ifStmt = new IfStmt();
                ifStmt.setCondition(outPutMethod.checkOutputValues(outVarName, input));
                ifStmt.setThenStmt(new BlockStmt().addStatement(new ReturnStmt(manager.getStaticMethod(EvalResult.class, "ofError")
                        .addArgument(new StringLiteralExpr((outputEntry == null ? "default" : "rule " + ruleIndex) + " output " + i + " values check failed")))));
                body.addStatement(ifStmt);
            }
        }

        if (outVars.size() == 1) {
            body.addStatement(new ReturnStmt(manager.getStaticMethod(EvalResult.class, "of").addArgument(outVars.get(0))));
        } else {
            var result = new VariableDeclarator(manager.getType(FContext.ANY.getJavaType()), "result");
            result.setInitializer(new ObjectCreationExpr(null, manager.getClassType(LinkedHashMap.class).setTypeArguments(),
                    NodeList.nodeList(new IntegerLiteralExpr(Integer.toString(outVars.size())))));
            NameExpr resultExpr = result.getNameAsExpression();
            for (int i = 0; i < outVars.size(); i++) {
                OutPutMethod outPutMethod = outPutMethods.get(i);
                body.addStatement(new MethodCallExpr(resultExpr, "put")
                        .addArgument(CodeGens.stringLiteral(outPutMethod.outputClause.name()))
                        .addArgument(outVars.get(i)));
            }
            body.addStatement(new ReturnStmt(manager.getStaticMethod(EvalResult.class, "of").addArgument(resultExpr)));
        }
        return new MethodCallExpr(null, methodDeclaration.getName());
    }

    @NotNull
    private MethodCallExpr getTestRule(int ruleIndex, List<InputMethod> inputMethods, List<String> ruleEntry) {
        if (ruleEntry.size() != inputMethods.size()) {
            throw new IllegalArgumentException("The size of ruleEntry must be equal to the size of inputMethods");
        }
        if (inputMethods.isEmpty()) {
            throw new IllegalArgumentException("The size of inputMethods must be greater than 0");
        }
        MethodDeclaration testRule = new MethodDeclaration();
        manager.getClassDeclaration().getMembers().add(testRule);
        testRule.setName("testRule" + ruleIndex);
        testRule.setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
        testRule.setType(PrimitiveType.booleanType());
        NameExpr input = new NameExpr("input");
        testRule.addParameter(new Parameter(inputType, input.getName()));
        BlockStmt body = new BlockStmt();
        testRule.setBody(body);
        for (int i = 0; i < inputMethods.size(); i++) {
            InputMethod inputMethod = inputMethods.get(i);
            String rule = ruleEntry.get(i);

            Expression obj;
            if (inputMethod.hasGetInput()) {
                var objVar = new VariableDeclarator(manager.getType(inputMethod.getInputReturnType.getJavaType()), "obj" + i);
                objVar.setInitializer(inputMethod.getInput(input));
                body.addStatement(new VariableDeclarationExpr(objVar));
                obj = objVar.getNameAsExpression();
            } else {
                obj = new NullLiteralExpr();
            }
            MethodCallExpr testRuleIndex = getTestRuleIndex(ruleIndex, i, rule, inputMethod.getInputReturnType, task.inputTypes())
                    .addArgument(obj).addArgument(input);
            if (i != inputMethods.size() - 1) {
                IfStmt ifStmt = new IfStmt();
                Expression condition = new UnaryExpr(testRuleIndex, UnaryExpr.Operator.LOGICAL_COMPLEMENT);
                if (inputMethod.hasCheckInputValues()) {
                    condition = new BinaryExpr(new UnaryExpr(inputMethod.checkInputValues(obj, input), UnaryExpr.Operator.LOGICAL_COMPLEMENT)
                            , condition, BinaryExpr.Operator.OR);
                }
                ifStmt.setCondition(condition);
                ifStmt.setThenStmt(new BlockStmt().addStatement(new ReturnStmt(new BooleanLiteralExpr(false))));
                body.addStatement(ifStmt);
            } else {
                if (inputMethod.hasCheckInputValues()) {
                    body.addStatement(new ReturnStmt(new BinaryExpr(testRuleIndex, inputMethod.checkInputValues(obj, input), BinaryExpr.Operator.AND)));
                } else {
                    body.addStatement(new ReturnStmt(testRuleIndex));
                }
            }
        }
        return new MethodCallExpr(null, testRule.getName());
    }

    private MethodCallExpr getTestRuleIndex(int ruleIndex, int inputIndex, String ruleExpression, FType testInputType, Map<String, FType> objInputTypes) {
        SimpleName methodName = new SimpleName("testRule" + ruleIndex + "Index" + inputIndex);
        CompilerTask.instance(context).asFeelExpr(false).methodName(methodName.getIdentifier());
        ASTNode astNode = FeelUtil.parseUT2AST(ruleExpression, testInputType, objInputTypes);
        ASTCompilerVisitor.instance(context).visit(astNode);
        return new MethodCallExpr(null, methodName);
    }

    private Expression getInputsCreator(@Nullable List<InputClause> inputClauses) {
        if (inputClauses == null) {
            return new NullLiteralExpr();
        }
        MethodCallExpr staticMethod = manager.getStaticMethod(List.class, "of");
        for (InputClause inputClause : inputClauses) {
            String inputExpression = inputClause.inputExpression();
            String inputValues = inputClause.inputValues();
            staticMethod.addArgument(new ObjectCreationExpr()
                    .setType(manager.getClassType(InputClause.class))
                    .addArgument(inputExpression == null ? new NullLiteralExpr() : CodeGens.stringLiteral(inputExpression))
                    .addArgument(inputValues == null ? new NullLiteralExpr() : CodeGens.stringLiteral(inputValues))
            );
        }
        return staticMethod;
    }

    private Expression getOutputsCreator(@Nullable List<OutputClause> outputClauses) {
        if (outputClauses == null) {
            return new NullLiteralExpr();
        }
        MethodCallExpr staticMethod = manager.getStaticMethod(List.class, "of");
        for (OutputClause outputClause : outputClauses) {
            String name = outputClause.name();
            String outputValues = outputClause.outputValues();
            String defaultOutputEntry = outputClause.defaultOutputEntry();
            staticMethod.addArgument(new ObjectCreationExpr()
                    .setType(manager.getClassType(OutputClause.class))
                    .addArgument(name == null ? new NullLiteralExpr() : CodeGens.stringLiteral(name))
                    .addArgument(outputValues == null ? new NullLiteralExpr() : CodeGens.stringLiteral(outputValues))
                    .addArgument(defaultOutputEntry == null ? new NullLiteralExpr() : CodeGens.stringLiteral(defaultOutputEntry))
            );
        }
        return staticMethod;
    }

    private Expression getStringsCreator(@Nullable List<String> strings) {
        if (strings == null) {
            return new NullLiteralExpr();
        }
        MethodCallExpr staticMethod = manager.getStaticMethod(List.class, "of");
        for (String string : strings) {
            staticMethod.addArgument(CodeGens.stringLiteral(string));
        }
        return staticMethod;
    }

    private Expression getDecisionRulesCreator(@Nullable List<DecisionRule> rules) {
        if (rules == null) {
            return new NullLiteralExpr();
        }
        MethodCallExpr staticMethod = manager.getStaticMethod(List.class, "of");
        for (DecisionRule rule : rules) {
            staticMethod.addArgument(new ObjectCreationExpr()
                    .setType(manager.getClassType(DecisionRule.class))
                    .addArgument(getStringsCreator(rule.inputEntry()))
                    .addArgument(getStringsCreator(rule.outputEntry()))
                    .addArgument(getStringsCreator(rule.annotationEntry()))
            );
        }
        return staticMethod;
    }

    private Expression getEnumCreator(@Nullable Enum<?> enumValue) {
        if (enumValue == null) {
            return new NullLiteralExpr();
        }
        return manager.getStaticField(enumValue.getDeclaringClass(), enumValue.name());
    }


    class InputMethod {
        private final InputClause inputClause;
        private final String getInputMethodName;
        private final FType getInputReturnType;
        private final String checkInputValuesMethodName;


        public InputMethod(InputClause inputClause, int index) {
            this.inputClause = Objects.requireNonNull(inputClause);
            CompilerTask task = CompilerTask.instance(context);

            String expression = inputClause.inputExpression();


            if (expression != null) {
                this.getInputMethodName = "getInput" + index;
                task.asFeelExpr(true).methodName(this.getInputMethodName);
                ASTNode astNode = FeelUtil.parseExpr2AST(expression, task.inputTypes());
                ASTCompilerVisitor.instance(context).visit(astNode);
                this.getInputReturnType = astNode.getType();
            } else {
                this.getInputReturnType = null;
                this.getInputMethodName = null;
            }

            String inputValues = inputClause.inputValues();
            if (inputValues != null) {
                this.checkInputValuesMethodName = "checkInputValues" + index;
                task.asFeelExpr(false).methodName(this.checkInputValuesMethodName);
                ASTNode astNode = FeelUtil.parseUT2AST(inputValues, this.getInputReturnType, task.inputTypes());
                ASTCompilerVisitor.instance(context).visit(astNode);
            } else {
                this.checkInputValuesMethodName = null;
            }
        }

        public boolean hasGetInput() {
            return inputClause.inputExpression() != null;
        }

        public boolean hasCheckInputValues() {
            return inputClause.inputValues() != null;
        }

        public MethodCallExpr getInput(Expression input) {
            if (hasGetInput()) {
                return new MethodCallExpr(null, getInputMethodName).addArgument(input);
            }
            throw new UnsupportedOperationException("No getInput");
        }

        public MethodCallExpr checkInputValues(Expression testInput, Expression input) {
            if (hasCheckInputValues()) {
                return new MethodCallExpr(null, checkInputValuesMethodName).addArgument(testInput).addArgument(input);
            }
            throw new UnsupportedOperationException("No checkInputValues");
        }
    }

    class OutPutMethod {
        private final OutputClause outputClause;
        private final String checkOutputValuesMethodName;
        private final String defaultOutputMethodName;

        public OutPutMethod(int index, OutputClause outputClause) {
            this.outputClause = Objects.requireNonNull(outputClause);
            CompilerTask task = CompilerTask.instance(context);
            FType defaultOutType = FAny.ANY;
            if (outputClause.defaultOutputEntry() != null) {
                this.defaultOutputMethodName = "defaultOutput" + index;
                task.asFeelExpr(true).methodName(this.defaultOutputMethodName);
                ASTNode astNode = FeelUtil.parseExpr2AST(outputClause.defaultOutputEntry(), task.inputTypes());
                defaultOutType = astNode.getType();
                ASTCompilerVisitor.instance(context).visit(astNode);
            } else {
                this.defaultOutputMethodName = null;
            }
            if (outputClause.outputValues() != null) {
                this.checkOutputValuesMethodName = "checkOutputValues" + index;
                task.asFeelExpr(false).methodName(this.checkOutputValuesMethodName);
                ASTNode astNode = FeelUtil.parseUT2AST(outputClause.outputValues(), defaultOutType, task.inputTypes());
                ASTCompilerVisitor.instance(context).visit(astNode);
            } else {
                this.checkOutputValuesMethodName = null;
            }
        }

        public boolean hasDefaultOutput() {
            return outputClause.defaultOutputEntry() != null;
        }

        public boolean hasCheckOutputValues() {
            return outputClause.outputValues() != null;
        }

        public MethodCallExpr checkOutputValues(Expression output, Expression rootInput) {
            if (hasCheckOutputValues()) {
                return new MethodCallExpr(null, this.checkOutputValuesMethodName).addArgument(output).addArgument(rootInput);
            }
            throw new UnsupportedOperationException("No checkOutputValues");
        }

        public MethodCallExpr defaultOutput(Expression input) {
            if (hasDefaultOutput()) {
                return new MethodCallExpr(null, this.defaultOutputMethodName).addArgument(input);
            }
            throw new UnsupportedOperationException("No defaultOutput");
        }
    }
}