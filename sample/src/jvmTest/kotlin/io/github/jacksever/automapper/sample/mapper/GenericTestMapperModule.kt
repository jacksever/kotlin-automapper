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

package io.github.jacksever.automapper.sample.mapper

import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule
import io.github.jacksever.automapper.annotation.DefaultValue
import io.github.jacksever.automapper.annotation.DefaultValueSource
import io.github.jacksever.automapper.sample.converter.AnyGenericConverter
import io.github.jacksever.automapper.sample.converter.BoundedGenericConverter
import io.github.jacksever.automapper.sample.converter.ExactGenericTestConverter
import io.github.jacksever.automapper.sample.converter.GenericSupertypeConverter
import io.github.jacksever.automapper.sample.converter.GenericTestConverter
import io.github.jacksever.automapper.sample.converter.InheritedGenericConverter
import io.github.jacksever.automapper.sample.converter.MixedGenericConverter
import io.github.jacksever.automapper.sample.converter.ReifiedGenericConverter
import io.github.jacksever.automapper.sample.converter.RepeatedGenericConverter
import io.github.jacksever.automapper.sample.converter.ReturnOnlyGenericConverter
import io.github.jacksever.automapper.sample.model.BoundedConverterSource
import io.github.jacksever.automapper.sample.model.BoundedConverterTarget
import io.github.jacksever.automapper.sample.model.BoundedGenericSource
import io.github.jacksever.automapper.sample.model.BoundedGenericTarget
import io.github.jacksever.automapper.sample.model.ContravariantGenericSource
import io.github.jacksever.automapper.sample.model.ContravariantGenericTarget
import io.github.jacksever.automapper.sample.model.GenericCollectionSource
import io.github.jacksever.automapper.sample.model.GenericCollectionTarget
import io.github.jacksever.automapper.sample.model.GenericConvertedCollectionSource
import io.github.jacksever.automapper.sample.model.GenericConvertedCollectionTarget
import io.github.jacksever.automapper.sample.model.GenericConvertedSource
import io.github.jacksever.automapper.sample.model.GenericConvertedTarget
import io.github.jacksever.automapper.sample.model.GenericInheritedSource
import io.github.jacksever.automapper.sample.model.GenericInheritedTarget
import io.github.jacksever.automapper.sample.model.GenericNestedSource
import io.github.jacksever.automapper.sample.model.GenericNestedTarget
import io.github.jacksever.automapper.sample.model.GenericNullableSource
import io.github.jacksever.automapper.sample.model.GenericRequiredTarget
import io.github.jacksever.automapper.sample.model.GenericRuntimeSource
import io.github.jacksever.automapper.sample.model.GenericRuntimeTarget
import io.github.jacksever.automapper.sample.model.GenericSealedSource
import io.github.jacksever.automapper.sample.model.GenericSealedTarget
import io.github.jacksever.automapper.sample.model.GenericSource
import io.github.jacksever.automapper.sample.model.GenericTarget
import io.github.jacksever.automapper.sample.model.IndependentGenericSource
import io.github.jacksever.automapper.sample.model.IndependentGenericTarget
import io.github.jacksever.automapper.sample.model.IndirectGenericSource
import io.github.jacksever.automapper.sample.model.IndirectGenericTarget
import io.github.jacksever.automapper.sample.model.InheritedConverterSource
import io.github.jacksever.automapper.sample.model.InheritedConverterTarget
import io.github.jacksever.automapper.sample.model.InlineGenericSource
import io.github.jacksever.automapper.sample.model.InlineGenericTarget
import io.github.jacksever.automapper.sample.model.ModuleGenericSource
import io.github.jacksever.automapper.sample.model.ModuleGenericTarget
import io.github.jacksever.automapper.sample.model.NestedObjectGenericSource
import io.github.jacksever.automapper.sample.model.NestedObjectGenericTarget
import io.github.jacksever.automapper.sample.model.NullableBoundedGenericSource
import io.github.jacksever.automapper.sample.model.NullableBoundedGenericTarget
import io.github.jacksever.automapper.sample.model.NullableGenericConverterSource
import io.github.jacksever.automapper.sample.model.NullableGenericConverterTarget
import io.github.jacksever.automapper.sample.model.OverloadedDefaultSource
import io.github.jacksever.automapper.sample.model.OverloadedDefaultTarget
import io.github.jacksever.automapper.sample.model.ProjectedGenericSource
import io.github.jacksever.automapper.sample.model.ProjectedGenericTarget
import io.github.jacksever.automapper.sample.model.RankedGenericIntSource
import io.github.jacksever.automapper.sample.model.RankedGenericLongSource
import io.github.jacksever.automapper.sample.model.RankedGenericTarget
import io.github.jacksever.automapper.sample.model.ReifiedContainerSource
import io.github.jacksever.automapper.sample.model.ReifiedGenericSource
import io.github.jacksever.automapper.sample.model.ReifiedGenericTarget
import io.github.jacksever.automapper.sample.model.RejectedBoundConverterSource
import io.github.jacksever.automapper.sample.model.RejectedBoundConverterTarget
import io.github.jacksever.automapper.sample.model.ReorderedGenericSource
import io.github.jacksever.automapper.sample.model.ReorderedGenericTarget
import io.github.jacksever.automapper.sample.model.RepeatedGenericSource
import io.github.jacksever.automapper.sample.model.RepeatedGenericTarget
import io.github.jacksever.automapper.sample.model.ReturnOnlyGenericSource
import io.github.jacksever.automapper.sample.model.ReturnOnlyGenericTarget
import io.github.jacksever.automapper.sample.model.SpecializedGenericSource
import io.github.jacksever.automapper.sample.model.SpecializedGenericTarget
import io.github.jacksever.automapper.sample.model.StarGenericSource
import io.github.jacksever.automapper.sample.model.StarGenericTarget
import io.github.jacksever.automapper.sample.model.StringAliasSource
import io.github.jacksever.automapper.sample.model.StringAliasTarget
import io.github.jacksever.automapper.sample.model.SupertypeConverterSource
import io.github.jacksever.automapper.sample.model.SupertypeConverterTarget
import io.github.jacksever.automapper.sample.model.UpperBoundConverterSource
import io.github.jacksever.automapper.sample.model.UpperBoundConverterTarget
import io.github.jacksever.automapper.sample.model.UpperBoundGenericSource
import io.github.jacksever.automapper.sample.model.UpperBoundGenericTarget

