pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "BaseProject"

// Add if your project requires a git repository (e.g. for including git commit hashes in the jar)
//if (!file(".git").exists()) {
//    val errorText = """
//
//        =====================[ ERROR ]=====================
//         The BaseProject project directory is not a properly cloned Git repository.
//
//         In order to build BaseProject from source you must clone
//         the BaseProject repository using Git, not download a code
//         zip from GitHub.
//
//         Built BaseProject jars are available for download at
//         https://github.com/FlorianMichael/BaseProject/releases
//        ===================================================
//    """.trimIndent()
//    error(errorText)
//}
