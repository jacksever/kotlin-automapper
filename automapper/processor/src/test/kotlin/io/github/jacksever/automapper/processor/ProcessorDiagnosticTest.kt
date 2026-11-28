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

package io.github.jacksever.automapper.processor

import com.google.devtools.ksp.impl.KotlinSymbolProcessing
import com.google.devtools.ksp.processing.KSPJvmConfig
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode
import io.github.jacksever.automapper.annotation.AutoMapper
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.io.TempDir

class ProcessorDiagnosticTest {

    @TempDir
    lateinit var workingDirectory: Path

    @Test
    fun `test mapper without source parameter reports a diagnostic`() {
        assertProcessingError(
            source = """
                package example

                import io.github.jacksever.automapper.annotation.AutoMapper
                import io.github.jacksever.automapper.annotation.AutoMapperModule

                data class Target(val value: Int)

                @AutoMapperModule
                internal interface MapperModule {
                    @AutoMapper(reversible = false)
                    fun map(): Target
                }
                """,
            expectedMessage = "Function 'map' annotated with @AutoMapper must have exactly one parameter",
        )
    }

    @Test
    fun `test converter with multiple parameters reports a diagnostic`() {
        assertProcessingError(
            source = """
                package example

                import io.github.jacksever.automapper.annotation.AutoConverter
                import io.github.jacksever.automapper.annotation.AutoMapper
                import io.github.jacksever.automapper.annotation.AutoMapperModule

                data class Source(val value: Int)
                data class Target(val value: Long)

                object InvalidConverter {
                    @AutoConverter
                    fun convert(first: Int, second: Int): Long = (first + second).toLong()
                }

                @AutoMapperModule(converters = [InvalidConverter::class])
                internal interface MapperModule {
                    @AutoMapper(reversible = false)
                    fun map(source: Source): Target
                }
                """,
            expectedMessage = "Invalid @AutoConverter function 'example.InvalidConverter.convert': must have exactly one parameter and a non-Unit return type",
        )
    }

    @Test
    fun `test converter with unresolved source type reports a diagnostic`() {
        assertProcessingError(
            source = """
                package example

                import io.github.jacksever.automapper.annotation.AutoConverter
                import io.github.jacksever.automapper.annotation.AutoMapper
                import io.github.jacksever.automapper.annotation.AutoMapperModule

                data class Source(val value: Int)
                data class Target(val value: Long)

                object InvalidConverter {
                    @AutoConverter
                    fun convert(value: MissingSource): Long = 0L
                }

                @AutoMapperModule(converters = [InvalidConverter::class])
                internal interface MapperModule {
                    @AutoMapper(reversible = false)
                    fun map(source: Source): Target
                }
                """,
            expectedMessage = "Cannot resolve signature of @AutoConverter function 'example.InvalidConverter.convert' in registered converter 'example.InvalidConverter'",
        )
    }

    @Test
    fun `test converter with unresolved return type reports a diagnostic`() {
        assertProcessingError(
            source = """
                package example

                import io.github.jacksever.automapper.annotation.AutoConverter
                import io.github.jacksever.automapper.annotation.AutoMapper
                import io.github.jacksever.automapper.annotation.AutoMapperModule

                data class Source(val value: Int)
                data class Target(val value: Long)

                object InvalidConverter {
                    @AutoConverter
                    fun convert(value: Int): MissingTarget = error("Unresolved return type")
                }

                @AutoMapperModule(converters = [InvalidConverter::class])
                internal interface MapperModule {
                    @AutoMapper(reversible = false)
                    fun map(source: Source): Target
                }
                """,
            expectedMessage = "Cannot resolve signature of @AutoConverter function 'example.InvalidConverter.convert' in registered converter 'example.InvalidConverter'",
        )
    }

