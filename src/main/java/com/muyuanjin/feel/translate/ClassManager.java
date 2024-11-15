package com.muyuanjin.feel.translate;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.muyuanjin.common.entity.Pair;
import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.common.util.TypeUtil;
import com.muyuanjin.feel.exception.FeelRuntimeException;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.FTypes;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.type.*;
import com.muyuanjin.feel.parser.ParserUtil;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;

import java.lang.invoke.*;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

/**
 * @author muyuanjin
 */
@Getter
@Setter
public class ClassManager {
    private final CompilationUnit compilationUnit;
    private final ClassOrInterfaceDeclaration classDeclaration;

    private final Map<String, ImportDeclaration> imports = new HashMap<>();
    /**
     * 静态常量字段，顺序很重要
     */
    private final LinkedHashMap<String, FieldDeclaration> constants = new LinkedHashMap<>();
    private final IdentityHashMap<FieldDeclaration, BlockStmt> constantBlocks = new IdentityHashMap<>();
    private final Map<Object, NameExpr> constantNames = new HashMap<>();
    private final Map<Object, Integer> countMap = new HashMap<>();

    private final List<MethodDeclaration> methods = new ArrayList<>();

    private BlockStmt staticBlock;

    private MethodDeclaration currentMethod;
    private BlockStmt currentBlock;
    private JavaExpr currentExpr;

    private ASTNode currentNode;

    private static final Context.Key<ClassManager> classManagerKey = new Context.Key<>();

    public static ClassManager instance(Context context) {
        ClassManager instance = context.get(classManagerKey);
        if (instance == null) {
            instance = new ClassManager(context);
        }
        return instance;
    }

    private ClassManager(Context context) {
        context.put(classManagerKey, this);
        var compilerTask = CompilerTask.instance(context);
        this.compilationUnit = new CompilationUnit(compilerTask.packageName());
        this.classDeclaration = this.compilationUnit.addClass(compilerTask.className());
    }

