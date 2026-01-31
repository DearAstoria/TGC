plugins {
    kotlin("jvm")
    application
    kotlin("plugin.serialization") version "2.0.0"
}

application {
    mainClass.set("server.MainKt")
}

dependencies {
    implementation(project(":shared"))
    implementation("io.ktor:ktor-server-core-jvm:3.0.0")
    implementation("io.ktor:ktor-server-netty-jvm:3.0.0")
    implementation("io.ktor:ktor-server-websockets-jvm:3.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.0.0")
    implementation("ch.qos.logback:logback-classic:1.5.6")
}