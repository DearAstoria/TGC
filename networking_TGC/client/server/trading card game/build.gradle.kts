plugins {
   kotlin("jvm") version "1.9.22"
   application
   id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}
repositories {
   mavenCentral()
}
dependencies {
   // Ktor server core
   implementation("io.ktor:ktor-server-core-jvm:2.3.7")
   implementation("io.ktor:ktor-server-netty-jvm:2.3.7")
   // WebSockets
   implementation("io.ktor:ktor-server-websockets-jvm:2.3.7")
   // Serialization (JSON)
   implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.7")
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
   // Logging
   implementation("ch.qos.logback:logback-classic:1.4.14")
   // Coroutines
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
   // Testing
   testImplementation(kotlin("test"))
}
application {
   mainClass.set("server.GameServerKt")
}

android {
   namespace = "com.yourgame.tcg"
   compileSdk = 34
   defaultConfig {
       minSdk = 21
       targetSdk = 34
   }
   buildFeatures {
       compose = true
   }
   composeOptions {
       kotlinCompilerExtensionVersion = "1.5.8"
   }
}