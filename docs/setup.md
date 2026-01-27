Ensure you have the `ksp` plugin applied in your module's `build.gradle.kts` file.

### For a Kotlin Multiplatform Project

 ```kotlin
 kotlin {
     sourceSets {
         commonMain.dependencies {
             // 1. Add the annotation dependency to commonMain
             implementation("io.github.jacksever.automapper:annotation:0.8.0")
         }
     }
 }

 // 2. Apply the processor to the targets you need
 dependencies {
     add("kspJs", "io.github.jacksever.automapper:processor:0.8.0")
     add("kspJvm", "io.github.jacksever.automapper:processor:0.8.0")
     add("kspIosX64", "io.github.jacksever.automapper:processor:0.8.0")
     // etc. for your other targets
 }
 ```

### For an Android-Only (or JVM) Project

In a standard Android or JVM module, you can add the dependencies directly.

 ```kotlin
 dependencies {
     // Annotation dependency
     implementation("io.github.jacksever.automapper:annotation:0.8.0")

     // KSP processor
     ksp("io.github.jacksever.automapper:processor:0.8.0")
 }
 ```
