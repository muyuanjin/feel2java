package com.muyuanjin.feel.dmn.impl;

import com.muyuanjin.common.util.LazyLog;
import com.muyuanjin.compiler.CompilationResult;
import com.muyuanjin.compiler.JavaCompiler;
import com.muyuanjin.compiler.util.JUnsafe;
import com.muyuanjin.feel.dmn.DMNCompiler;
import com.muyuanjin.feel.dmn.DecisionTable;
import com.muyuanjin.feel.dmn.DecisionTableDefinition;
import com.muyuanjin.feel.translate.CompilerTask;
import com.muyuanjin.feel.translate.DMNGenerator;
import de.fxlae.typeid.TypeId;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

/**
 * @author muyuanjin
 */
@AllArgsConstructor
public class DefaultDMNCompiler implements DMNCompiler {
    private static final LazyLog log = LazyLog.of(DefaultDMNCompiler.class);
    private final String packageName;
    private final String classNamePrefix;

    public DefaultDMNCompiler() {
        this.packageName = "runtime.feel.dmn";
        this.classNamePrefix = "DMN";
    }

    @Override
    public <T> DecisionTable<T> compile(DecisionTableDefinition definition, T input) {
        return doCompile(definition, input);
    }

    @Override
    public <T> DecisionTable<T> compile(DecisionTableDefinition definition, Class<T> inputType) {
        return doCompile(definition, inputType);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <T> DecisionTable<T> doCompile(DecisionTableDefinition definition, Object inputOrInputType) {
        CompilerTask compilerTask = new CompilerTask().packageName(packageName)
                .className(classNamePrefix + "$" + TypeId.generate())
                .rootInput(inputOrInputType);

        String generate = DMNGenerator.instance(compilerTask.context()).generate(definition);
        try {
            CompilationResult compile = JavaCompiler.NATIVE.compile(compilerTask.className(), generate);
            Class<Object> compiledClass = compile.classes().size() == 1 ? compile.loadSingle() :
                    compile.load(compilerTask.packageName() + "." + compilerTask.className());
            return (DecisionTable<T>) JUnsafe.UNSAFE.allocateInstance(compiledClass);
        } catch (Exception e) {
            log.error("compile error: {}", generate);
            throw e;
        }
    }
}