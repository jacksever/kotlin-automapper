plugins {
    `kotlin-dsl`
}

group = "io.github.jacksever.automapper.buildlogic"

dependencies {
    implementation(libs.gradlePlugin.ksp)
    implementation(libs.gradlePlugin.kotlin)
    implementation(libs.gradlePlugin.kotlinJvm)
    implementation(libs.gradlePlugin.kotlinMultiplatform)
    implementation(libs.gradlePlugin.mavenPublish)
}
