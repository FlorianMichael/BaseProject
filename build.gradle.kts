plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    signing
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.3.1"
}

group = property("maven_group") as String
version = property("maven_version") as String
description = property("maven_description") as String

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    compileOnly("net.fabricmc:fabric-loom:1.10-SNAPSHOT")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
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