@AutoMapperModule
internal interface GenericTestMapperModule {

    @AutoMapper
    fun textMapper(from: GenericSource<String>): GenericTarget<String>

    @AutoMapper
    fun numberMapper(from: GenericSource<Int>): GenericTarget<Long>

    @AutoMapper
    fun <T> genericMapper(from: GenericSource<T>): GenericTarget<T>

    @AutoMapper
    fun collectionMapper(from: GenericCollectionSource<Int>): GenericCollectionTarget<Long>

    @AutoMapper(
        reversible = false,
        defaultValues = [DefaultValue(property = "payload", source = DefaultValueSource.PARAMETER)]
    )
    fun <T> nullableMapper(from: GenericNullableSource<T>): GenericRequiredTarget<T>

    @AutoMapper(
        reversible = false,
        defaultValues = [DefaultValue(property = "payload", source = DefaultValueSource.PARAMETER)]
    )
    fun <T> runtimeMapper(from: GenericRuntimeSource<T>): GenericRuntimeTarget<T>

    @AutoMapper(reversible = false)
    fun nestedMapper(from: GenericNestedSource<String>): GenericNestedTarget<String>

    @AutoMapper(reversible = false)
    fun inheritedMapper(from: GenericInheritedSource): GenericInheritedTarget

    @AutoMapper
    fun <T> boundedMapper(from: BoundedGenericSource<T>): BoundedGenericTarget<T> where T : Number, T : Comparable<T>

    @AutoMapper
    fun aliasMapper(from: StringAliasSource): StringAliasTarget

    @AutoMapper(reversible = false, converters = [GenericTestConverter::class])
    fun converterMapper(from: GenericConvertedSource): GenericConvertedTarget

    @AutoMapper(reversible = false, converters = [GenericTestConverter::class])
    fun converterCollectionMapper(from: GenericConvertedCollectionSource): GenericConvertedCollectionTarget

    @AutoMapper
    fun <T> sealedMapper(from: GenericSealedSource<T>): GenericSealedTarget<T>

    @AutoMapper
    fun reorderedMapper(from: ReorderedGenericSource<Int, String>): ReorderedGenericTarget<Int, String>

    @AutoMapper(reversible = false)
    fun starMapper(from: StarGenericSource<*>): StarGenericTarget<*>

    @AutoMapper(reversible = false)
    fun projectedMapper(from: ProjectedGenericSource<out Number>): ProjectedGenericTarget<out Number>

    @AutoMapper(reversible = false, converters = [GenericSupertypeConverter::class])
    fun supertypeConverterMapper(from: SupertypeConverterSource): SupertypeConverterTarget

    @AutoMapper(reversible = false, converters = [BoundedGenericConverter::class])
    fun boundedConverterMapper(from: BoundedConverterSource): BoundedConverterTarget

