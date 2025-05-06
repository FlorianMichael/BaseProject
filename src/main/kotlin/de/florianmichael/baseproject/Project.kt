/*
 * This file is part of BaseProject - https://github.com/FlorianMichael/BaseProject
 * Copyright (C) 2024-2025 FlorianMichael/EnZaXD <florian.michael07@gmail.com> and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.florianmichael.baseproject

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

/**
 * Sets up the Gradle project with common configurations including:
 * - Core project metadata (group, version, description, archive name)
 * - Java toolchain setup
 * - License file renaming to avoid name conflicts
 * - Compiler encoding configuration
 *
 * Supports the following Gradle properties:
 * - `project_group`: The group ID of the project
 * - `project_version`: The version of the project
 * - `project_description`: A short description of the project
 * - `project_name`: The archive base name for outputs
 * - `project_jvm_version`: The Java version to compile against
 */
fun Project.setupProject() {
    configureProjectDetails()
    setupJava()
    renameLicenseFile()
    configureEncoding()
}

/**
 * Configures detailed project metadata:
 * - Applies the `base` plugin
 * - Sets project description
 * - Configures the archive name
 *
 * Optional project properties:
 * - `project_group`
 * - `project_version`
 * - `project_description`
 * - `project_name`
 */
fun Project.configureProjectDetails() {
    findProperty("project_group")?.let { group = it as String }
    findProperty("project_version")?.let { version = it as String }
    findProperty("project_description")?.let { description = it as String }
    apply(plugin = "base")
    extensions.getByType(BasePluginExtension::class.java).apply {
        findProperty("project_name")?.let { archivesName.set(it as String) }
    }
}

/**
 * Ensures UTF-8 encoding for all Java source compilation tasks.
 */
fun Project.configureEncoding() {
    tasks.withType(JavaCompile::class.java).configureEach {
        options.encoding = "UTF-8"
    }
}

/**
 * Applies the `java-library` plugin and configures the Java toolchain and compatibility settings.
 *
 * Required project property:
 * - `project_jvm_version`: Must be an integer (e.g., 17)
 *
 * @param version Optional override for the JVM version. Defaults to `project_jvm_version`.
 */
fun Project.setupJava(version: Int = project.property("project_jvm_version").toString().toInt()) {
    apply(plugin = "java-library")
    extensions.getByType(JavaPluginExtension::class.java).apply {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(version))
        }
        sourceCompatibility = JavaVersion.toVersion(version)
        targetCompatibility = JavaVersion.toVersion(version)
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        extensions.getByType(KotlinJvmExtension::class.java).apply {
            jvmToolchain(version)
        }
    }
}

/**
 * Renames the `LICENSE` file to `LICENSE_<project_name>` in the final JAR
 * to avoid naming conflicts in multi-module projects.
 */
fun Project.renameLicenseFile() {
    tasks.named("jar", Jar::class.java).configure {
        val projectName = project.name

        // Rename the project's license file to LICENSE_<project_name> to avoid conflicts
        from("LICENSE") {
            rename { "LICENSE_$projectName" }
        }
    }
}
