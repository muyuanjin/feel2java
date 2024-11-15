package com.muyuanjin.feel.lang.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.muyuanjin.common.util.LazyRef;
import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.common.util.TypeUtil;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.FeelFunction;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author muyuanjin
 */
public class FFunction implements FType {
    @Getter
    private final FType returnType;
    @Getter
    private final @Nullable List<String> parameterNames;//内置函数和函数定义 具备参数名，类型声明不具备参数名
    @Getter
    private final List<FType> parameterTypes;
    private final LazyRef<Map<String, FType>> members;

    @JsonIgnore
    private transient int hashcode = Integer.MIN_VALUE;
    @JsonIgnore
    private transient String toString;
    @JsonIgnore
    private transient Type javaType;

    public static final FFunction ANY = new FFunction();

    protected FFunction() {
        this(FAny.ANY, null, List.of(FList.ofVars(FAny.ANY)));
    }

    // 构造器用于匿名函数或不需要参数名的情况
    protected FFunction(FType returnType, Map<String, ? extends FType> parameterTypes) {
        this(returnType, parameterTypes.keySet(), parameterTypes.values());
    }

    // 构造器用于需要参数名的情况
    protected FFunction(@NotNull FType returnType, @Nullable Collection<String> parameterNames, @NotNull Collection<? extends FType> parameterTypes) {
        if (parameterNames != null && parameterNames.size() != parameterTypes.size()) {
            throw new IllegalArgumentException("parameterNames and parameterTypes must have the same length");
        }
        boolean hasVarargs = false;
        for (FType parameterType : parameterTypes) {
            Objects.requireNonNull(parameterType);
            if (hasVarargs) {
                throw new IllegalArgumentException("varargs must be the last parameter");
            }
            if (parameterType instanceof FList fList && fList.isVarargs()) {
                hasVarargs = true;
            }
        }
        this.returnType = Objects.requireNonNull(returnType);
        this.parameterNames = parameterNames == null ? null : List.copyOf(parameterNames); // 包含参数名，保证不可变
        this.parameterTypes = List.copyOf(parameterTypes); // 保证不可变
        // 对于可能创建其他类型的类型，需要使用懒加载，防止StackOverflowError
        this.members = LazyRef.of(() -> {
            if (this.parameterNames != null) {
                return Map.of(
                        "returnType", FString.STRING,
                        "parameterTypes", FList.of(FString.STRING),
                        "parameterNames", FList.of(FString.STRING)
                );
            } else {
                return Map.of(
                        "returnType", FString.STRING,
                        "parameterTypes", FList.of(FString.STRING)
                );
            }
        });
    }

    @Override
    public String getName() {
        return "function";
    }

    @Override
    public int canBe(FType type) {
        if (equals(type)) {
            return EQ;
        }
        if (type instanceof FAny) {
            return CT;
        }
        if (type instanceof FFunction function) {
            int result = returnType.canBe(function.returnType);
            if (result == NO) {
                return NO;
            }
            if (parameterTypes.size() != function.parameterTypes.size()) {
                return NO;
            }
            for (int i = 0; i < parameterTypes.size(); i++) {
                int canBe = parameterTypes.get(i).canBe(function.parameterTypes.get(i));
                if (canBe == NO) {
                    return NO;
                }
                result = Math.max(result, canBe);
            }
            return result;
        }
        return NO;
    }

    @Override
    public FType maxSub(FType type) {
        if (equals(type)) {
            return this;
        }
        if (type instanceof FAny) {
            return this;
        }
        if (type instanceof FFunction function) {
            FType returnType = getReturnType().maxSub(function.getReturnType());
            List<String> parameterNames = getParameterNames();
            if (!Objects.equals(parameterNames, function.getParameterNames())) {
                parameterNames = null;
            }

            FType[] parameterTypes = new FType[getParameterTypes().size()];
            for (int i = 0; i < getParameterTypes().size(); i++) {
                parameterTypes[i] = getParameterTypes().get(i).maxSub(function.getParameterTypes().get(i));
            }
            return of(returnType, parameterNames, Arrays.asList(parameterTypes));
        }
        return FNull.NULL;
    }

