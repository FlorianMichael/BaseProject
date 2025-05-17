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
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

/**
 * Configures all test tasks to use JUnit Platform and enable logging for passed, skipped, and failed tests.
 * Also sets parallel forks based on available processor cores.
 *
 * @param condition If true, enables the test tasks. Defaults to true.
 */
fun Project.configureTestTasks(condition: Boolean = true) {
    tasks.withType<Test>().configureEach {
        enabled = condition
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        if (file("run").exists()) {
            workingDir = file("run")
        }
        maxParallelForks = Runtime.getRuntime().availableProcessors()
    }
}