    @Test
    fun `test missing required target property reports a diagnostic`() {
        assertProcessingError(
            source = """
                package example

                import io.github.jacksever.automapper.annotation.AutoMapper
                import io.github.jacksever.automapper.annotation.AutoMapperModule

                data class Source(val value: Int)
                data class Target(val value: Int, val required: String)

                @AutoMapperModule
                internal interface MapperModule {
                    @AutoMapper(reversible = false)
                    fun map(source: Source): Target
                }
                """,
            expectedMessage = "Cannot map property 'required' for example.Target. No matching property found in example.Source and no default value is provided",
        )
    }

    @Test
    fun `test underdetermined sealed type parameter reports a diagnostic`() {
        assertProcessingError(
            source = """
                package example

                import io.github.jacksever.automapper.annotation.AutoMapper
                import io.github.jacksever.automapper.annotation.AutoMapperModule

                sealed interface Source<T> {
                    data class Value<T>(val payload: T) : Source<T>
                }
                sealed interface Target<T> {
                    data class Value<A, B>(val payload: A, val extra: B) : Target<A>
                }

                @AutoMapperModule
                internal interface MapperModule {
                    @AutoMapper(reversible = false)
                    fun map(source: Source<String>): Target<String>
                }
                """,
            expectedMessage = "Cannot infer type arguments for sealed subclass 'example.Target.Value'",
        )
    }

    @Test
    fun `test unconstructible projected target reports a diagnostic`() {
        assertProcessingError(
            source = """
                package example

                import io.github.jacksever.automapper.annotation.AutoMapper
                import io.github.jacksever.automapper.annotation.AutoMapperModule

                data class Source(val payload: Int)
                data class Target<T : Comparable<T>>(val payload: T)

                @AutoMapperModule
                internal interface MapperModule {
                    @AutoMapper(reversible = false)
                    fun map(source: Source): Target<*>
                }
                """,
            expectedMessage = "use concrete target type arguments",
        )
    }

    @Test
    fun `test generic mappings with conflicting receiver report a diagnostic`() {
        assertProcessingError(
            source = """
                package example

                import io.github.jacksever.automapper.annotation.AutoMapper
                import io.github.jacksever.automapper.annotation.AutoMapperModule

                data class Source<T>(val payload: T)
                data class Target<T>(val payload: T)

                @AutoMapperModule
                internal interface MapperModule {
                    @AutoMapper(reversible = false)
                    fun first(source: Source<String>): Target<String>

                    @AutoMapper(reversible = false)
                    fun second(source: Source<String>): Target<Long>
                }
                """,
            expectedMessage = "Conflicting generated mapping signature for mapper methods: first, second",
        )
    }

    @Test
    fun `test nullable root mapping reports a diagnostic`() {
        assertProcessingError(
            source = """
                package example

                import io.github.jacksever.automapper.annotation.AutoMapper
                import io.github.jacksever.automapper.annotation.AutoMapperModule

                data class Source<T>(val payload: T)
                data class Target<T>(val payload: T)

                @AutoMapperModule
                internal interface MapperModule {
                    @AutoMapper(reversible = false)
                    fun map(source: Source<String>?): Target<String>
                }
                """,
            expectedMessage = "Mapper 'map' must use non-nullable source and target types",
        )
    }

    @Test
    fun `test unrelated generic parameters report missing conversion`() {
        assertProcessingError(
            source = """
                package example

                import io.github.jacksever.automapper.annotation.AutoMapper
                import io.github.jacksever.automapper.annotation.AutoMapperModule

                data class Source<T>(val payload: T)
                data class Target<T>(val payload: T)

                @AutoMapperModule
                internal interface MapperModule {
                    @AutoMapper(reversible = false)
                    fun <T, U> map(source: Source<T>): Target<U>
                }
                """,
            expectedMessage = "No conversion available from 'T' to 'U'",
        )
    }

