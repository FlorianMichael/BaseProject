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

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.fabricapi.FabricApiExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.expand
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.repositories
import org.gradle.language.jvm.tasks.ProcessResources

typealias MappingsConfigurer = Project.() -> Unit

/**
 * Returns a [MappingsConfigurer] that configures Yarn mappings for the project.
 *
 * Required project property:
 * - `yarn_mappings_version`: The version of Yarn mappings to use.
 *
 * @param version Optional override for the Yarn version.
 */
fun yarnMapped(version: String? = null): MappingsConfigurer = {
    val yarnVersion = version ?: property("yarn_mappings_version") as String
    dependencies {
        "mappings"("net.fabricmc:yarn:$yarnVersion:v2")
    }
}

/**
 * Returns a [MappingsConfigurer] that configures Mojang + Parchment layered mappings.
 *
 * Required project property:
 * - `parchment_version`: Version of Parchment mappings to use.
 */
fun mojangMapped(parchment: String? = null): MappingsConfigurer = {
    val parchmentVersion = parchment ?: property("parchment_version") as String
    val loom = extensions.getByType(LoomGradleExtensionAPI::class.java)
    dependencies {
        "mappings"(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-$parchmentVersion@zip")
        })
    }
}

/**
 * Sets up Fabric Loom with Minecraft dependencies, mappings, Kotlin support, and mod metadata processing.
 *
 * Required project properties:
 * - `minecraft_version`: Minecraft version to target
 * - `fabric_loader_version`: Fabric loader version
 * - `fabric_kotlin_version`: Fabric Kotlin language module version (used if Kotlin plugin is applied)
 *
 * Optional project properties:
 * - `supported_minecraft_versions`: Used in mod metadata if provided
 *
 * @param mappings The mappings configuration to apply (Yarn or Mojang+Parchment)
 * @param accessWidener Whether to enable access widener support
 */
fun Project.setupFabric(mappings: MappingsConfigurer = mojangMapped(), accessWidener: Boolean = false) {
    plugins.apply("fabric-loom")
    if (accessWidener) {
        extensions.getByType(LoomGradleExtensionAPI::class.java).apply {
            accessWidenerPath.set(file("src/main/resources/${project.name.lowercase()}.accesswidener"))
        }
    }
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.parchmentmc.org")
    }
    dependencies {
        "minecraft"("com.mojang:minecraft:${property("minecraft_version")}")
        "modImplementation"("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        dependencies {
            "modImplementation"("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
        }
    }
    mappings()
    tasks.named<ProcessResources>("processResources").configure {
        val projectVersion = project.version
        val projectDescription = project.description
        val mcVersion = mcVersion()
        filesMatching("fabric.mod.json") {
            expand(
                "version" to projectVersion,
                "description" to projectDescription,
                "mcVersion" to mcVersion,
            )
        }
    }

    excludeRunFolder()
}

/**
 * Resolves the target Minecraft version from `supported_minecraft_versions`, falling back to `minecraft_version` if unset.
 *
 * Required project property:
 * - `minecraft_version`
 *
 * Optional project property:
 * - `supported_minecraft_versions`
 *
 * @return The Minecraft version string to use for metadata.
 */
fun Project.mcVersion(): String {
    val supportedVersions = property("supported_minecraft_versions") as String
    return supportedVersions.ifEmpty {
        property("minecraft_version") as String
    }
}

/**
 * Adds core Fabric API modules to the project.
 *
 * Required project property:
 * - `fabric_api_version`: The version of the Fabric API to use.
 *
 * Modules added:
 * - `fabric-api-base`
 * - `fabric-resource-loader-v0`
 * - `fabric-screen-api-v1`
 * - `fabric-key-binding-api-v1`
 * - `fabric-lifecycle-events-v1`
 *
 * Requires that the `fabric-loom` plugin is applied.
 */
fun Project.coreFabricApiModules(version: String = property("fabric_api_version") as String) {
    pluginManager.withPlugin("fabric-loom") {
        val fabricApi = extensions.getByType(FabricApiExtension::class.java)
        dependencies {
            "modImplementation"(fabricApi.module("fabric-api-base", version))
            "modImplementation"(fabricApi.module("fabric-resource-loader-v0", version))
            "modImplementation"(fabricApi.module("fabric-screen-api-v1", version))
            "modImplementation"(fabricApi.module("fabric-key-binding-api-v1", version))
            "modImplementation"(fabricApi.module("fabric-lifecycle-events-v1", version))
        }
    }
}
