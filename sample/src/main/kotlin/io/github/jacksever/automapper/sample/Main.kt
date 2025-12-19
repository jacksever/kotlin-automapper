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

package io.github.jacksever.automapper.sample

import io.github.jacksever.automapper.sample.domain.shape.Shape
import io.github.jacksever.automapper.sample.domain.status.Status
import io.github.jacksever.automapper.sample.domain.user.User
import io.github.jacksever.automapper.sample.mapper.asShape
import io.github.jacksever.automapper.sample.mapper.asShapeEntity
import io.github.jacksever.automapper.sample.mapper.asStatus
import io.github.jacksever.automapper.sample.mapper.asStatusEntity
import io.github.jacksever.automapper.sample.mapper.asUiShape
import io.github.jacksever.automapper.sample.mapper.asUiStatus
import io.github.jacksever.automapper.sample.mapper.asUiUser
import io.github.jacksever.automapper.sample.mapper.asUser
import io.github.jacksever.automapper.sample.mapper.asUserEntity

/**
 * Main entry point for demonstrating the Kotlin AutoMapper library
 */
fun main() {
    val labelWidth = 27

    // --- Data Class Mapping ---
    println("\n1. Data Class Mapping (User <-> UserEntity, User -> UiUser):")
    val domainUser = User(id = 1L, name = "John Doe", age = 30, status = Status.ACTIVE)
    val entityUser = domainUser.asUserEntity()
    val revertedUser = entityUser.asUser()
    val uiUser = domainUser.asUiUser()

    println("   - ${"Original Domain User:".padEnd(labelWidth)} $domainUser")
    println("   - ${"Mapped to Entity:".padEnd(labelWidth)} $entityUser")
    println("   - ${"Mapped back to Domain:".padEnd(labelWidth)} $revertedUser")
    println("   - ${"Mapped to UI Model:".padEnd(labelWidth)} $uiUser")

    // --- Enum Mapping ---
    println("\n2. Enum Mapping (Status <-> StatusEntity, Status -> UiStatus):")
    val domainStatus = Status.PENDING
    val entityStatus = domainStatus.asStatusEntity()
    val revertedStatus = entityStatus.asStatus()
    val uiStatus = domainStatus.asUiStatus()

    println("   - ${"Original Domain Status:".padEnd(labelWidth)} $domainStatus")
    println("   - ${"Mapped to Entity:".padEnd(labelWidth)} $entityStatus")
    println("   - ${"Mapped back to Domain:".padEnd(labelWidth)} $revertedStatus")
    println("   - ${"Mapped to UI Model:".padEnd(labelWidth)} $uiStatus")

    // --- Sealed Class Mapping ---
    println("\n3. Sealed Class Mapping (Shape <-> ShapeEntity, Shape -> UiShape):")
    val domainShape = Shape.Circle(radius = 10.5)
    val entityShape = domainShape.asShapeEntity()
    val revertedShape = entityShape.asShape()
    val uiShape = domainShape.asUiShape()

    println("   - ${"Original Domain Shape:".padEnd(labelWidth)} $domainShape")
    println("   - ${"Mapped to Entity:".padEnd(labelWidth)} $entityShape")
    println("   - ${"Mapped back to Domain:".padEnd(labelWidth)} $revertedShape")
    println("   - ${"Mapped to UI Model:".padEnd(labelWidth)} $uiShape")
}
