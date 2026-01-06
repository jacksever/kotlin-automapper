import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.com.google.ksp)
}

kotlin {
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js(IR) {
        nodejs()
        browser()
        binaries.executable()
    }

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

    sourceSets {
        commonMain.dependencies {
            implementation(project(":automapper:annotation"))
        }

        jvmTest.dependencies {
            implementation(libs.junit)
            implementation(kotlin("test"))
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":automapper:processor"))
    add("kspJvm", project(":automapper:processor"))
    add("kspJvmTest", project(":automapper:processor"))
}
