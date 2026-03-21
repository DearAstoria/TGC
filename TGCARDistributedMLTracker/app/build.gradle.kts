plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.tgcardistributedmltracker"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.tgcardistributedmltracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            // If the merger is fighting over files inside the META-INF or manifests
            pickFirsts.add("**/AndroidManifest.xml")
            excludes.add("META-INF/DEPENDENCIES")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.room.external.antlr)
    implementation(libs.identity.doctypes.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
// Standardize on LiteRT (The new TFLite)
    implementation("com.google.ai.edge.litert:litert:2.1.3")
    implementation("com.google.ai.edge.litert:litert-support-api:1.4.2"){
        exclude(group = "com.google.ai.edge.litert", module = "litert-api")
    }
    // Used for ML & AR (ARCore)
    implementation(libs.google.ar.core)
    // SceneView 3D & AR Rendering
    implementation(libs.sceneview.ar){
        exclude(group = "com.google.ai.edge.litert", module = "litert-support-api")
        exclude(group = "org.tensorflow", module = "tensorflow-lite-support")
    }
    // In order to send moves to the server
    implementation("io.socket:socket.io-client:2.1.0")
}

configurations.all {
    resolutionStrategy {
        // This forces Gradle to pick ONE version and ignore the duplicate API package
        force("com.google.ai.edge.litert:litert-support:1.4.2")

        // This explicitly tells Gradle: "If you see the old TFLite, replace it with LiteRT"
        dependencySubstitution {
            substitute(module("org.tensorflow:tensorflow-lite-support"))
                .using(module("com.google.ai.edge.litert:litert-support:1.4.2"))
        }
    }
}