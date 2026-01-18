/*
 * Copyright (c) 2026 Alexander Gorodnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.jacksever.automapper.processor.converter

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock

/**
 * A helper object that provides conversion expressions for mapping between different primitive types
 *
 * This object maintains a map of standard Kotlin conversion functions (e.g., ".toInt()", ".toString()")
 * for various primitive type pairs. It is used by the processor to automatically generate code
 * for converting a property from one primitive type to another when a direct assignment is not possible
 *
 * For example, if mapping a `String` property to an `Int` property, this object provides the
 * expression `.toInt()`
 */
internal object PrimitiveConverter {

    /**
     * A map that stores the conversion expressions between different primitive types
     */
    private val conversions = mutableMapOf<Pair<String, String>, String>()

    init {
        conversion<String, Int>(".toInt()")
        conversion<String, Long>(".toLong()")
        conversion<String, Float>(".toFloat()")
        conversion<String, Double>(".toDouble()")
        conversion<String, Boolean>(".toBoolean()")

        conversion<Int, Long>(".toLong()")
        conversion<Int, String>(".toString()")

        conversion<Long, Int>(".toInt()")
        conversion<Long, String>(".toString()")

        conversion<Double, Float>(".toFloat()")
        conversion<Double, String>(".toString()")

        conversion<Float, Double>(".toDouble()")
        conversion<Float, String>(".toString()")

        conversion<Boolean, String>(".toString()")
    }

    /**
     * Retrieves the conversion expression for mapping one primitive type to another
     *
     * @param from source [KSType] to convert from
     * @param to target [KSType] to convert to
     * @return [CodeBlock] representing the conversion function call (e.g., ".toInt()"), or an empty [CodeBlock] if no conversion is defined.
     */
    fun getConversion(from: KSType, to: KSType): CodeBlock {
        val fromQualifiedName = from.declaration.qualifiedName?.asString()
        val toQualifiedName = to.declaration.qualifiedName?.asString()

        val conversion = conversions[fromQualifiedName to toQualifiedName]
        return conversion?.let { format -> CodeBlock.of(format) } ?: CodeBlock.of("")
    }

    /**
     * A helper function to register a conversion expression between two primitive Kotlin types
     */
    private inline fun <reified From : Any, reified To : Any> conversion(conversionExpression: String) {
        val fromQualifiedName = "kotlin.${From::class.simpleName}"
        val toQualifiedName = "kotlin.${To::class.simpleName}"

        conversions[fromQualifiedName to toQualifiedName] = conversionExpression
    }
}
