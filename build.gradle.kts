plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    signing
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.0.0"
}

group = property("project_group") as String
version = property("project_version") as String
description = property("project_description") as String

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
    compileOnly("net.fabricmc:fabric-loom:1.10-SNAPSHOT")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    jar {
        val projectName = project.name
        from("LICENSE") {
            rename { "LICENSE_${projectName}" }
        }
    }

    publishPlugins {
        notCompatibleWithConfigurationCache("plugin-publish plugin is not compatible with the configuration cache yet.")
    }
}

gradlePlugin {
    website = "https://github.com/FlorianMichael/BaseProject"
    vcsUrl = "https://github.com/FlorianMichael/BaseProject.git"
    plugins {
        create("baseProjectPlugin") {
            id = "${project.group}.${project.name}"
            implementationClass = "de.florianmichael.baseproject.BaseProjectPlugin"
            displayName = "BaseProject Convention Plugin"
            description = project.description
            tags = listOf("fabric", "convention")
        }
    }
}

signing {
    isRequired = false
    sign(publishing.publications)
}
