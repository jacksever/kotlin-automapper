# Kotlin AutoMapper

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE) [![Maven Central](https://img.shields.io/maven-central/v/io.github.jacksever.automapper/annotation)](https://search.maven.org/artifact/io.github.jacksever.automapper/annotation) ![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue) ![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-blue.svg?logo=kotlin) ![KSP](https://img.shields.io/badge/KSP-2.3.5-blue.svg)

Effortless, type-safe object-to-object mapping in Kotlin. Tired of writing boilerplate code to convert one object to another? This library does it for you at compile time, with full support for Kotlin Multiplatform.

Kotlin AutoMapper uses KSP (Kotlin Symbol Processing) to generate extension functions that automatically map your `data`, `enum`, and `sealed` classes. No reflection, no runtime magic - just pure, fast, and safe generated code for all your targets.

**➡️ [View the full documentation for Setup, How to Use, and Advanced Usage](https://jacksever.github.io/kotlin-automapper/) ⬅️**

## Features

-   **Kotlin Multiplatform First:** Works seamlessly across a wide range of targets:
    -   **Android** & **JVM/Desktop**
    -   **JavaScript** (Browser & Node.js)
    -   **Wasm** (JS & Wasi)
    -   **iOS**, **macOS**, **watchOS**, **tvOS**
    -   **Linux**
-   **Type-Safe & Compile-Time Verified:** All mappings are generated and validated by KSP at compile time. No reflection, no runtime errors.
-   **Zero Runtime Dependencies:** The annotation library is `SOURCE`-only and adds no overhead to your production app.
-   **Broad Class Support:** Out-of-the-box support for `data`, `enum`, and `sealed` classes, including complex nested hierarchies.
-   **Powerful Mapping Customization:**
    -   **Property Renaming:** Easily map properties with different names using `@PropertyMapping`.
    -   **Default Values:** Provide fallback values for missing or `null` properties with `@DefaultValue`.
-   **Advanced Type Conversion:**
    -   **Custom Converters:** Define your own conversion logic for complex types (like `Uuid` or `Instant`) using `@AutoConverter`.
    -   **Automatic Primitive Conversion:** Handles conversions between primitives like `Int`, `Long`, `String` automatically.
-   **Smart & Safe Generation:**
    -   **Bidirectional Mapping:** Generate forward and reverse mappings with a single `reversible = true` flag.
    -   **Visibility Control:** Generated code automatically inherits the visibility (`public` or `internal`) of your `@AutoMapperModule`.
    -   **`@OptIn` Propagation:** Automatically propagates `@OptIn` annotations from your custom converters to the generated functions, ensuring compile-time safety for experimental APIs.

## License

    Copyright (c) 2026 Alexander Gorodnikov
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
