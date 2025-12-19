plugins {
    kotlin("jvm")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

dependencies {
    /**
     * Automapper internal dependencies
     */
    // region Automapper internal dependencies
    implementation(project(":automapper:annotation"))
    // endregion

    /**
     * Kotlinpoet dependencies
     */
    // region Kotlinpoet dependencies
    implementation(libs.com.squareup.kotlinpoet)
    implementation(libs.com.squareup.kotlinpoet.ksp)
    // endregion

    /**
     * Google KSP dependencies
     */
    // region Google KSP dependencies
    implementation(libs.google.devtools.ksp.processor.api)
    // endregion
}
