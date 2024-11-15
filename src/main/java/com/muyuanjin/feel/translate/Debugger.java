package com.muyuanjin.feel.translate;

import java.util.Map;

/**
 * @author muyuanjin
 */
public class Debugger {
    public interface Worker {
        String getSource();

        boolean evaluateUnaryTest(int row, int column, Map<String, Object> context);
    }

    private static final Context.Key<Debugger> debuggerKey = new Context.Key<>();
    private final Context context;

    private Debugger(Context context) {
        context.put(debuggerKey, this);
        this.context = context;
    }

    public static Debugger instance(Context context) {
        Debugger instance = context.get(debuggerKey);
        if (instance == null) {
            instance = new Debugger(context);
        }
        return instance;
    }

    public void enable() {
        ClassManager manager = ClassManager.instance(context);
        manager.getClassDeclaration().addImplementedType(manager.getClassType(Worker.class));
        //TODD 想办法在generate时将源码注入到Worker实现中
    }

}
