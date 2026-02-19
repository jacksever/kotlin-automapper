import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    id("kotlin.automapper")
    id("kotlin.automapper.publish")
}

kotlin {
    // JVM/Android targets
    jvm()

    // JS targets
    js(IR) {
        nodejs()
        browser()
        binaries.executable()
    }

    // Wasm targets
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
        browser()
        binaries.executable()
    }

    // iOS targets
    iosArm64()
    iosSimulatorArm64()

    // macOS targets
    macosArm64()

    // watchOS targets
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()

    // tvOS targets
    tvosArm64()
    tvosSimulatorArm64()

    // Linux targets
    linuxX64()
    linuxArm64()
}

automapper {
    artifact {
        id = "annotation"
        name = "Kotlin AutoMapper Annotation"
        description =
            "Annotations for the Kotlin AutoMapper KSP library, used to define mapping modules and functions"
    }
}
