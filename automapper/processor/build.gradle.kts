plugins {
    id("kotlin.automapper")
    id("kotlin.automapper.jvm")
    id("kotlin.automapper.publish")
    id("kotlin.automapper.validation")
}

automapper {
    artifact {
        id = "processor"
        name = "Kotlin AutoMapper Processor"
        description =
            "The KSP processor for Kotlin AutoMapper, which generates mapping extension functions"
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
