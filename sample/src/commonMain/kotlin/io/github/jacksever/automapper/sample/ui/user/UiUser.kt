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

package io.github.jacksever.automapper.sample.ui.user

import kotlin.time.Instant

/**
 * Represents a User in the UI layer
 *
 * @property name name of the user
 * @property age age of the user
 * @property address address of the user
 * @property createdAt instant when the user was created
 */
data class UiUser(
    val name: String,
    val age: Int,
    val address: String,
    val createdAt: Instant,
)
