package com.muyuanjin.feel.parser.symtab;

import com.muyuanjin.common.entity.Pair;
import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.type.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TypeBinding 类用于根据给定的类型和附加的类型信息计算推断出的结果类型
 *
 * @author muyuanjin
 */
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public final class TypeBinding {
    private final FType original;
    private final TypeBinding parent;
    private Map<FAny, Pair<TypeBinding, List<TypeBinding>>> anyBindingMap;

    private Map<String, TypeBinding> boundMembers;
    private TypeBinding boundReturnType;
    private List<TypeBinding> boundParameterTypes;
    private TypeBinding boundElementType;
    private Boolean rangeStart;
    private Boolean rangeEnd;
    @ToString.Include
    private FType bound;

    private TypeBinding(FType original, TypeBinding parent) {
        this.original = original;
        this.parent = parent;

        if (original instanceof FContext) {
            this.boundMembers = MapUtil.newLinkedHashMap(original.getMembers().size());
            for (var entry : original.getMembers().entrySet()) {
                boundMembers.put(entry.getKey(), internalOf(entry.getValue()));
            }
        } else if (original instanceof FFunction function) {
            this.boundReturnType = internalOf(function.getReturnType());
            this.boundParameterTypes = new ArrayList<>(function.getParameterTypes().size());
            for (var type : function.getParameterTypes()) {
                boundParameterTypes.add(internalOf(type));
            }
        } else if (original instanceof FList list) {
            this.boundElementType = internalOf(list.getElementType());
        } else if (original instanceof FRange range) {
            this.boundElementType = internalOf(range.getElementType());
            this.rangeStart = range.getStart();
            this.rangeEnd = range.getEnd();
        } else if (original instanceof FAny) {
            bound = original;
        }
    }

    private synchronized TypeBinding internalOf(FType o) {
        if (o instanceof FAny any && any != FAny.ANY) {
            return reportAny(any);
        }
        return new TypeBinding(o, this);
    }

    private synchronized TypeBinding reportAny(FAny any) {
        return reportAny(any, this);
    }

    private synchronized TypeBinding reportAny(FAny any, TypeBinding firstParent) {
        if (this.parent != null) {
            return parent.reportAny(any, firstParent);
        }
        if (anyBindingMap == null) {
            anyBindingMap = new HashMap<>();
        }
        // 泛型TypeBinding是公用的，所以不存在固定的 parent，需要搜集所有 firstParent 用于 clear
        var pair = anyBindingMap.computeIfAbsent(any, k -> new Pair<>(new TypeBinding(k, firstParent), new ArrayList<>(1)));
        pair.getValue().add(firstParent);
        return pair.getKey();
    }

    /**
     * 如果是FAny.A .B 这种泛型需要清理缓存，那么将所有该泛型的父级缓存全部清理，防止出现缓存不一致的情况
     */
    private synchronized void clear() {
        if (original instanceof FAny any) {
            if (any != FAny.ANY) {
                TypeBinding root = this.parent;
                var map = anyBindingMap;
                while (root != null) {
                    map = root.anyBindingMap;
                    root = root.parent;
                }
                var pair = map.get(any);
                if (pair != null) {
                    pair.getValue().forEach(TypeBinding::clear);
                }
            }
        } else {
            bound = null;
        }
        if (parent != null) {
            parent.clear();
        }
    }

    public synchronized void reset() {
        bound = null;
        if (original instanceof FContext) {
            boundMembers.values().forEach(TypeBinding::reset);
        } else if (original instanceof FFunction function) {
            boundParameterTypes.forEach(TypeBinding::reset);
            boundReturnType.reset();
        } else if (original instanceof FList list) {
            boundElementType.reset();
        } else if (original instanceof FRange range) {
            boundElementType.reset();
            this.rangeStart = range.getStart();
            this.rangeEnd = range.getEnd();
        } else if (original instanceof FAny) {
            bound = original;
        }
    }

    public synchronized FType getBound() {
        if (bound == null) {
            if (original instanceof FContext) {
                Map<String, FType> members = MapUtil.newLinkedHashMap(boundMembers.size());
                for (var entry : boundMembers.entrySet()) {
                    members.put(entry.getKey(), entry.getValue().getBound());
                }
                bound = FContext.of(members);
            } else if (original instanceof FFunction function) {
                List<FType> list = new ArrayList<>(boundParameterTypes.size());
                for (var binding : boundParameterTypes) {
                    FType type = binding.getBound();
                    list.add(type);
                }
                bound = FFunction.of(boundReturnType.getBound(), function.getParameterNames(), list);
            } else if (original instanceof FList list) {
                bound = list.isVarargs() ? FList.ofVars(boundElementType.getBound()) : FList.of(boundElementType.getBound());
            } else if (original instanceof FRange) {
                bound = FRange.of(boundElementType.getBound(), this.rangeStart, this.rangeEnd);
            } else if (!(original instanceof FAny)) {
                bound = original;
            }
        }
        return bound;
    }

    public static <T extends FType> TypeBinding of(T original) {
        return new TypeBinding(original, null);
    }

    public synchronized boolean bind(FType type) {
        if (original instanceof FContext && type instanceof FContext) {
            return bindEntryType(type.getMembers());
        }
        if (original instanceof FFunction && type instanceof FFunction function) {
            boolean ret = bindReturnType(function.getReturnType());
            boolean param = bindParameterTypes(function.getParameterTypes());
            return ret || param;
        }
        if (original instanceof FList && type instanceof FList list) {
            return bindListType(list.getElementType());
        }
        if (original instanceof FRange && type instanceof FRange range) {
            return bindRangeType(range.getElementType(), range.getStart(), range.getEnd());
        }
        if (original instanceof FAny) {
            return bindAnyType(type);
        }
        return false;
    }

    public synchronized boolean bindAnyType(FType type) {
        if (!(original instanceof FAny)) throw new UnsupportedOperationException();

        // 尝试绑定 type 或 original，依据它们之间的兼容性和具体情况判断
        if (bound.canConvertTo(type) || bound.equals(original)) {
            // type 更通用或 bound 与 original 相等则绑定 type
            bound = type;
            clear();
            return true;
        }
        // 其他情况均返回false
        return false;
    }

    public synchronized boolean bindListType(FType elementType) {
        if (!(original instanceof FList)) throw new UnsupportedOperationException();
        return tryBind(boundElementType, elementType);
    }

    public synchronized boolean bindRangeType(FType elementType) {
        return bindRangeType(elementType, null, null);
    }

    public synchronized boolean bindRangeType(FType elementType, Boolean start, Boolean end) {
        if (!(original instanceof FRange)) throw new UnsupportedOperationException();
        boolean bind = tryBind(boundElementType, elementType);
        bind |= bindRangeType(start, end);
        return bind;
    }

    public synchronized boolean bindRangeType(Boolean start, Boolean end) {
        if (!(original instanceof FRange)) throw new UnsupportedOperationException();
        if (start == null && end == null) {
            return false;
        }
        if (this.rangeStart == null && this.rangeEnd == null) {
            this.rangeStart = start;
            this.rangeEnd = end;
            this.bound = null;// clear();
            return true;
        }
        return false;
    }

    public synchronized boolean bindEntryType(String key, FType type) {
        if (!(original instanceof FContext)) throw new UnsupportedOperationException();
        TypeBinding entryBinding = boundMembers.get(key);
        if (entryBinding == null) {
            boundMembers.put(key, internalOf(type));
            this.bound = null;// clear();
            return true;
        }
        return tryBind(entryBinding, type);
    }

    public synchronized boolean bindEntryType(Map<String, FType> members) {
        if (!(original instanceof FContext)) throw new UnsupportedOperationException();
        if (members.isEmpty()) {
            return false;
        }
        boolean result = false;
        for (var entry : members.entrySet()) {
            result |= bindEntryType(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public synchronized boolean bindReturnType(FType returnType) {
        if (!(original instanceof FFunction)) throw new UnsupportedOperationException();
        return tryBind(this.boundReturnType, returnType);
    }

    public synchronized boolean bindParameterType(int index, FType type) {
        if (!(original instanceof FFunction)) throw new UnsupportedOperationException();
        if (boundParameterTypes.isEmpty()) {
            return false;
        }
        TypeBinding binding = index >= boundParameterTypes.size() ? boundParameterTypes.get(boundParameterTypes.size() - 1) : boundParameterTypes.get(index);
        if (binding.getOriginal() instanceof FList fList && fList.isVarargs()) {
            //处理变长参数
            if (binding.bindListType(type)) {
                this.bound = null;// clear();
                return true;
            }
            return false;
        }
        return tryBind(binding, type);
    }

    public synchronized boolean bindParameterType(String name, FType type) {
        if (!(original instanceof FFunction function)) throw new UnsupportedOperationException();
        if (function.getParameterNames() == null || function.getParameterNames().isEmpty()) {
            return false;
        }
        int index = function.getParameterNames().indexOf(name);
        if (index < 0) {
            return false;
        }
        return bindParameterType(index, type);
    }

    public synchronized boolean bindParameterTypes(List<FType> types) {
        if (!(original instanceof FFunction)) throw new UnsupportedOperationException();
        if (types.isEmpty()) {
            return false;
        }
        boolean result = false;
        for (int i = 0; i < types.size(); i++) {
            result |= bindParameterType(i, types.get(i));
        }
        return result;
    }

    public synchronized boolean bindParameterTypes(List<String> names, List<FType> types) {
        if (!(original instanceof FFunction function)) throw new UnsupportedOperationException();
        if (names == null || names.isEmpty() || types == null || types.isEmpty() || names.size() != types.size()) {
            return false;
        }
        boolean result = false;
        for (int i = 0; i < names.size(); i++) {
            result |= bindParameterType(names.get(i), types.get(i));
        }
        return result;
    }

    private boolean tryBind(TypeBinding binding, FType type) {
        FType original = binding.getOriginal();
        if (type == null || type.equals(original) || !type.canConvertTo(original)) {
            return false;
        }
        FType bound = binding.getBound();
        // 尝试绑定elementType或original，依据它们之间的兼容性和具体情况判断
        if (bound.canConvertTo(type) || bound.equals(original)) {
            // elementType更通用或bound与original相等，尝试绑定elementType
            return doBind(binding, type);
        } else if (!type.canConvertTo(bound)) {
            // 互不兼容的情况，但满足原始要求，判断是否需要切换回original
            // 如果不是FAny，且成功绑定original，则返回true
            return !(original instanceof FAny) && doBind(binding, original);
        }
        // 其他情况均返回false
        return false;
    }

    private boolean doBind(TypeBinding binding, FType type) {
        if (binding.bind(type)) {
            this.bound = null; // clear()
            return true;
        }
        return false;
    }
}