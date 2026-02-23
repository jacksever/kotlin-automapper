plugins {
    id("kotlin.automapper")
    id("kotlin.automapper.publish")
    id("kotlin.automapper.validation")
    id("kotlin.automapper.multiplatform")
}

automapper {
    artifact {
        id = "annotation"
        name = "Kotlin AutoMapper Annotation"
        description =
            "Annotations for the Kotlin AutoMapper KSP library, used to define mapping modules and functions"
    }
}
