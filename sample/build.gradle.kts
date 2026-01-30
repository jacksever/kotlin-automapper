import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.com.google.ksp)
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

    sourceSets {
        commonMain.dependencies {
            implementation(project(":automapper:annotation"))
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit5"))
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":automapper:processor"))
    add("kspJvm", project(":automapper:processor"))
    add("kspJvmTest", project(":automapper:processor"))
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }
