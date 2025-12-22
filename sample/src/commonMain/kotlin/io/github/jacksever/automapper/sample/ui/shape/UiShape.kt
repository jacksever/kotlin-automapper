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

package io.github.jacksever.automapper.sample.ui.shape

/**
 * Represents a geometric shape in the UI layer
 */
sealed interface UiShape {

    /**
     *  An object representing the absence of a shape
     */
    data object NoShape : UiShape

    /**
     * A square with a given [side] length
     */
    data class Square(val side: Double) : UiShape

    /**
     * A circle with a given [radius]
     */
    data class Circle(val radius: Double) : UiShape
}
