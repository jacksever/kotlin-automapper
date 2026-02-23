plugins {
    id("kotlin.automapper.ksp")
    id("kotlin.automapper.multiplatform")
}

kotlin {
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
