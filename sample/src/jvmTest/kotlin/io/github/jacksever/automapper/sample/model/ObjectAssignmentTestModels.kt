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

package io.github.jacksever.automapper.sample.model

data class Child(val id: Int)

data class ObjectSource(val child: Child)

data class NullableObjectSource(val child: Child?)

data class ObjectTarget(val child: Child)

data class NullableObjectTarget(val child: Child?)

data class ObjectFallbackTarget(val child: Child)

interface Animal

data class Dog(val id: Int) : Animal

data class DogSource(val animal: Dog)

data class NullableDogSource(val animal: Dog?)

data class AnimalTarget(val animal: Animal)

data class NullableAnimalTarget(val animal: Animal?)

data class ConvertedAnimalTarget(val animal: Animal)