    @Override
    public FType minSuper(FType type) {
        if (equals(type)) {
            return this;
        }
        if (type instanceof FNull) {
            return this;
        }
        if (type instanceof FFunction function) {
            FType returnType = getReturnType().minSuper(function.getReturnType());

            List<String> parameterNames = getParameterNames();
            if (!Objects.equals(parameterNames, function.getParameterNames())) {
                parameterNames = null;
            }

            int leftSize = getParameterTypes().size();
            int rightSize = function.getParameterTypes().size();
            if (leftSize == rightSize) {
                FType[] parameterTypes = new FType[leftSize];
                for (int i = 0; i < leftSize; i++) {
                    parameterTypes[i] = getParameterTypes().get(i).minSuper(function.getParameterTypes().get(i));
                }
                return of(returnType, parameterNames, Arrays.asList(parameterTypes));
            }
        }
        return FAny.ANY;
    }


    @Override
    public @NotNull Map<String, FType> getMembers() {
        return members.get();
    }

    public int getParamCount() {
        return parameterTypes.size();
    }

    public boolean hasVarargs() {
        return parameterTypes.get(parameterTypes.size() - 1) instanceof FList list && list.isVarargs();
    }

    public FType getParamType(int i) {
        int paramCount = parameterTypes.size();
        FType parameterType = i >= paramCount ? parameterTypes.get(paramCount - 1) : parameterTypes.get(i);
        if (parameterType instanceof FList list && list.isVarargs()) {
            parameterType = list.getElementType();
        }
        return parameterType;
    }

    @Override
    public boolean isInstance(Object o) {
        if (!(o instanceof FeelFunction<?> function)) {
            return false;
        }
        FFunction type = function.type();
        return type != null && type.conformsTo(this);
    }

    @Override
    public Type getJavaType() {
        if (javaType == null) synchronized (this) {
            if (javaType != null) {
                return javaType;
            }
            javaType = TypeUtil.makeParameterizedType(null, FeelFunction.class, returnType.getWrappedJavaType());
        }
        return javaType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !getClass().isAssignableFrom(o.getClass())) return false;

        FFunction fFunction = (FFunction) o;

