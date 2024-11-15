package com.muyuanjin.feel.dmn;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Singular;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * DMN表的规则内容<p/>
 * Table 36: DecisionRule attributes and model associations
 *
 * @param inputEntry      获取指定输入条件的UnaryTests实例列表
 *                        这些实例指定了此DecisionRule必须匹配的输入条件
 *                        与对应的（按索引匹配）{@link DecisionTableDefinition#inputs()} 相对应。<p/>
 * @param outputEntry     获取组成此DecisionRule输出组件的LiteralExpression实例列表。
 *                        这些实例按索引组成此DecisionRule的输出组件。<p/>
 * @param annotationEntry 获取组成此DecisionRule注释的RuleAnnotation实例列表。
 *                        这些实例按索引组成此DecisionRule的注释
 *                        并与对应的RuleAnnotationClause实例相匹配。<p/>
 */
@Builder
public record DecisionRule(
        @NotEmpty @Singular("input") List<String> inputEntry,
        @NotEmpty @Singular("output") List<String> outputEntry,
        @Nullable @Singular("annotation") List<String> annotationEntry) {
}