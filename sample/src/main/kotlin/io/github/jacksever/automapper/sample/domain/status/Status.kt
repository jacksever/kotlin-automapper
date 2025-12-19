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

package io.github.jacksever.automapper.sample.domain.status

/**
 * Represents user status in the domain layer
 */
enum class Status {

    /**
     * Represents an active user status
     */
    ACTIVE,

    /**
     * Represents a pending user status
     */
    PENDING,

    /**
     * Represents an inactive user status
     */
    INACTIVE,
    ;
}
