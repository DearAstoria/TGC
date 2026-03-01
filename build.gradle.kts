// AdamTCGARFINAL/build.gradle.kts
// Root build file — almost empty
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}