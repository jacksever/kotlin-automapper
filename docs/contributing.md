# Contributing to Kotlin AutoMapper

First off, thank you for considering contributing to Kotlin AutoMapper! It's people like you that make such a project great.

## Where to Start?

If you've noticed a bug or have a feature request, please [open a new issue](https://github.com/jacksever/kotlin-automapper/issues/new/choose). It's generally best to discuss your proposal before you start writing code.

## Making Changes

1.  Fork the repository and create your branch from `main`.
2.  Make your changes.
3.  Ensure your code follows the project's coding style.
4.  Submit a pull request to the `main` branch.

## Running Tests

Run all project checks with `./gradlew check`.

- `./gradlew :sample:jvmTest` checks the behavior of generated mappers.
- `./gradlew :automapper:processor:test` checks processor diagnostics by running KSP over isolated invalid sources in the test JVM. The KSP runtime uses the same version as the processor API.

Add invalid examples to `ProcessorDiagnosticTest` and assert the expected processing failure and diagnostic message. Source examples are written to a temporary directory and do not become part of the test suite's own compilation. Keep checks of generated mapper behavior in `sample/src/jvmTest`.

## Code of Conduct

This project and everyone participating in it is governed by the [Code of Conduct](https://jacksever.github.io/kotlin-automapper/code_of_conduct). By participating, you are expected to uphold this code.

We look forward to your contributions!
