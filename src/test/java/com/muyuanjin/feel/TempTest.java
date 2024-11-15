package com.muyuanjin.feel;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.muyuanjin.feel.translate.ClassManager;
import com.muyuanjin.feel.translate.Context;
import com.muyuanjin.feel.translate.Debugger;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/**
 * @author muyuanjin
 */
public class TempTest {
    @Test
    @SneakyThrows
    void test_2024_11_12_16_59_56() {
        Context context = new Context();
        ClassManager manager = ClassManager.instance(context);
        ClassOrInterfaceType classType = manager.getClassType(Debugger.Worker.class);
        System.out.println(classType.toString());
        Debugger.instance(context).enable();
    }
}