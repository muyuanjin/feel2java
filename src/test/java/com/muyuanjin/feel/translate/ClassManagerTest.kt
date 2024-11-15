package com.muyuanjin.feel.translate

import com.github.javaparser.StaticJavaParser
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.javaType
import kotlin.reflect.typeOf
import com.github.javaparser.ast.type.Type as javaParserType
import java.lang.reflect.Type as javaType

/**
 * @author muyuanjin
 */
@OptIn(ExperimentalStdlibApi::class)
internal class ClassManagerTest : StringSpec({
    "parse FType" {
        var types: MutableList<javaType> = mutableListOf()
        types.add(String::class.java)
        types.add(Array<String>::class.java)
        types.add(typeOf<List<String>>().javaType)
        types.add(typeOf<List<Array<String>>>().javaType)
        types.add(typeOf<Map<String, Int>>().javaType)
        types.add(typeOf<Map<String, List<Int>>>().javaType)
        types.add(typeOf<Map<String, List<Map<String, Int>>>>().javaType)
        types.add(typeOf<Map<String, List<Map<String, List<Int>>>>>().javaType)
        types.add(typeOf<Map<String, List<Map<Array<String>, List<Array<Int>>>>>>().javaType)
        types = types.toMutableList()
        for (type in types) {
            p(type) shouldBe tp(type)
        }

        val ma =
            ClassManager.instance(CompilerTask().packageName("com.f").className("Test").methodName("test").context())
        for (type in types) {
            ma.getType((type)).toString() shouldBe tp(type).toString()
                .replace("java.lang.", "")
                .replace("java.util.", "")
        }
    }
})

fun p(clazz: Class<Any>): javaParserType = CodeGens.parse(clazz)

fun p(type: javaType): javaParserType = CodeGens.parse(type)

fun tp(type: javaType): javaParserType {
    val parseType = StaticJavaParser.parseType(type.typeName)
    parseType.setTokenRange(null)
    parseType.setRange(null)
    parseType.dataKeys.forEach(parseType::removeData)
    return parseType
}