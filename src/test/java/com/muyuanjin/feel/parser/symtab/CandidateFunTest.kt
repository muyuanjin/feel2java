package com.muyuanjin.feel.parser.symtab

import com.muyuanjin.feel.lang.FeelFunctions
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * @author muyuanjin
 */
internal class CandidateFunTest : StringSpec({
    "test" {
        for (value in FeelFunctions.entries) {
            val candidateFun = CandidateFun(value.functions)
            candidateFun shouldBe candidateFun.mostSpecific
            candidateFun.mostSpecific shouldBe candidateFun
        }
    }
})