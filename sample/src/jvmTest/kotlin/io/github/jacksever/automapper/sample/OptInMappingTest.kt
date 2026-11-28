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

@file:OptIn(
    ExperimentalMappingApi::class,
    ExperimentalFirstMappingApi::class,
    ExperimentalSecondMappingApi::class,
)

package io.github.jacksever.automapper.sample

import io.github.jacksever.automapper.sample.mapper.asAliasedOptInTarget
import io.github.jacksever.automapper.sample.mapper.asAnnotationOrderSealedTarget
import io.github.jacksever.automapper.sample.mapper.asAnnotationOrderSource
import io.github.jacksever.automapper.sample.mapper.asAnnotationOrderTarget
import io.github.jacksever.automapper.sample.mapper.asConverterOptInTarget
import io.github.jacksever.automapper.sample.mapper.asMultipleOptInTarget
import io.github.jacksever.automapper.sample.mapper.asOptInFirstTarget
import io.github.jacksever.automapper.sample.mapper.asPropertyOptInTarget
import io.github.jacksever.automapper.sample.model.AliasedOptInSource
import io.github.jacksever.automapper.sample.model.AnnotationOrderSealedSource
import io.github.jacksever.automapper.sample.model.AnnotationOrderSealedTarget
import io.github.jacksever.automapper.sample.model.AnnotationOrderSource
import io.github.jacksever.automapper.sample.model.AnnotationOrderTarget
import io.github.jacksever.automapper.sample.model.ConverterOptInSource
import io.github.jacksever.automapper.sample.model.ConverterOptInTarget
import io.github.jacksever.automapper.sample.model.ExperimentalFirstMappingApi
import io.github.jacksever.automapper.sample.model.ExperimentalFirstMappingValue
import io.github.jacksever.automapper.sample.model.ExperimentalMappingApi
import io.github.jacksever.automapper.sample.model.ExperimentalMappingValue
import io.github.jacksever.automapper.sample.model.ExperimentalSecondMappingApi
import io.github.jacksever.automapper.sample.model.ExperimentalSecondMappingValue
import io.github.jacksever.automapper.sample.model.MultipleOptInSource
import io.github.jacksever.automapper.sample.model.OptInFirstSource
import io.github.jacksever.automapper.sample.model.PropertyOptInSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class OptInMappingTest {

    @Test
    fun `test unrelated class annotation before opt-in does not prevent mapping`() {
        val content = ExperimentalMappingValue(text = "source")
        val target = AnnotationOrderSource(content = content).asAnnotationOrderTarget()

        assertSame(expected = content, actual = target.content)
    }

    @Test
    fun `test reverse mapping also receives opt-in after unrelated annotation`() {
        val content = ExperimentalMappingValue(text = "target")
        val source = AnnotationOrderTarget(content = content).asAnnotationOrderSource()

        assertSame(expected = content, actual = source.content)
    }

    @Test
    fun `test opt-in before unrelated annotation continues to work`() {
        val content = ExperimentalMappingValue(text = "first")
        val target = OptInFirstSource(content = content).asOptInFirstTarget()

        assertSame(expected = content, actual = target.content)
    }

    @Test
    fun `test property opt-in is found after unrelated annotation`() {
        val target = PropertyOptInSource(id = 7).asPropertyOptInTarget()

        assertEquals(expected = "property:7", actual = target.content)
    }

    @Test
    fun `test opt-in is propagated from sealed subclasses`() {
        val content = ExperimentalMappingValue(text = "sealed")
        val target =
            AnnotationOrderSealedSource.Value(content = content).asAnnotationOrderSealedTarget()

        assertIs<AnnotationOrderSealedTarget.Value>(value = target)
        assertSame(expected = content, actual = target.content)
    }

    @Test
    fun `test all opt-in markers are propagated`() {
        val first = ExperimentalFirstMappingValue(text = "first")
        val second = ExperimentalSecondMappingValue(text = "second")
        val target = MultipleOptInSource(first = first, second = second).asMultipleOptInTarget()

        assertSame(expected = first, actual = target.first)
        assertSame(expected = second, actual = target.second)
    }

    @Test
    fun `test aliased kotlin opt-in is found after unrelated opt-in annotation`() {
        val content = ExperimentalMappingValue(text = "alias")
        val target = AliasedOptInSource(content = content).asAliasedOptInTarget()

        assertSame(expected = content, actual = target.content)
    }

    @Test
    fun `test opt-in is propagated from converter function and owner`() {
        val source = ConverterOptInSource.Value(id = 7, flag = true)
        val target = source.asConverterOptInTarget()

        assertEquals(
            expected = ConverterOptInTarget.Value(id = "id:7", flag = "true"),
            actual = target
        )
    }
}
