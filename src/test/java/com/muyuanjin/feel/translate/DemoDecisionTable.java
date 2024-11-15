package com.muyuanjin.feel.translate;

import com.muyuanjin.feel.dmn.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author muyuanjin
 */
public class DemoDecisionTable implements DecisionTable<Object> {
    private static final DecisionTableDefinition DEFINITION = new DecisionTableDefinition(
            "",
            List.of(new InputClause(null, null)),
            List.of(new OutputClause(null, null, null)),
            null, null, HitPolicy.ANY, Aggregation.COUNT
    );


    @Override
    public String getSource() {
        return null;
    }

    @Override
    public EvalResult evaluate(Object rootInput) {
        if (testRule0(rootInput)) {
            return outputRule0(rootInput);
        }
        // testRule1
        // testRule2
        // testRule3

        return EvalResult.of(defaultOutput(rootInput));
    }

    private static boolean testRule0(Object input) {
        var obj0 = getInput0(input);
        if (!checkInputValues0(obj0, input) || !testRule0Index0(obj0, input)) {
            return false;
        }
        var obj1 = getInput1(input);
        return checkInputValues1(obj1, input) && testRule0Index1(obj1, input);
    }


    private static boolean testRule0Index0(Object testInput, Object input) {
        //TODO
        return true;
    }

    private static boolean testRule0Index1(Object testInput, Object input) {
        //TODO
        return true;
    }


    private static EvalResult outputRule0(Object input) {
        Map<String, Object> context = new LinkedHashMap<>(3);
        Object out0 = outputRule0Index0(input);
        if (!checkOutputValues0(out0, input)) {
            return EvalResult.ofError("rule 0 output 0 values check failed");
        }
        context.put("a", out0);

        Object out1 = outputRule0Index0(input);
        if (!checkOutputValues0(out1, input)) {
            return EvalResult.ofError("rule 0 output 1 values check failed");
        }
        context.put("b", out1);

        Object out2 = outputRule0Index0(input);
        if (!checkOutputValues0(out2, input)) {
            return EvalResult.ofError("rule 0 output 2 values check failed");
        }
        context.put("c", out2);

        return EvalResult.of(context);
    }

    private static Object outputRule0Index0(Object input) {
        return null;
    }

    private static Object outputRule0Index1(Object input) {
        return null;
    }

    private static Object outputRule0Index2(Object input) {
        return null;
    }

    private static boolean checkOutputValues0(Object testInput, Object input) {
        return false;
    }

    private static boolean checkOutputValues1(Object testInput, Object input) {
        return false;
    }

    private static boolean checkOutputValues2(Object testInput, Object input) {
        return false;
    }

    private static Object getInput0(Object input) {
        //TODO
        return input;
    }

    private static boolean checkInputValues0(Object testInput, Object input) {
        //TODO
        return true;
    }

    private static Object getInput1(Object input) {
        //TODO
        return input;
    }

    private static boolean checkInputValues1(Object testInput, Object input) {
        //TODO
        return true;
    }


    private static Map<String, Object> defaultGetInput(Map<String, Object> input) {
        return input;
    }

    private static boolean defaultCheckInputValues(Map<String, Object> input) {
        return true;
    }

    private static boolean defaultUnaryTest(Map<String, Object> input) {
        return true;
    }

    private static Object defaultOutput(Object input) {
        Map<String, Object> context = new LinkedHashMap<>(3);
        context.put("xxx", defaultOutput0(input));
        return context;
    }

    private static Object defaultOutput0(Object input) {
        return input;
    }


    @Override
    public DecisionTableDefinition getDefinition() {
        return DEFINITION;
    }
}
