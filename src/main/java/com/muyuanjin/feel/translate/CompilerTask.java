package com.muyuanjin.feel.translate;

import com.muyuanjin.feel.FeelFunctionFactory;
import com.muyuanjin.feel.FeelTypeFactory;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.type.FAny;
import com.muyuanjin.feel.lang.type.FContext;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * @author muyuanjin
 */
@Data
@Accessors(fluent = true)
public class CompilerTask {
    private final Context context;

    private String expression;
    // or is feel unit test
    private boolean asFeelExpr = true;
    private Map<String, FType> inputTypes;

    private String methodName;
    private String className;
    private String packageName;

    private String rootInputParam = "input";
    private FType rootInputType = FContext.ANY;
    private Type rootInputJavaType = FContext.ANY.getJavaType();

    private String unaryTestInputParam = "testInput";
    private FType unaryTestInputType = FAny.ANY;
    private Type unaryTestInputJavaType = FAny.ANY.getJavaType();

    private boolean noLambda = false;
    private boolean allFinal = false;
    private boolean castGenerics = false;

    private FeelTypeFactory typeFactory;
    private FeelFunctionFactory functionFactory;

    public CompilerTask() {
        this.context = new Context();
        this.context.put(taskKey, this);
        this.typeFactory = FeelTypeFactory.instance(context);
        this.functionFactory = FeelFunctionFactory.instance(context);
    }

    private CompilerTask(Context context) {
        context.put(taskKey, this);
        this.context = context;
        this.typeFactory = FeelTypeFactory.instance(context);
        this.functionFactory = FeelFunctionFactory.instance(context);
    }

    public CompilerTask typeFactory(FeelTypeFactory typeFactory) {
        this.typeFactory = Objects.requireNonNull(typeFactory, "typeFactory");
        this.context.put(FeelTypeFactory.class, typeFactory);
        return this;
    }

    public CompilerTask functionFactory(FeelFunctionFactory functionFactory) {
        this.functionFactory = Objects.requireNonNull(functionFactory, "functionFactory");
        this.context.put(FeelFunctionFactory.class, functionFactory);
        return this;
    }

    public CompilerTask rootInput(Object inputObjOrInputJavaType) {
        FType fType = inputObjOrInputJavaType instanceof Type type ? FType.of(type) : FType.of(inputObjOrInputJavaType);
        if (!(fType instanceof FContext ctx)) {
            throw new IllegalArgumentException("Invalid input type, only support POJO or Map");
        }
        this.rootInputType = fType;
        this.inputTypes = ctx.getMembers();
        this.rootInputJavaType = fType.getJavaType();
        return this;
    }

    public CompilerTask unaryTestInput(Object inputObjOrInputJavaType) {
        FType fType;
        if (inputObjOrInputJavaType instanceof Type type) {
            fType = FType.of(type);
        } else if (inputObjOrInputJavaType != null) {
            fType = FType.of(inputObjOrInputJavaType);
        } else {
            fType = FAny.ANY;
        }
        this.unaryTestInputType = fType;
        this.unaryTestInputJavaType = fType.getJavaType();
        return this;
    }

    public CompilerTask copy() {
        CompilerTask copy = new CompilerTask();
        copy.expression = expression;
        copy.asFeelExpr = asFeelExpr;
        copy.inputTypes = inputTypes;
        copy.methodName = methodName;
        copy.className = className;
        copy.packageName = packageName;
        copy.rootInputParam = rootInputParam;
        copy.rootInputType = rootInputType;
        copy.rootInputJavaType = rootInputJavaType;
        copy.unaryTestInputParam = unaryTestInputParam;
        copy.unaryTestInputType = unaryTestInputType;
        copy.unaryTestInputJavaType = unaryTestInputJavaType;
        copy.noLambda = noLambda;
        copy.allFinal = allFinal;
        copy.castGenerics = castGenerics;
        return copy;
    }

    private static final Context.Key<CompilerTask> taskKey = new Context.Key<>();

    public static CompilerTask instance(Context context) {
        CompilerTask instance = context.get(taskKey);
        if (instance == null) {
            instance = new CompilerTask(context);
        }
        return instance;
    }
}