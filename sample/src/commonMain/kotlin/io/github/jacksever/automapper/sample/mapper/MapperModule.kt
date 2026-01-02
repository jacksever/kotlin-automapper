/*
 * Copyright (c) 2025 Alexander Gorodnikov
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
import io.github.jacksever.automapper.annotation.PropertyMapping
import io.github.jacksever.automapper.sample.domain.shape.Shape
import io.github.jacksever.automapper.sample.domain.status.Status
import io.github.jacksever.automapper.sample.domain.user.User
import io.github.jacksever.automapper.sample.entity.shape.ShapeEntity
import io.github.jacksever.automapper.sample.entity.status.StatusEntity
import io.github.jacksever.automapper.sample.entity.user.UserEntity
import io.github.jacksever.automapper.sample.ui.shape.UiShape
import io.github.jacksever.automapper.sample.ui.status.UiStatus
import io.github.jacksever.automapper.sample.ui.user.UiUser

/**
 * Defines all the mapping configurations for the sample module
 *
 * The `internal` visibility of this interface makes the generated extensions `internal` as well
 */
@AutoMapperModule
internal interface MapperModule {

    /**
     * The processor will generate `User.asUserEntity()` and `UserEntity.asUser()` extensions
     */
    @AutoMapper(
        mappings = [
            PropertyMapping(from = "id", to = "userId")
        ]
    )
    fun userMapper(user: User): UserEntity

    /**
     * Since `reversible` is false, only the `User.asUiUser()` extension will be generated
     */
    @AutoMapper(reversible = false)
    fun uiUserMapper(user: User): UiUser

    /**
     * The processor will generate `Shape.asShapeEntity()` and `ShapeEntity.asShape()` extensions
     */
    @AutoMapper
    fun shapeMapper(shape: Shape): ShapeEntity

    /**
     * Since `reversible` is false, only the `Shape.asUiShape()` extension will be generated
     */
    @AutoMapper(reversible = false)
    fun uiShapeMapper(shape: Shape): UiShape

    /**
     * The processor will generate `Status.asStatusEntity()` and `StatusEntity.asStatus()` extensions
     */
    @AutoMapper
    fun statusMapper(status: Status): StatusEntity

    /**
     * Since `reversible` is false, only the `Status.asUiStatus()` extension will be generated
     */
    @AutoMapper(reversible = false)
    fun uiStatusMapper(status: Status): UiStatus
}
