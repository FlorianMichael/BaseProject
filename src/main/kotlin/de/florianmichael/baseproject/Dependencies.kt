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

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar
import java.util.concurrent.TimeUnit

/**
 * Configures caching strategies for dynamic and changing modules to improve build performance.
 *
 * Caches:
 * - Dynamic versions: 5 minutes
 * - Changing modules: 5 minutes
 */
fun Project.cacheDynamicAndChangingModules() {
    configurations.all {
        resolutionStrategy.apply {
            cacheDynamicVersionsFor(5, TimeUnit.MINUTES)
            cacheChangingModulesFor(5, TimeUnit.MINUTES)
        }
    }
}

/**
 * Configures a custom `shadedDependencies` configuration used to embed shaded dependencies in the JAR.
 * Excludes certain metadata files and avoids duplicate entries.
 *
 * @return The created `shadedDependencies` configuration.
 */
fun Project.configureShadedDependencies(): Configuration {
    val shadedDependencies = configurations.create("shadedDependencies").apply {
        isCanBeResolved = true
        isCanBeConsumed = true
        configurations.findByName("implementation")?.extendsFrom(this)
    }

    tasks.named("jar", Jar::class.java).configure {
        dependsOn(shadedDependencies)

        from({
            shadedDependencies.map { zipTree(it) }
        }) {
            exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    return shadedDependencies
}