        if (!getReturnType().equals(fFunction.getReturnType())) return false;
        if (!Objects.equals(getParameterNames(), fFunction.getParameterNames()))
            return false;
        return getParameterTypes().equals(fFunction.getParameterTypes());
    }

    @Override
    public int hashCode() {
        if (hashcode == Integer.MIN_VALUE) synchronized (this) {
            if (hashcode != Integer.MIN_VALUE) {
                return hashcode;
            }
            int result = getReturnType().hashCode();
            result = 31 * result + Objects.hashCode(getParameterNames());
            result = 31 * result + getParameterTypes().hashCode();
            hashcode = result;
        }
        return hashcode;
    }

    @Override
    public String toString() {
        if (toString == null) synchronized (this) {
            if (toString != null) {
                return toString;
            }
            // 匿名函数为：  function<number>->number
            // 不匿名函数为：function<name:number>->number
            StringBuilder builder = new StringBuilder("function<");
            if (parameterNames != null) {
                for (int i = 0; i < parameterNames.size(); i++) {
                    builder.append(parameterNames.get(i)).append(":").append(parameterTypes.get(i));
                    if (i < parameterNames.size() - 1) {
                        builder.append(",");
                    }
                }
            } else {
                for (int i = 0; i < parameterTypes.size(); i++) {
                    builder.append(parameterTypes.get(i));
                    if (i < parameterTypes.size() - 1) {
                        builder.append(",");
                    }
                }
            }
            builder.append(">").append("->").append(returnType);
            toString = builder.toString();
        }
        return toString;
    }

    public static FFunction of(FType returnType, FContext parameterTypes) {
        return of(returnType, parameterTypes.getMembers());
    }

    public static FFunction of(FType returnType) {
        return of(returnType, List.of());
    }

    public static FFunction of(FType returnType, FType... parameterTypes) {
        return of(returnType, List.of(parameterTypes));
    }

    // 构造器用于匿名函数或不需要参数名的情况
    public static FFunction of(FType returnType, Map<String, ? extends FType> parameterTypes) {
        return new FFunction(returnType, parameterTypes);
    }

    public static FFunction of(FType returnType, Collection<? extends FType> parameterTypes) {
        return new FFunction(returnType, null, parameterTypes);
    }

    // 构造器用于需要参数名的情况
    public static FFunction of(@NotNull FType returnType, @Nullable Collection<String> parameterNames, @NotNull Collection<? extends FType> parameterTypes) {
        return new FFunction(returnType, parameterNames, parameterTypes);
    }

    public static FFunction of(FType returnType, String param1, FType type1) {
        return new FFunction(returnType, MapUtil.of(param1, type1));
    }

    public static FFunction of(FType returnType, String param1, FType type1, String param2, FType type2) {
        return new FFunction(returnType, MapUtil.of(param1, type1, param2, type2));
    }

    public static FFunction of(FType returnType, String param1, FType type1, String param2, FType type2, String param3, FType type3) {
        return new FFunction(returnType, MapUtil.of(param1, type1, param2, type2, param3, type3));
    }

    public static FFunction of(FType returnType, String param1, FType type1, String param2, FType type2, String param3, FType type3, String param4, FType type4) {
        return new FFunction(returnType, MapUtil.of(param1, type1, param2, type2, param3, type3, param4, type4));
    }

    public static FFunction of(FType returnType, String param1, FType type1, String param2, FType type2, String param3, FType type3, String param4, FType type4, String param5, FType type5) {
        return new FFunction(returnType, MapUtil.of(param1, type1, param2, type2, param3, type3, param4, type4, param5, type5));
    }

    public static FFunction of(FType returnType, String param1, FType type1, String param2, FType type2, String param3, FType type3, String param4, FType type4, String param5, FType type5, String param6, FType type6) {
        return new FFunction(returnType, MapUtil.of(param1, type1, param2, type2, param3, type3, param4, type4, param5, type5, param6, type6));
    }

    public static FFunction of(FType returnType, String param1, FType type1, String param2, FType type2, String param3, FType type3, String param4, FType type4, String param5, FType type5, String param6, FType type6, String param7, FType type7) {
        return new FFunction(returnType, MapUtil.of(param1, type1, param2, type2, param3, type3, param4, type4, param5, type5, param6, type6, param7, type7));
    }

    public static FFunction of(FType returnType, String param1, FType type1, String param2, FType type2, String param3, FType type3, String param4, FType type4, String param5, FType type5, String param6, FType type6, String param7, FType type7, String param8, FType type8) {
        return new FFunction(returnType, MapUtil.of(param1, type1, param2, type2, param3, type3, param4, type4, param5, type5, param6, type6, param7, type7, param8, type8));
    }

    public static FFunction of(FType returnType, String param1, FType type1, String param2, FType type2, String param3, FType type3, String param4, FType type4, String param5, FType type5, String param6, FType type6, String param7, FType type7, String param8, FType type8, String param9, FType type9) {
        return new FFunction(returnType, MapUtil.of(param1, type1, param2, type2, param3, type3, param4, type4, param5, type5, param6, type6, param7, type7, param8, type8, param9, type9));
    }


    @NotNull
    @SafeVarargs
    public static FFunction of(FType returnType, @NotNull kotlin.Pair<String, ? extends FType>... pairs) {
        return new FFunction(returnType, kotlin.collections.MapsKt.mapOf(pairs));
    }
}