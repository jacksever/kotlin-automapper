# Kotlin AutoMapper

[![Maven Central](https://img.shields.io/maven-central/v/io.github.jacksever.automapper/annotation)](https://search.maven.org/artifact/io.github.jacksever.automapper/annotation) ![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue) ![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-blue.svg?logo=kotlin) ![KSP](https://img.shields.io/badge/KSP-2.3.6-blue.svg)

Effortless, type-safe object-to-object mapping in Kotlin. Tired of writing boilerplate code to convert one object to another? This library does it for you at compile time, with full support for Kotlin Multiplatform.

Kotlin AutoMapper uses KSP (Kotlin Symbol Processing) to generate extension functions that automatically map your `data`, `enum`, and `sealed` classes. No reflection, no runtime magic - just pure, fast, and safe generated code for all your targets.

## Supported Platforms

Kotlin AutoMapper is built with Kotlin Multiplatform and supports a wide range of targets out-of-the-box:

*   :fontawesome-brands-android: **Android**
*   :fontawesome-brands-java: **JVM/Desktop**
*   :fontawesome-brands-js: **JavaScript** (Browser & Node.js)
*   :material-web: **Wasm** (JS & Wasi)
*   :fontawesome-brands-apple: **iOS**, **macOS**, **watchOS**, **tvOS** (Device & Simulator)
*   :fontawesome-brands-linux: **Linux**

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
