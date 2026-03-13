plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.0"
}

dependencies {
    implementation(project(":shared"))
    implementation("io.ktor:ktor-client-core-jvm:3.0.0")
    implementation("io.ktor:ktor-client-cio-jvm:3.0.0")
    implementation("io.ktor:ktor-client-websockets-jvm:3.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.0.0")
}