    @AutoMapper(reversible = false, converters = [BoundedGenericConverter::class])
    fun rejectedBoundMapper(from: RejectedBoundConverterSource): RejectedBoundConverterTarget

    @AutoMapper(reversible = false, converters = [InheritedGenericConverter::class])
    fun inheritedConverterMapper(from: InheritedConverterSource): InheritedConverterTarget

    @AutoMapper(reversible = false, converters = [GenericTestConverter::class])
    fun nullableConverterMapper(from: NullableGenericConverterSource): NullableGenericConverterTarget

    @AutoMapper
    fun sealedNumberMapper(from: GenericSealedSource<Int>): GenericSealedTarget<Long>

    @AutoMapper
    fun indirectMapper(from: IndirectGenericSource<List<String>>): IndirectGenericTarget<List<String>>

    @AutoMapper
    fun specializedMapper(from: SpecializedGenericSource<Int>): SpecializedGenericTarget<Long>

    @AutoMapper(
        reversible = false,
        converters = [AnyGenericConverter::class, BoundedGenericConverter::class, ExactGenericTestConverter::class]
    )
    fun rankedIntMapper(from: RankedGenericIntSource): RankedGenericTarget

    @AutoMapper(
        reversible = false,
        converters = [AnyGenericConverter::class, BoundedGenericConverter::class]
    )
    fun rankedLongMapper(from: RankedGenericLongSource): RankedGenericTarget

    @AutoMapper(
        reversible = false,
        converters = [RepeatedGenericConverter::class, MixedGenericConverter::class]
    )
    fun repeatedMapper(from: RepeatedGenericSource): RepeatedGenericTarget

    @AutoMapper(reversible = false, converters = [ReturnOnlyGenericConverter::class])
    fun returnOnlyMapper(from: ReturnOnlyGenericSource): ReturnOnlyGenericTarget

    @AutoMapper(reversible = false, converters = [BoundedGenericConverter::class])
    fun nullableBoundedMapper(from: NullableBoundedGenericSource): NullableBoundedGenericTarget

    @AutoMapper(
        reversible = false,
        defaultValues = [DefaultValue(property = "label", value = "inline")]
    )
    fun inlineMapper(from: InlineGenericSource<String>): InlineGenericTarget<String>

    @AutoMapper(reversible = false)
    fun contravariantMapper(from: ContravariantGenericSource<String>): ContravariantGenericTarget<in String>

    @AutoMapper(reversible = false)
    fun <T : GenericSealedSource<Int>> upperBoundMapper(from: UpperBoundGenericSource<T>): UpperBoundGenericTarget

    @AutoMapper(reversible = false)
    fun nestedObjectMapper(from: NestedObjectGenericSource): NestedObjectGenericTarget

    @AutoMapper(reversible = false, defaultValues = [DefaultValue(property = "payload", value = "default")])
    fun inlineOverloadMapper(from: OverloadedDefaultSource): OverloadedDefaultTarget

    @AutoMapper(
        reversible = false,
        defaultValues = [DefaultValue(property = "payload", source = DefaultValueSource.PARAMETER)]
    )
    fun runtimeOverloadMapper(from: OverloadedDefaultSource): OverloadedDefaultTarget

    @AutoMapper(reversible = false, converters = [GenericSupertypeConverter::class])
    fun <T : Iterable<String>> upperBoundConverterMapper(from: UpperBoundConverterSource<T>): UpperBoundConverterTarget

    @AutoMapper(reversible = false, converters = [ReifiedGenericConverter::class])
    fun <T> reifiedListMapper(from: ReifiedContainerSource<List<T>>): ReifiedGenericTarget

    @AutoMapper(reversible = false, converters = [ReifiedGenericConverter::class])
    fun reifiedArrayMapper(from: ReifiedContainerSource<Array<String>>): ReifiedGenericTarget
}

@AutoMapperModule
internal interface GenericModuleTestMapperModule<T : CharSequence> {

    @AutoMapper
    fun map(from: ModuleGenericSource<T>): ModuleGenericTarget<T>
}

/** Used for testing independent method and module type parameter scopes */
@AutoMapperModule
internal interface IndependentGenericModule<T : Number> {

    @AutoMapper
    fun <R> map(from: IndependentGenericSource<R>): IndependentGenericTarget<R>
}

/** Used for testing legal inline declarations with reified mapper parameters */
@AutoMapperModule
internal interface ReifiedGenericTestMapperModule {

    @AutoMapper(reversible = false, converters = [ReifiedGenericConverter::class])
    private inline fun <reified T> map(from: ReifiedGenericSource<T>): ReifiedGenericTarget =
        error(message = "This method only declares a mapping")
}