    @Test
    fun `test reified converter rejects non-reified mapper argument`() {
        assertProcessingError(
            source = """
                package example

                import io.github.jacksever.automapper.annotation.AutoConverter
                import io.github.jacksever.automapper.annotation.AutoMapper
                import io.github.jacksever.automapper.annotation.AutoMapperModule

                data class Source<T>(val payload: T)
                data class Target(val payload: String)

                object Converter {
                    @AutoConverter
                    inline fun <reified T> convert(value: T): String = T::class.simpleName.orEmpty()
                }

                @AutoMapperModule(converters = [Converter::class])
                internal interface MapperModule {
                    @AutoMapper(reversible = false)
                    fun <T> map(source: Source<T>): Target
                }
                """,
            expectedMessage = "No conversion available from 'T' to 'String'",
        )
    }

    @Test
    fun `test reified converter rejects array with non-reified element parameter`() {
        assertProcessingError(
            source = """
                package example

                import io.github.jacksever.automapper.annotation.AutoConverter
                import io.github.jacksever.automapper.annotation.AutoMapper
                import io.github.jacksever.automapper.annotation.AutoMapperModule

                data class Source<T>(val payload: Array<T>)
                data class Target(val payload: String)

                object Converter {
                    @AutoConverter
                    inline fun <reified R> convert(value: R): String = R::class.simpleName.orEmpty()
                }

                @AutoMapperModule(converters = [Converter::class])
                internal interface MapperModule {
                    @AutoMapper(reversible = false)
                    fun <T> map(source: Source<T>): Target
                }
                """,
            expectedMessage = "No conversion available from 'Array<T>' to 'String'",
        )
    }

    private fun assertProcessingError(source: String, expectedMessage: String) {
        val inputDirectory = workingDirectory.resolve("sources").createDirectories()
        inputDirectory.resolve("Input.kt").writeText(source.trimIndent())
        val outputDirectory = workingDirectory.resolve("output").toFile()

        val kotlinVersion = KotlinVersion.CURRENT.toString()
        val config = KSPJvmConfig.Builder().apply {
            jvmTarget = "21"
            apiVersion = kotlinVersion
            outputBaseDir = outputDirectory
            moduleName = "processor-diagnostic-test"
            projectBaseDir = workingDirectory.toFile()
            sourceRoots = listOf(inputDirectory.toFile())
            languageVersion = kotlinVersion
            jdkHome = File(System.getProperty("java.home"))
            javaOutputDir = outputDirectory.resolve("java")
            classOutputDir = outputDirectory.resolve("classes")
            kotlinOutputDir = outputDirectory.resolve("kotlin")
            cachesDir = workingDirectory.resolve("caches").toFile()
            resourceOutputDir = outputDirectory.resolve("resources")
            libraries = listOf(AutoMapper::class.java, Unit::class.java).map { type ->
                File(type.protectionDomain.codeSource.location.toURI())
            }
        }.build()

        val logger = DiagnosticLogger()
        val result = KotlinSymbolProcessing(
            logger = logger,
            kspConfig = config,
            symbolProcessorProviders = listOf(AutoMapperProcessorProvider()),
        ).execute()

        val diagnostics = logger.errors.joinToString(separator = "\n")

        assertEquals(expected = KotlinSymbolProcessing.ExitCode.PROCESSING_ERROR, actual = result)
        assertTrue(
            actual = logger.errors.any { message -> expectedMessage in message },
            message = "Expected diagnostic '$expectedMessage', got:\n$diagnostics",
        )
    }

    private class DiagnosticLogger : KSPLogger {
        val errors = mutableListOf<String>()

        override fun error(message: String, symbol: KSNode?) {
            errors += message
        }

        override fun exception(e: Throwable) {
            throw AssertionError("Unexpected exception during symbol processing", e)
        }

        override fun logging(message: String, symbol: KSNode?) = Unit
        override fun info(message: String, symbol: KSNode?) = Unit
        override fun warn(message: String, symbol: KSNode?) = Unit
    }
}
