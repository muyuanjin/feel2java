package com.muyuanjin.feel.translate;

/**
 * @author muyuanjin
 */
public class ASTParser {
    public ASTParser(Context context) {
        context.put(astParserKey, this);
    }

    public static final Context.Key<ASTParser> astParserKey = new Context.Key<>();

    public static ASTParser instance(Context context) {
        ASTParser instance = context.get(astParserKey);
        if (instance == null) {
            instance = new ASTParser(context);
        }
        return instance;
    }


}
