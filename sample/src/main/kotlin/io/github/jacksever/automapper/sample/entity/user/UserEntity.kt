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

package io.github.jacksever.automapper.sample.entity.user

import io.github.jacksever.automapper.sample.entity.status.StatusEntity

/**
 * Represents a User in the data (e.g., database) layer
 *
 * @property id unique identifier of the user
 * @property name name of the user
 * @property age age of the user
 * @property status status of the user
 */
data class UserEntity(
    val id: Long,
    val name: String,
    val age: Int,
    val status: StatusEntity,
)
