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

package io.github.jacksever.automapper.sample.domain.shape

/**
 * Represents a geometric shape in the domain layer
 */
sealed interface Shape {

    /**
     * An object representing the absence of a shape
     */
    data object NoShape : Shape

    /**
     * A square with a given [side] length
     */
    data class Square(val side: Double) : Shape

    /**
     * A rectangle with a given [width] and [height]
     */
    data class Rectangle(val width: Double, val height: Double) : Shape

    /**
     * A nested sealed interface representing shapes with rounded corners or curves
     */
    sealed interface Rounded : Shape {

        /**
         * A circle with a given [radius]
         */
        data class Circle(val radius: Double) : Rounded

        /**
         * An ellipse with a given [majorAxis] and [minorAxis]
         */
        data class Ellipse(val majorAxis: Double, val minorAxis: Double) : Rounded
    }
}