    public void nextMethod(String methodName, Map<String, java.lang.reflect.Type> paramTypes) {
        closeLast();
        currentMethod = new MethodDeclaration();
        currentMethod.setName(methodName);

        paramTypes.forEach((key, value) -> currentMethod.addParameter(getType(value), key));
        currentMethod.setModifiers(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        currentBlock = new BlockStmt();
        currentMethod.setBody(currentBlock);
        this.currentExpr = null;

        methods.add(currentMethod);
        countMap.compute("__$Method$", (k, v) -> v == null ? 1 : v + 1);
    }

    public String generate() {
        try {
            closeLast();
            compilationUnit.getImports().addAll(imports.values());
            for (FieldDeclaration fieldDeclaration : beautifyNaming(constants.values())) {
                classDeclaration.getMembers().add(fieldDeclaration);
                BlockStmt blockStmt = constantBlocks.get(fieldDeclaration);
                if (blockStmt != null) {
                    classDeclaration.getMembers().add(new InitializerDeclaration(true, blockStmt));
                }
            }
            if (staticBlock != null) {
                classDeclaration.getMembers().add(new InitializerDeclaration(true, staticBlock));
            }
            classDeclaration.getMembers().addAll(methods);
            return compilationUnit.toString();
        } finally {
            clear();
        }
    }

    private void closeLast() {
        if (currentBlock != null) {
            // 给最后的方法添加返回语句
            currentBlock.addStatement(new ReturnStmt(this.currentExpr == null ? new BooleanLiteralExpr(true) : Objects.requireNonNullElseGet(this.currentExpr.expr(), () -> new BooleanLiteralExpr(true))));
        }
        if (currentMethod != null) {
            currentMethod.setType(getType(this.currentExpr == null ? boolean.class : Objects.requireNonNullElse(this.currentExpr.javaType(), boolean.class)));
        }
    }

    public void clear() {
        imports.clear();
        constants.clear();
        methods.clear();
        staticBlock = null;
        currentMethod = null;
        currentBlock = null;
        currentExpr = null;
    }

    public void node(ASTNode node) {
        currentNode = node;
    }

    public ASTNode node() {
        return currentNode;
    }

    public BlockStmt getStaticBlock() {
        if (staticBlock == null) {
            staticBlock = new BlockStmt();
        }
        return staticBlock;
    }

    public JavaExpr update(JavaExpr expression) {
        this.currentExpr = expression;
        return expression;
    }

    public void addComment(String comment) {
        if (comment == null || comment.isBlank()) return;
        for (String str : comment.split("\\R")) {
            if ((str = str.strip()).isBlank()) {
                continue;
            }
            if (currentBlock == null) {
                compilationUnit.addOrphanComment(new LineComment(str));
            } else {
                currentBlock.addOrphanComment(new LineComment(str));
            }
        }
    }

    public void addLocalField(VariableDeclarator declarator) {
        addStatement(CodeGens.setComment(new VariableDeclarationExpr(declarator), node().getText()));
    }

    public void addStatement(Expression expr) {
        currentBlock.addStatement(new ExpressionStmt(expr));
    }

    public void addStatement(Statement statement) {
        currentBlock.addStatement(statement);
    }

    public FieldDeclaration getStaticField(NameExpr name) {
        return constants.get(name.getNameAsString());
    }

    public FieldDeclaration getStaticField(String name) {
        return constants.get(name);
    }

    public void addStaticFieldInit(String name, BlockStmt staticInitBlock) {
        addStaticFieldInit(getStaticField(name), staticInitBlock);
    }

    public void addStaticFieldInit(NameExpr name, BlockStmt staticInitBlock) {
        addStaticFieldInit(getStaticField(name), staticInitBlock);
    }

    public void addStaticFieldInit(FieldDeclaration fieldDeclaration, BlockStmt staticInitBlock) {
        if (staticInitBlock != null) {
            constantBlocks.put(fieldDeclaration, staticInitBlock);
        }
    }

    public NameExpr addStaticField(VariableDeclarator declarator) {
        return addStaticField(declarator, null);
    }

    public NameExpr addStaticField(VariableDeclarator declarator, Object specifier) {
        return addStaticField(new VariableDeclarationExpr(declarator), specifier);
    }

    public NameExpr addStaticField(VariableDeclarator declarator, java.lang.reflect.Type specifierType, Object specifierValue) {
        return addStaticField(new VariableDeclarationExpr(declarator), Pair.of(specifierType, specifierValue));
    }

    public NameExpr addStaticField(VariableDeclarationExpr declarationExpr) {
        return addStaticField(declarationExpr, null);
    }

    public NameExpr addStaticField(VariableDeclarationExpr declarationExpr, java.lang.reflect.Type specifierType, Object specifierValue) {
        return addStaticField(declarationExpr, Pair.of(specifierType, specifierValue));
    }

    /**
     * 添加一个静态常量字段
     *
     * @param declarationExpr 变量声明表达式
     * @param specifier       用于标识的对象，如果不为null则会将specifier映射到常量名，用于重复对象的共享
     * @return 引用常量的表达式
     */
    public NameExpr addStaticField(VariableDeclarationExpr declarationExpr, Object specifier) {
        NameExpr name;
        if (specifier != null && (name = constantNames.get(specifier)) != null) {
            return name;
        }
        int serialNumber = getMethodCount();
        CodeGens.setComment(declarationExpr, node().getText());
        VariableDeclarator variable = declarationExpr.getVariable(0);
        String identifier = variable.getName().getIdentifier();
        String fieldName;
        FieldDeclaration fieldDeclaration;
        do {
            fieldName = ParserUtil.escapeIdentifier("S" + serialNumber++ + '_', identifier);
            fieldDeclaration = constants.get(fieldName);
        } while (fieldDeclaration != null);
        fieldDeclaration = CodeGens.staticField(variable.getType(), fieldName, variable.getInitializer().orElse(null));
        constants.put(fieldName, fieldDeclaration);
        Comment comment = declarationExpr.getComment().orElse(declarationExpr.getVariable(0).getComment().orElse(null));
        if (comment != null) {
            fieldDeclaration.setComment(comment);
        }
        NameExpr nameExpr = new NameExpr(fieldDeclaration.getVariable(0).getName());
        if (specifier != null) {
            constantNames.put(specifier, nameExpr);
        }
        return nameExpr;
    }

    public NameExpr addStaticBigDecimal(String numericLiteral, String... comments) {
        return addStaticBigDecimal(ParserUtil.parserJavaNumber(numericLiteral), comments);
    }

    public NameExpr addStaticBigDecimal(BigDecimal numeric, String... comments) {
        String numericLiteral = numeric.stripTrailingZeros().toString();
        String fieldName = ParserUtil.escapeIdentifier("N_", numericLiteral);
        FieldDeclaration fieldDeclaration = constants.get(fieldName);
        if (fieldDeclaration == null) {
            ObjectCreationExpr initializer = new ObjectCreationExpr();
            ClassOrInterfaceType type = getClassType(BigDecimal.class);
            initializer.setType(type);
            initializer.addArgument(CodeGens.stringLiteral(numericLiteral));
            initializer.addArgument(getStaticField(MathContext.class, "DECIMAL128"));
            fieldDeclaration = CodeGens.staticField(type, fieldName, initializer, comments);
            constants.put(fieldName, fieldDeclaration);
        }
        SimpleName name = fieldDeclaration.getVariable(0).getName();
        return constantNames.computeIfAbsent(numeric, key -> new NameExpr(name));
    }

    public NameExpr addStaticDate(LocalDate date, String... comments) {
        String fieldName = ParserUtil.escapeIdentifier("D_", date.toString());
        FieldDeclaration fieldDeclaration = constants.get(fieldName);
        if (fieldDeclaration == null) {
            ClassOrInterfaceType type = getClassType(LocalDate.class);
            MethodCallExpr initializer = getStaticMethod(LocalDate.class, "parse");
            initializer.addArgument(CodeGens.stringLiteral(date.format(DateTimeFormatter.ISO_LOCAL_DATE)));
            fieldDeclaration = CodeGens.staticField(type, fieldName, initializer, comments);
            constants.put(fieldName, fieldDeclaration);
        }
        SimpleName name = fieldDeclaration.getVariable(0).getName();
        return constantNames.computeIfAbsent(date, key -> new NameExpr(name));
    }

    public NameExpr addStaticTime(LocalTime time, String... comments) {
        String fieldName = ParserUtil.escapeIdentifier("T_", time.toString());
        FieldDeclaration fieldDeclaration = constants.get(fieldName);
        if (fieldDeclaration == null) {
            ClassOrInterfaceType type = getClassType(LocalTime.class);
            MethodCallExpr initializer = getStaticMethod(LocalTime.class, "parse");
            initializer.addArgument(CodeGens.stringLiteral(time.format(DateTimeFormatter.ISO_LOCAL_TIME)));
            fieldDeclaration = CodeGens.staticField(type, fieldName, initializer, comments);
            constants.put(fieldName, fieldDeclaration);
        }
        SimpleName name = fieldDeclaration.getVariable(0).getName();
        return constantNames.computeIfAbsent(time, key -> new NameExpr(name));
    }

    public NameExpr addStaticDateTime(LocalDateTime dateTime, String... comments) {
        String fieldName = ParserUtil.escapeIdentifier("DT_", dateTime.toString());
        FieldDeclaration fieldDeclaration = constants.get(fieldName);
        if (fieldDeclaration == null) {
            ClassOrInterfaceType type = getClassType(LocalDateTime.class);
            MethodCallExpr initializer = getStaticMethod(LocalDateTime.class, "parse");
            initializer.addArgument(CodeGens.stringLiteral(dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            fieldDeclaration = CodeGens.staticField(type, fieldName, initializer, comments);
            constants.put(fieldName, fieldDeclaration);
        }
        SimpleName name = fieldDeclaration.getVariable(0).getName();
        return constantNames.computeIfAbsent(dateTime, key -> new NameExpr(name));
    }

    public NameExpr addStaticDuration(Duration duration, String... comments) {
        String fieldName = ParserUtil.escapeIdentifier("DU_", duration.toString());
        FieldDeclaration fieldDeclaration = constants.get(fieldName);
        if (fieldDeclaration == null) {
            ClassOrInterfaceType type = getClassType(Duration.class);
            MethodCallExpr initializer = new MethodCallExpr(new NameExpr("Duration"), "parse");
            initializer.addArgument(CodeGens.stringLiteral(duration.toString()));
            fieldDeclaration = CodeGens.staticField(type, fieldName, initializer, comments);
            constants.put(fieldName, fieldDeclaration);
        }
        SimpleName name = fieldDeclaration.getVariable(0).getName();
        return constantNames.computeIfAbsent(duration, key -> new NameExpr(name));
    }

    public NameExpr addStaticPeriod(Period period, String... comments) {
        String fieldName = ParserUtil.escapeIdentifier("P_", period.toString());
        FieldDeclaration fieldDeclaration = constants.get(fieldName);
        if (fieldDeclaration == null) {
            ClassOrInterfaceType type = getClassType(Period.class);
            MethodCallExpr initializer = new MethodCallExpr(new NameExpr("Period"), "parse");
            initializer.addArgument(CodeGens.stringLiteral(period.toString()));
            fieldDeclaration = CodeGens.staticField(type, fieldName, initializer, comments);
            constants.put(fieldName, fieldDeclaration);
        }
        SimpleName name = fieldDeclaration.getVariable(0).getName();
        return constantNames.computeIfAbsent(period, key -> new NameExpr(name));
    }


    public Expression addStaticFType(FType type, String... comments) {
        if (type instanceof FAny) {
            return getStaticField(FTypes.class, "ANY");
        }
        if (type instanceof FBoolean) {
            return getStaticField(FTypes.class, "BOOLEAN");
        }
        if (type instanceof FDate) {
            return getStaticField(FTypes.class, "DATE");
        }
        if (type instanceof FDateTime) {
            return getStaticField(FTypes.class, "DATE_TIME");
        }
        if (type instanceof FDayTimeDuration) {
            return getStaticField(FTypes.class, "DAY_TIME_DURATION");
        }
        if (type instanceof FNull) {
            return getStaticField(FTypes.class, "NULL");
        }
        if (type instanceof FNumber) {
            return getStaticField(FTypes.class, "NUMBER");
        }
        if (type instanceof FString) {
            return getStaticField(FTypes.class, "STRING");
        }
        if (type instanceof FTime) {
            return getStaticField(FTypes.class, "TIME");
        }
        if (type instanceof FYearMonthDuration) {
            return getStaticField(FTypes.class, "YEAR_MONTH_DURATION");
        }
        MethodCallExpr initializer;
        ClassOrInterfaceType classType;
        if (type instanceof FList list) {
            initializer = getStaticMethod(FList.class, "of").addArgument(addStaticFType(list.getElementType()));
            classType = getClassType(FList.class);
        } else if (type instanceof FRange range) {
            Boolean start = range.getStart();
            Boolean end = range.getEnd();
            classType = getClassType(FRange.class);
            initializer = getStaticMethod(FRange.class, "of")
                    .addArgument(addStaticFType(range.getElementType()))
                    .addArgument(start == null ? new NullLiteralExpr() : new BooleanLiteralExpr(start))
                    .addArgument(end == null ? new NullLiteralExpr() : new BooleanLiteralExpr(end));
        } else if (type instanceof FContext context) {
            classType = getClassType(FContext.class);
            initializer = getStaticMethod(FContext.class, "ofVars");
            for (Map.Entry<String, FType> entry : context.getMembers().entrySet()) {
                initializer.addArgument(CodeGens.stringLiteral(entry.getKey()));
                initializer.addArgument(addStaticFType(entry.getValue()));
            }
        } else if (type instanceof FFunction function) {
            classType = getClassType(FFunction.class);
            List<String> parameterNames = function.getParameterNames();
            FType returnType = function.getReturnType();
            List<FType> parameterTypes = function.getParameterTypes();
            if (parameterTypes.isEmpty()) {
                initializer = getStaticMethod(FFunction.class, "of").addArgument(addStaticFType(returnType));
            } else {
                MethodCallExpr paramTList = getStaticMethod(List.class, "of");
                for (FType parameterType : parameterTypes) {
                    paramTList.addArgument(addStaticFType(parameterType));
                }
                if (parameterNames != null) {
                    MethodCallExpr paramNList = getStaticMethod(List.class, "of");
                    for (String parameterName : parameterNames) {
                        paramNList.addArgument(CodeGens.stringLiteral(parameterName));
                    }
                    initializer = getStaticMethod(FFunction.class, "of").addArgument(addStaticFType(returnType)).addArgument(paramNList).addArgument(paramTList);
                } else {
                    initializer = getStaticMethod(FFunction.class, "of").addArgument(addStaticFType(returnType)).addArgument(paramTList);
                }
            }
        } else {
            throw new UnsupportedOperationException("Cannot compile type: " + type);
        }
        String strId = type.toString();
        if (type instanceof FRange range) {
            // 美化 range 的 field name, 例如range[]<any> -> range11<any> 防止出现一堆$
            strId = "range" + (range.getStart() != null ? (range.getStart() ? "1" : "0") : "X") +
                    (range.getEnd() != null ? (range.getEnd() ? "1" : "0") : "X") +
                    strId.substring(5);
        }

        String fieldName = ParserUtil.escapeIdentifier("TYPE_", strId);
        FieldDeclaration fieldDeclaration = constants.get(fieldName);
        if (fieldDeclaration == null) {
            fieldDeclaration = CodeGens.staticField(classType, fieldName, initializer, comments);
            constants.put(fieldName, fieldDeclaration);
        }
        SimpleName name = fieldDeclaration.getVariable(0).getName();
        return constantNames.computeIfAbsent(type, key -> new NameExpr(name));
    }

    public EnclosedExpr castTo(java.lang.reflect.Type type, Expression expr) {
        return new EnclosedExpr(new CastExpr(getType(type), new EnclosedExpr(expr)));
    }

    public EnclosedExpr castTo(Type type, Expression expr) {
        return new EnclosedExpr(new CastExpr(type, new EnclosedExpr(expr)));
    }

    public CastExpr simpleCastTo(Type type, Expression expr) {
        return new CastExpr(type, expr);
    }

    public Type getType(@NotNull java.lang.reflect.Type type) {
        if (type instanceof Class<?> clazz) {
            if (clazz.isArray()) {
                return getArrayType(clazz);
            }
            if (clazz == void.class) {
                return getClassType(void.class);
            }
            if (clazz.isPrimitive()) {
                return new PrimitiveType(PrimitiveType.Primitive.byTypeName(clazz.getName()).orElseThrow());
            }
            return getClassType(clazz);
        }
        if (type instanceof ParameterizedType parameterizedType) {
            return getClassType(parameterizedType);
        }
        if (type instanceof GenericArrayType genericArrayType) {
            return new ArrayType(getType(genericArrayType.getGenericComponentType()));
        }
        return CodeGens.parse(type);
    }

    public ClassOrInterfaceType getClassType(@NotNull java.lang.reflect.Type type) {
        if (type instanceof Class<?> clazz) {
            if (clazz.isPrimitive()) {
                return new ClassOrInterfaceType(null, CodeGens.getBoxedName(clazz));
            }
            if (clazz.isArray()) {
                throw new IllegalArgumentException(clazz.getName() + " is an array type, use getArrayType instead");
            }
            if (trySimple(clazz)) {
                return new ClassOrInterfaceType(null, clazz.getSimpleName());
            }
        } else if (type instanceof ParameterizedType parameterizedType) {
            var arguments = parameterizedType.getActualTypeArguments();
            List<Type> list = new ArrayList<>(arguments.length);
            for (var _type : arguments) {
                list.add(getType(_type));
            }
            NodeList<Type> typeArguments = NodeList.nodeList(list);
            if (getType(parameterizedType.getRawType()) instanceof ClassOrInterfaceType clazzType) {
                return new ClassOrInterfaceType(clazzType.getScope().orElse(null), clazzType.getName(), typeArguments);
            }
            return new ClassOrInterfaceType(null, new SimpleName(parameterizedType.getRawType().getTypeName()), typeArguments);
        }
        return (ClassOrInterfaceType) CodeGens.parse(type);
    }

    public ArrayType getArrayType(@NotNull Class<?> clazz) {
        if (!clazz.isArray()) {
            throw new IllegalArgumentException(clazz.getName() + " is not an array type, use getClassType instead");
        }
        if (trySimple(clazz)) {
            return new ArrayType(getType(clazz.getComponentType()));
        }
        return (ArrayType) CodeGens.parse(clazz);
    }

    public Type getListType(@NotNull java.lang.reflect.Type type) {
        return getListType(getType(type));
    }

    public Type getListType() {
        return getListType((Type) null);
    }

    public Type getListType(@Nullable Type type) {
        ClassOrInterfaceType listType = getClassType(List.class);
        return new ClassOrInterfaceType(listType.getScope().orElse(null), listType.getName(), type == null ? null : NodeList.nodeList(type));
    }

    public FieldAccessExpr getStaticField(@NotNull Class<?> clazz, String fieldName) {
        if (trySimple(clazz)) {
            return new FieldAccessExpr(new NameExpr(clazz.getSimpleName()), fieldName);
        }
        return new FieldAccessExpr(new NameExpr(clazz.getCanonicalName()), fieldName);
    }

    public MethodCallExpr getStaticMethod(@NotNull Class<?> clazz, String methodName) {
        if (trySimple(clazz)) {
            return new MethodCallExpr(new NameExpr(clazz.getSimpleName()), methodName);
        }
        return new MethodCallExpr(new NameExpr(clazz.getCanonicalName()), methodName);
    }

    public boolean trySimple(@NotNull Class<?> clazz) {
        if (clazz.isArray()) {
            return trySimple(clazz.getComponentType());
        }
        if (TypeUtil.isPrimitiveOrWrapper(clazz) || "java.lang".equals(clazz.getPackage().getName())) {
            return true;
        }
        if (clazz.isAnonymousClass() || clazz.isLocalClass()) {
            throw new IllegalArgumentException(clazz.getName() + " is an anonymous or local class therefore it can't be added with addImport");
        }
        ImportDeclaration declaration = imports.get(clazz.getSimpleName());
        if (declaration == null) {
            declaration = new ImportDeclaration(clazz.getCanonicalName(), false, false);
            imports.put(clazz.getSimpleName(), declaration);
            return true;
        }
        return declaration.getNameAsString().equals(clazz.getCanonicalName());
    }

    /**
     * <pre>{@code
     *  MethodHandles.Lookup lookup = MethodHandles.lookup();
     * }</pre>
     */
    public NameExpr getLookUp() {
        return constantNames.computeIfAbsent("__$lookUp", key -> {
            FieldDeclaration fieldDeclaration = CodeGens.staticField(getClassType(MethodHandles.Lookup.class),
                    "__$lookUp", new MethodCallExpr(new NameExpr(getClassType(MethodHandles.class).getName()), "lookup"));
            constants.put("__$lookUp", fieldDeclaration);
            return fieldDeclaration.getVariable(0).getNameAsExpression();
        });
    }

    public NameExpr getFunctionType() {
        return constantNames.computeIfAbsent("__$functionType", key -> {
            MethodCallExpr functionType = new MethodCallExpr(new NameExpr("MethodType"), "methodType");
            ClassExpr classExpr = new ClassExpr(getClassType(Object.class));
            functionType.addArgument(classExpr);
            functionType.addArgument(classExpr);
            FieldDeclaration fieldDeclaration = CodeGens.staticField(getClassType(MethodType.class), "__$functionType", functionType);
            constants.put("__$functionType", fieldDeclaration);
            return fieldDeclaration.getVariable(0).getNameAsExpression();
        });
    }

    /**
     * <pre>{@code
     *  MethodType invokedType = MethodType.methodType(Function.class);
     * }</pre>
     */
    public NameExpr getFunInvokeType(Map<String, JavaExpr> outerVars) {
        return constantNames.computeIfAbsent(List.of("__$invokeType", outerVars), key -> {
            MethodCallExpr invokeType = new MethodCallExpr(new NameExpr("MethodType"), "methodType");
            invokeType.addArgument(new ClassExpr(getClassType(Function.class)));
            for (var entry : outerVars.entrySet()) {
                invokeType.addArgument(new ClassExpr(getType(entry.getValue().javaType())));
            }
            String name = nextName("__$invokeType");
            FieldDeclaration fieldDeclaration = CodeGens.staticField(getClassType(MethodType.class), name, invokeType);
            constants.put(name, fieldDeclaration);
            return fieldDeclaration.getVariable(0).getNameAsExpression();
        });
    }

    public NameExpr getImplMethodType(java.lang.reflect.Type returnType, java.lang.reflect.Type... parameterTypes) {
        return constantNames.computeIfAbsent(List.of("__$methodType", returnType, Arrays.asList(parameterTypes)), key -> {
            MethodCallExpr initializer = new MethodCallExpr(new NameExpr("MethodType"), "methodType");
            initializer.addArgument(new ClassExpr(getType(returnType)));
            for (java.lang.reflect.Type parameterType : parameterTypes) {
                initializer.addArgument(new ClassExpr(getType(parameterType)));
            }
            String name = nextName("__$methodType");
            FieldDeclaration fieldDeclaration = CodeGens.staticField(getClassType(MethodType.class), name, initializer);
            constants.put(name, fieldDeclaration);
            return fieldDeclaration.getVariable(0).getNameAsExpression();
        });
    }

    public NameExpr getInsMethodType(java.lang.reflect.Type returnType) {
        return constantNames.computeIfAbsent(List.of("__$methodType", returnType, List.of(Object[].class)), key -> {
            MethodCallExpr initializer = new MethodCallExpr(new NameExpr("MethodType"), "methodType");
            initializer.addArgument(new ClassExpr(getType(returnType)));
            initializer.addArgument(new ClassExpr(getType(Object[].class)));
            String name = nextName("__$methodType");
            FieldDeclaration fieldDeclaration = CodeGens.staticField(getClassType(MethodType.class), name, initializer);
            constants.put(name, fieldDeclaration);
            return fieldDeclaration.getVariable(0).getNameAsExpression();
        });
    }

    public NameExpr getImplMethodHandle(String implMethodName, java.lang.reflect.Type returnType, java.lang.reflect.Type... parameterTypes) {
        NameExpr implMethodType = getImplMethodType(returnType, parameterTypes);
        NameExpr nameExpr = constantNames.computeIfAbsent(classDeclaration, key -> classDeclaration.getNameAsExpression());
        return constantNames.computeIfAbsent(List.of("__$implMethodHandle", implMethodName, returnType, parameterTypes), key -> {
            MethodCallExpr initializer = new MethodCallExpr(getLookUp(), "findStatic");
            initializer.addArgument(new ClassExpr(new ClassOrInterfaceType(null, nameExpr.getName(), null)));
            initializer.addArgument(CodeGens.stringLiteral(implMethodName));
            initializer.addArgument(implMethodType);
            String name = nextName("__$implMethodHandle");

            FieldDeclaration fieldDeclaration = CodeGens.staticField(getClassType(MethodHandle.class), name, null);
            constants.put(name, fieldDeclaration);
            NameExpr nameAsExpression = fieldDeclaration.getVariable(0).getNameAsExpression();
            TryStmt tryStmt = tryInit(nameAsExpression, initializer);
            constantBlocks.put(fieldDeclaration, new BlockStmt().addStatement(tryStmt));
            return nameAsExpression;
        });
    }

    public NameExpr getFunCallSite(String implMethodName, java.lang.reflect.Type returnType, java.lang.reflect.Type... parameterTypes) {
        return getFunCallSite(implMethodName, returnType, Collections.emptyMap(), parameterTypes);
    }

    public NameExpr getFunCallSite(String implMethodName, java.lang.reflect.Type returnType, Map<String, JavaExpr> outerVars, java.lang.reflect.Type... parameterTypes) {
        NameExpr lookUp = getLookUp();
        NameExpr interfaceMethodType = getFunctionType();
        NameExpr invokedType = getFunInvokeType(outerVars);
        NameExpr insMethodType = getInsMethodType(returnType);
        java.lang.reflect.Type[] params = new java.lang.reflect.Type[outerVars.size() + 1];

        MethodDeclaration lambdaMethod = new MethodDeclaration();
        lambdaMethod.setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
        lambdaMethod.setName("__$lambda$$" + implMethodName);
        lambdaMethod.setType(getType(returnType));
        MethodCallExpr methodCallExpr = new MethodCallExpr(implMethodName);
        int index = 0;

        for (var entry : outerVars.entrySet()) {
            //TODO name 可能是在java中的关键字 或不合法名，需要转义
            java.lang.reflect.Type type = entry.getValue().javaType();
            lambdaMethod.addParameter(getType(type), entry.getKey());
            methodCallExpr.addArgument(new NameExpr(entry.getKey()));
            params[index++] = type;
        }
        params[index] = Object[].class;
        NameExpr implMethodType = getImplMethodType(returnType, params);

        lambdaMethod.addParameter(getArrayType(Object[].class), "args");

        NameExpr nameExpr = new NameExpr("args");
        for (int i = 0, parameterTypesLength = parameterTypes.length; i < parameterTypesLength; i++) {
            java.lang.reflect.Type parameterType = parameterTypes[i];
            methodCallExpr.addArgument(new CastExpr(getType(parameterType), new ArrayAccessExpr(nameExpr, new IntegerLiteralExpr(Integer.toString(i)))));
        }
        lambdaMethod.setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(methodCallExpr)));
        methods.add(lambdaMethod);
        String lambdaFunName = lambdaMethod.getNameAsString();
        return constantNames.computeIfAbsent(List.of("__$callSite", lambdaFunName, returnType, parameterTypes), key -> {
            MethodCallExpr initializer = new MethodCallExpr(new NameExpr(getClassType(LambdaMetafactory.class).getName()), "metafactory");
            initializer.addArgument(lookUp);
            initializer.addArgument(CodeGens.stringLiteral("apply"));
            initializer.addArgument(invokedType);
            initializer.addArgument(interfaceMethodType);

            MethodCallExpr findStatic = new MethodCallExpr(getLookUp(), "findStatic");
            NameExpr thisClass = classDeclaration.getNameAsExpression();
            findStatic.addArgument(new ClassExpr(new ClassOrInterfaceType(null, thisClass.getName(), null)));
            findStatic.addArgument(CodeGens.stringLiteral(lambdaFunName));
            findStatic.addArgument(implMethodType);

            initializer.addArgument(findStatic);
            initializer.addArgument(insMethodType);
            String callSite = nextName("__$callSite");
            FieldDeclaration fieldDeclaration = CodeGens.staticField(getClassType(CallSite.class), callSite, null);
            constants.put(callSite, fieldDeclaration);
            NameExpr nameAsExpression = fieldDeclaration.getVariable(0).getNameAsExpression();
            TryStmt tryStmt = tryInit(nameAsExpression, initializer, "Lambda CallSite for " + (node() == null ? lambdaFunName : node().getText()));
            constantBlocks.put(fieldDeclaration, new BlockStmt().addStatement(tryStmt));
            return nameAsExpression;
        });
    }

    public TryStmt tryInit(NameExpr field, Expression initializer, String... comments) {
        return tryInit(node(), field, initializer, comments);
    }

    public TryStmt tryInit(ASTNode node, NameExpr field, Expression initializer, String... comments) {
        TryStmt tryStmt = new TryStmt();
        ExpressionStmt expressionStmt = new ExpressionStmt(new AssignExpr(field, initializer, AssignExpr.Operator.ASSIGN));
        CodeGens.setComment(expressionStmt, comments);
        tryStmt.setTryBlock(new BlockStmt().addStatement(expressionStmt));
        tryStmt.setCatchClauses(NodeList.nodeList(new CatchClause(new Parameter(getClassType(Throwable.class), "_throw"), new BlockStmt()
                .addStatement(new ThrowStmt(new ObjectCreationExpr().setType(getClassType(FeelRuntimeException.class))
                        .addArgument(new IntegerLiteralExpr(Integer.toString(node == null ? 0 : node.getStartLine())))
                        .addArgument(new IntegerLiteralExpr(Integer.toString(node == null ? 0 : node.getEndLine())))
                        .addArgument(new IntegerLiteralExpr(Integer.toString(node == null ? 0 : node.getStartColumn())))
                        .addArgument(new IntegerLiteralExpr(Integer.toString(node == null ? 0 : node.getEndColumn())))
                        .addArgument(CodeGens.stringLiteral(node == null ? "Init Error" : node.getText()))
                        .addArgument(new NameExpr("_throw"))
                )))));
        return tryStmt;
    }

    public String nextName(String prefix) {
        int compute = countMap.compute(prefix, (k, v) -> v == null ? 0 : v + 1);
        return compute == 0 ? prefix : (prefix + compute);
    }

    public int getMethodCount() {
        return countMap.getOrDefault("__$Method$", 0);
    }

    private static Collection<FieldDeclaration> beautifyNaming(Collection<FieldDeclaration> values) {
        Map<String, SimpleName> map = MapUtil.newHashMap(values.size());
        int maxLen = 0;
        for (FieldDeclaration field : values) {
            SimpleName name = field.getVariable(0).getName();
            String key = name.getIdentifier();
            map.put(key, name);
            maxLen = Math.max(maxLen, key.length());
        }
        StringBuilder builder = new StringBuilder(maxLen);
        for (FieldDeclaration field : values) {
            SimpleName name = field.getVariable(0).getName();
            String identifier = name.getIdentifier();
            if (identifier.startsWith("__") || identifier.length() < 6) {
                continue;
            }
            String newName = ParserUtil.removeEscape(identifier);
            if (newName.length() < 3) {
                continue;
            }
            if (newName.length() > 15) {
                String[] split = newName.split("_");
//用于测试变量是否同一引用，如果有不小心不同引用的，修改名字后会报错
//                int index = 0;
//                for (int i = 0; i < split.length; i++) {
//                    if (split[i].length() > 1) {
//                        split[i] = split[i].substring(0, 1).toUpperCase() + split[i].substring(1);
//                    } else {
//                        split[i] = split[i].toUpperCase();
//                    }
//                }
                for (String s : split) {
                    String string = builder.append(s).toString();
                    if (!string.isBlank() && !map.containsKey(string)) {
                        newName = string;
                        break;
                    }
                    builder.append("_");
                }
            }
            if (!newName.isBlank() && !map.containsKey(newName)) {
                name.setIdentifier(newName);
                map.put(newName, name);
            }
            builder.setLength(0);
        }
        return values;
    }
}