# Kotlin AutoMapper

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE) [![Maven Central](https://img.shields.io/maven-central/v/io.github.jacksever.automapper/annotation)](https://search.maven.org/artifact/io.github.jacksever.automapper/annotation) ![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue) ![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-blue.svg?logo=kotlin) ![KSP](https://img.shields.io/badge/KSP-2.3.4-blue.svg)

Effortless, type-safe object-to-object mapping in Kotlin. Tired of writing boilerplate code to convert one object to another? This library does it for you at compile time, with full support for Kotlin Multiplatform.

Kotlin AutoMapper uses KSP (Kotlin Symbol Processing) to generate extension functions that automatically map your `data`, `enum`, and `sealed` classes. No reflection, no runtime magic - just pure, fast, and safe generated code for all your targets.
