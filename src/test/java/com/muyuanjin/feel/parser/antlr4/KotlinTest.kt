package com.muyuanjin.feel.parser.antlr4

import com.muyuanjin.feel.util.BenchmarkUtil
import org.junit.jupiter.api.Test

/**
 * @author muyuanjin
 */
class KotlinTest {
    data class Role(val weight: Double, val count: Int)

    @Test
    fun test() {
        // 使用数据类构建角色信息列表
        val roles = listOf(
            Role(0.4, 5),
            Role(0.3, 3)
            // ... 添加其他角色信息 ...
        )
        // 使用 sumOf 方法计算加权总人数
        val weightedTotalPeople = roles.sumOf { it.weight * it.count }
        println("加权总人数: $weightedTotalPeople")

        BenchmarkUtil.benchmark1000 { roles.sumOf { it.weight * it.count } }
        BenchmarkUtil.benchmark1000 { roles.sumOf { it.weight * it.count } }
        BenchmarkUtil.benchmark1000 { roles.sumOf { it.weight * it.count } }
    }
}