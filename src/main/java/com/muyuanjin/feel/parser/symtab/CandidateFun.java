package com.muyuanjin.feel.parser.symtab;

import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.type.FFunction;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用于重载函数的候选函数集合<P>
 * 重载函数的候选函数集合是一个具有相同的名称函数的集合，但是参数列表不同。在解析过程中，需要根据参数类型选择最匹配的函数。
 *
 * @author muyuanjin
 */
@Getter
@Setter
final class CandidateFun extends FFunction implements FType {
    private final Map<FFunction, TypeBinding> functions;
    private FFunction mostSpecific;
    private boolean onlyOneWinner;

    private List<String> paramNames;
    private List<FType> paramTypes;

    public CandidateFun(List<FFunction> functions) {
        super(functions.get(0).getReturnType(), functions.get(0).getParameterNames(), functions.get(0).getParameterTypes());
        this.functions = MapUtil.newHashMap(functions.size());
        for (FFunction function : functions) {
            this.functions.put(function, TypeBinding.of(function));
        }
        this.mostSpecific = functions.get(0);
    }

    /**
     * 添加一个命名参数
     */
    public void addParam(String name, FType type) {
        if (paramNames == null) {
            paramNames = new ArrayList<>();
        }
        if (paramTypes == null) {
            paramTypes = new ArrayList<>();
        }
        paramNames.add(name);
        paramTypes.add(type);
        updateMostSpecific(name, type);
    }

    /**
     * 添加一个参数
     */
    public void addParam(FType type) {
        if (paramTypes == null) {
            paramTypes = new ArrayList<>();
        }
        paramTypes.add(type);
        updateMostSpecific(null, type);
    }

    /**
     * 更新最匹配的函数
     *
     * @param paramName 参数名
     * @param paramType 参数类型
     */
    private synchronized void updateMostSpecific(String paramName, FType paramType) {
        double maxScore = 0;
        for (var entry : this.functions.entrySet()) {
            FFunction function = entry.getKey();
            TypeBinding binding = entry.getValue();
            int paramCount = function.getParamCount();
            double matchScore = 0;
            if (paramNames == null || function.getParameterNames() == null || paramNames.size() != paramTypes.size()) {
                if (binding.bindParameterType(paramTypes.size() - 1, paramType)) {
                    matchScore += paramCount;
                }
                for (int i = 0; i < paramTypes.size(); i++) {
                    FType type = paramTypes.get(i);
                    FType parameterType = function.getParamType(i);
                    int canBe = type.canBe(parameterType);
                    if (canBe != FType.NO) {
                        matchScore += 2 - 0.5 * canBe;
                    } else {
                        matchScore = 0;
                        break;
                    }
                }
            } else {
                if (binding.bindParameterType(paramName, paramType)) {
                    matchScore += paramCount;
                }
                for (int i = 0; i < paramNames.size(); i++) {
                    FType type = paramTypes.get(i);
                    int index = function.getParameterNames().indexOf(paramNames.get(i));
                    int canBe;
                    if (index != -1 && (canBe = type.canBe(function.getParamType(index))) != FType.NO) {
                        matchScore += 2 - 0.5 * canBe;
                    } else {
                        matchScore = 0;
                        break;
                    }
                }
            }

            if (matchScore > maxScore) {
                maxScore = matchScore;
                onlyOneWinner = true;
                mostSpecific = (FFunction) binding.getBound();
            } else if (matchScore == maxScore) {
                onlyOneWinner = false;
            }
        }
    }

    @Override
    public FType getReturnType() {
        return mostSpecific.getReturnType();
    }

    @Override
    public @Nullable List<String> getParameterNames() {
        return mostSpecific.getParameterNames();
    }

    @Override
    public List<FType> getParameterTypes() {
        return mostSpecific.getParameterTypes();
    }

    @Override
    public String toString() {
        return mostSpecific.toString();
    }

    @Override
    public String getName() {
        return mostSpecific.getName();
    }

    @Override
    public boolean conformsTo(FType t) {
        return mostSpecific.conformsTo(t);
    }

    @Override
    public boolean canConvertTo(FType type) {
        return mostSpecific.canConvertTo(type);
    }

    @Override
    public int canBe(FType type) {
        return mostSpecific.canBe(type);
    }

    @Override
    public FType maxSub(FType type) {
        return mostSpecific.maxSub(type);
    }

    @Override
    public FType minSuper(FType type) {
        return mostSpecific.minSuper(type);
    }

    @Override
    public int getParamCount() {
        return mostSpecific.getParamCount();
    }

    @Override
    public boolean hasVarargs() {
        return mostSpecific.hasVarargs();
    }

    @Override
    public FType getParamType(int i) {
        return mostSpecific.getParamType(i);
    }

    @Override
    public @NotNull Map<String, FType> getMembers() {
        return mostSpecific.getMembers();
    }

    @Override
    public boolean isInstance(Object o) {
        return mostSpecific.isInstance(o);
    }

    @Override
    public Type getJavaType() {
        return mostSpecific.getJavaType();
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {
        return mostSpecific.equals(o);
    }

    @Override
    public int hashCode() {
        return mostSpecific.hashCode();
    }
}