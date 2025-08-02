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
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
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
 * Optional project property:
 * - `parchment_version`: Version of Parchment mappings to use.
 *
 * @param parchment Optional override for the Parchment version.
 */
fun mojangMapped(parchment: String? = null): MappingsConfigurer = {
    val parchmentVersion: String? = parchment ?: findProperty("parchment_version") as? String
    val loom = extensions.getByType(LoomGradleExtensionAPI::class.java)
    dependencies {
        if (parchmentVersion == null) {
            "mappings"(loom.officialMojangMappings())
        } else {
            "mappings"(loom.layered {
                officialMojangMappings()
                parchment("org.parchmentmc.data:parchment-$parchmentVersion@zip")
            })
        }
    }
}

/**
 * Sets up Fabric Loom with Minecraft dependencies, mappings, Kotlin support, and mod metadata processing.
 *
 * Required project properties:
 * - `minecraft_version`: Minecraft version to target
 * - `fabric_loader_version`: Fabric loader version
 *
 * Optional project properties:
 * - `fabric_kotlin_version`: Fabric Kotlin language module version (used if Kotlin plugin is applied)
 * - `supported_minecraft_versions`: Used in mod metadata if provided
 *
 * @param mappings The mappings configuration to apply (Yarn or Mojang+Parchment)
 */
fun Project.setupFabric(mappings: MappingsConfigurer = mojangMapped()) {
    plugins.apply("fabric-loom")
    val accessWidenerFile = file("src/main/resources/${project.name.lowercase()}.accesswidener")
    if (accessWidenerFile.exists()) {
        extensions.getByType(LoomGradleExtensionAPI::class.java).apply {
            accessWidenerPath.set(accessWidenerFile)
        }
    }
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.parchmentmc.org/")
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
        val projectName = project.name
        val projectVersion = project.version
        val projectDescription = project.description
        val mcVersion = if (!project.hasProperty("supported_minecraft_versions")) {
            project.property("minecraft_version") as String
        } else {
            val supportedVersions = project.property("supported_minecraft_versions") as String
            supportedVersions.ifEmpty {
                project.property("minecraft_version") as String
            }
        }
        val latestCommitHash = latestCommitHash()
        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "version" to projectVersion,
                "implVersion" to "git-${projectName}-${projectVersion}:${latestCommitHash}",
                "description" to projectDescription,
                "mcVersion" to mcVersion
            ))
        }
    }

    excludeRunFolder()
}

/**
 * Creates or retrieves the `jij` configuration and sets it to be extended by:
 * - `implementation`
 * - `include`
 *
 * This setup is commonly used for Java-in-Jar (JiJ) dependencies in standard Java projects.
 * Dependencies added to `jij` will be treated as compile/runtime dependencies and will be bundled into the final jar.
 *
 * @return The created or existing `jij` configuration.
 */
fun Project.configureJij(): Configuration {
    val jijConfig = configurations.maybeCreate("jij")

    configurations.getByName("implementation").extendsFrom(jijConfig)
    configurations.getByName("include").extendsFrom(jijConfig)

    return jijConfig
}

/**
 * Creates or retrieves the `modJij` configuration and sets it to be extended by:
 * - `modImplementation`
 * - `modCompileOnlyApi`
 * - `include`
 *
 * This setup is intended for Fabric or mod-based projects using Java-in-Jar (JiJ) dependencies.
 * It ensures the dependencies are available at compile-time, runtime, and are bundled into the final mod jar.
 *
 * @return The created or existing `modJij` configuration.
 */
fun Project.configureModJij(): Configuration {
    val jijConfig = configurations.maybeCreate("modJij")

    configurations.getByName("modImplementation").extendsFrom(jijConfig)
    configurations.getByName("modCompileOnlyApi").extendsFrom(jijConfig)
    configurations.getByName("include").extendsFrom(jijConfig)

    return jijConfig
}

/**
 * Adds a submodule which is a Fabric mod to the project.
 *
 * @param name The name of the submodule
 */
fun Project.includeFabricSubmodule(name: String) {
    dependencies {
        project(mapOf("path" to ":$name", "configuration" to "namedElements")).apply {
            "implementation"(this)
            "compileOnlyApi"(this)
        }
        "include"(project(":$name"))
    }
}

/**
 * Add support to the jar in jar system from Fabric to support transitive dependencies by manually proxying them into the jar.
 */
fun Project.includeTransitiveJijDependencies() {
    afterEvaluate {
        val jijConfig = configurations.findByName("jij") ?: return@afterEvaluate

        jijConfig.incoming.resolutionResult.allDependencies.forEach { dep ->
            val requested = dep.requested.displayName

            val compileOnlyDep = dependencies.create(requested) {
                isTransitive = false
            }

            val implDep = dependencies.create(compileOnlyDep)

            dependencies.add("compileOnlyApi", compileOnlyDep)
            dependencies.add("implementation", implDep)
            dependencies.add("include", compileOnlyDep)
        }
    }
}

/**
 * Adds core Fabric API modules to the project and directly shades them into the jar using the `jij` configuration.
 *
 * See [configureFabricApiModules] for details on the modules added.
 *
 * @param modules The Fabric API modules to include.
 */
fun Project.includeFabricApiModules(vararg modules: String) {
    configureModJij()
    configureFabricApiModules("modJij", *modules)
}

/**
 * Adds core Fabric API modules to the project.
 *
 * See [configureFabricApiModules] for details on the modules added.
 *
 * @param modules The Fabric API modules to include.
 */
fun Project.loadFabricApiModules(vararg modules: String) {
    configureFabricApiModules("modImplementation", *modules)
}

/**
 * Adds Fabric API modules to the project.
 *
 * Requires that the `fabric-loom` plugin is applied.
 * @param configuration The configuration to add the modules to. Defaults to `modImplementation`.
 * @param modules The Fabric API modules to include.
 * @param version The version of the Fabric API to use. Defaults to the value of `fabric_api_version` property.
 */
fun Project.configureFabricApiModules(
    configuration: String,
    vararg modules: String,
    version: String = property("fabric_api_version") as String
) {
    pluginManager.withPlugin("fabric-loom") {
        val fabricApi = extensions.getByType(FabricApiExtension::class.java)
        dependencies {
            configuration(fabricApi.module("fabric-api-base", version))
            modules.forEach {
                configuration(fabricApi.module(it, version))
            }
        }
    }
}
