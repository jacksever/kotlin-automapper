plugins {
    kotlin("jvm")
    alias(libs.plugins.com.google.ksp)
}

kotlin {
    jvmToolchain(jdkVersion = 21)
}

dependencies {
    implementation(project(":automapper:annotation"))
    ksp(project(":automapper:processor"))
}
