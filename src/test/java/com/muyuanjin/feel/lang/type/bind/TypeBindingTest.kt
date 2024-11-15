package com.muyuanjin.feel.lang.type.bind

import com.muyuanjin.feel.lang.FType
import com.muyuanjin.feel.lang.type.FAny.*
import com.muyuanjin.feel.lang.type.FBoolean.BOOLEAN
import com.muyuanjin.feel.lang.type.FFunction
import com.muyuanjin.feel.lang.type.FList
import com.muyuanjin.feel.lang.type.FNumber.INTEGER
import com.muyuanjin.feel.lang.type.FNumber.NUMBER
import com.muyuanjin.feel.lang.type.FRange
import com.muyuanjin.feel.lang.type.FString.STRING
import com.muyuanjin.feel.parser.symtab.TypeBinding
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * @author muyuanjin
 */
internal class TypeBindingTest : StringSpec({
    "test" {
        var typeBinding = TypeBinding.of(fn(list(A), "list" to list(A), "precedes" to fn(BOOLEAN, A, A)))
        typeBinding.bindReturnType(list(STRING))
        typeBinding.bound shouldBe fn(list(STRING), "list" to list(STRING), "precedes" to fn(BOOLEAN, STRING, STRING))

        typeBinding = TypeBinding.of(fn(INTEGER, list(A), A))
        typeBinding.bindParameterType(0, list(STRING))
        typeBinding.bound shouldBe fn(INTEGER, list(STRING), STRING)

        typeBinding = TypeBinding.of(fn(INTEGER, list(A), A))
        typeBinding.bindParameterType(1, STRING)
        typeBinding.bound shouldBe fn(INTEGER, list(STRING), STRING)

        typeBinding = TypeBinding.of(fn(INTEGER, list(A), A))
        typeBinding.bindParameterType(1, fn(INTEGER, list(A), A))
        typeBinding.bound shouldBe fn(INTEGER, list(fn(INTEGER, list(A), A)), fn(INTEGER, list(A), A))

        typeBinding = TypeBinding.of(fn(INTEGER, list(A), A, B, fn(B, B, list(B))))
        typeBinding.bindParameterType(1, STRING)
        typeBinding.bound shouldBe fn(INTEGER, list(STRING), STRING, B, fn(B, B, list(B)))
        typeBinding.bindParameterType(2, NUMBER)
        typeBinding.bound shouldBe fn(INTEGER, list(STRING), STRING, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))

        typeBinding = TypeBinding.of(fn(INTEGER, list(A), A, B, fn(B, B, list(B))))
        typeBinding.bindParameterType(2, NUMBER)
        typeBinding.bound shouldBe fn(INTEGER, list(A), A, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))
        typeBinding.bindParameterType(1, STRING)
        typeBinding.bound shouldBe fn(INTEGER, list(STRING), STRING, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))

        typeBinding = TypeBinding.of(fn(INTEGER, list(ANY), A, B, fn(B, B, list(B))))
        typeBinding.bindParameterType(2, NUMBER)
        typeBinding.bound shouldBe fn(INTEGER, list(ANY), A, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))
        typeBinding.bindParameterType(1, STRING)
        typeBinding.bound shouldBe fn(INTEGER, list(ANY), STRING, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))
        typeBinding.bindParameterType(0, list(STRING))
        typeBinding.bound shouldBe fn(INTEGER, list(STRING), STRING, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))

        typeBinding =
            TypeBinding.of(fn(INTEGER, list(ANY), A, B, fn(list(list(list(list(A)))), B, list(list(list(B))))))
        typeBinding.bindParameterType(2, NUMBER)
        typeBinding.bound shouldBe fn(
            INTEGER,
            list(ANY),
            A,
            NUMBER,
            fn(list(list(list(list(A)))), NUMBER, list(list(list(NUMBER))))
        )
        typeBinding.bindParameterType(1, STRING)
        typeBinding.bound shouldBe fn(
            INTEGER,
            list(ANY),
            STRING,
            NUMBER,
            fn(list(list(list(list(STRING)))), NUMBER, list(list(list(NUMBER))))
        )
        typeBinding.bindParameterType(0, list(list(STRING)))
        typeBinding.bound shouldBe fn(
            INTEGER,
            list(list(STRING)),
            STRING,
            NUMBER,
            fn(list(list(list(list(STRING)))), NUMBER, list(list(list(NUMBER))))
        )

        typeBinding = TypeBinding.of(fn(INTEGER, list(ANY), A, B, fn(B, B, list(B))))
        typeBinding.bindParameterType(2, NUMBER)
        typeBinding.bound shouldBe fn(INTEGER, list(ANY), A, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))
        typeBinding.bindParameterType(1, STRING)
        typeBinding.bound shouldBe fn(INTEGER, list(ANY), STRING, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))
        typeBinding.bindParameterType(0, list(list(STRING)))
        typeBinding.bound shouldBe fn(INTEGER, list(list(STRING)), STRING, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))

        typeBinding.reset()

        typeBinding.bindParameterType(2, NUMBER)
        typeBinding.bound shouldBe fn(INTEGER, list(ANY), A, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))
        typeBinding.bindParameterType(1, STRING)
        typeBinding.bound shouldBe fn(INTEGER, list(ANY), STRING, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))
        typeBinding.bindParameterType(0, list(list(STRING)))
        typeBinding.bound shouldBe fn(INTEGER, list(list(STRING)), STRING, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))

        typeBinding.reset()

        typeBinding.bindParameterType(2, STRING)
        typeBinding.bound shouldBe fn(INTEGER, list(ANY), A, STRING, fn(STRING, STRING, list(STRING)))
        typeBinding.bindParameterType(1, NUMBER)
        typeBinding.bound shouldBe fn(INTEGER, list(ANY), NUMBER, STRING, fn(STRING, STRING, list(STRING)))
        typeBinding.bindParameterType(0, list(list(NUMBER)))
        typeBinding.bound shouldBe fn(INTEGER, list(list(NUMBER)), NUMBER, STRING, fn(STRING, STRING, list(STRING)))

        typeBinding.reset()

        typeBinding.bindParameterType(2, NUMBER)
        typeBinding.bound shouldBe fn(INTEGER, list(ANY), A, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))
        typeBinding.bindParameterType(1, STRING)
        typeBinding.bound shouldBe fn(INTEGER, list(ANY), STRING, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))
        typeBinding.bindParameterType(0, list(list(STRING)))
        typeBinding.bound shouldBe fn(INTEGER, list(list(STRING)), STRING, NUMBER, fn(NUMBER, NUMBER, list(NUMBER)))

        typeBinding.reset()

        typeBinding.bindParameterType(2, STRING)
        typeBinding.bound shouldBe fn(INTEGER, list(ANY), A, STRING, fn(STRING, STRING, list(STRING)))
        typeBinding.bindParameterType(1, NUMBER)
        typeBinding.bound shouldBe fn(INTEGER, list(ANY), NUMBER, STRING, fn(STRING, STRING, list(STRING)))
        typeBinding.bindParameterType(0, list(list(NUMBER)))
        typeBinding.bound shouldBe fn(INTEGER, list(list(NUMBER)), NUMBER, STRING, fn(STRING, STRING, list(STRING)))

    }

    "变长参数" {
        var typeBinding = TypeBinding.of(fn(list(A), A, FList.ofVars(A)))
        typeBinding.bindParameterType(99, NUMBER)
        typeBinding.bound shouldBe fn(list(NUMBER), NUMBER, FList.ofVars(NUMBER))

        typeBinding = TypeBinding.of(fn(list(A), STRING, FList.ofVars(A)))
        typeBinding.bindParameterType(2, INTEGER)
        typeBinding.bindParameterType(3, NUMBER)
        typeBinding.bound shouldBe fn(list(NUMBER), STRING, FList.ofVars(NUMBER))
    }

    "区间" {
        var typeBinding = TypeBinding.of(fn(INTEGER, range(A), A, A))
        typeBinding.bindParameterType(2, NUMBER)
        typeBinding.bound shouldBe fn(INTEGER, range(NUMBER), NUMBER, NUMBER)

        typeBinding = TypeBinding.of(fn(INTEGER, range(A, true), A, A))
        typeBinding.bindParameterType(2, NUMBER)
        typeBinding.bound shouldBe fn(INTEGER, range(NUMBER, true), NUMBER, NUMBER)

        typeBinding = TypeBinding.of(fn(INTEGER, range(A), A, A))
        typeBinding.bindParameterType(0, range(NUMBER, true))
        typeBinding.bound shouldBe fn(INTEGER, range(NUMBER, true), NUMBER, NUMBER)


    }
})

private fun list(type: FType): FList {
    return FList.of(type)
}

private fun range(type: FType, star: Boolean? = null, end: Boolean? = null): FRange {
    return FRange.of(type, star, end)
}

private fun fn(returnType: FType, vararg params: Pair<String, FType>): FFunction {
    return FFunction.of(returnType, params.toMap(LinkedHashMap()))
}

@Suppress("SameParameterValue")
private fun fn(returnType: FType, vararg params: FType): FFunction {
    return FFunction.of(returnType, *params)
}