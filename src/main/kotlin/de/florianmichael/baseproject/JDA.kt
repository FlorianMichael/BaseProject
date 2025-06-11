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
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories

/**
 * Sets up the JDA (Java Discord API) dependency for the project.
 *
 * Required project properties:
 * - `jda_version`: The version of JDA to use.
 *
 * Optional project properties:
 * - `jda_ktx_version`: Version of JDA KTX extension.
 * - `lavaplayer_version`: Version of Lavaplayer extension.
 * - `udp_queue_version`: Version of UDP Queue extension.
 *
 * @param extensions Optional JDA extensions to include in the project.
 */
fun Project.setupJDA(vararg extensions: JDAExtension = emptyArray()) {
    repositories {
        mavenCentral()
        maven("https://m2.dv8tion.net/releases")

        if (extensions.isNotEmpty()) {
            maven("https://jitpack.io")
        }
    }

    dependencies {
        "implementation"("net.dv8tion:JDA:${property("jda_version")}")

        extensions.forEach { extension ->
            when (extension) {
                JDAExtension.KTX -> "implementation"("club.minnced:jda-ktx:${property("jda_ktx_version") ?: "0.12.0"}")
                JDAExtension.LAVAPLAYER -> "implementation"("dev.arbjerg:lavaplayer:${property("lavaplayer_version") ?: "2.2.3"}")
                JDAExtension.UDP_QUEUE -> "implementation"("club.minnced:udpqueue-api:${property("udp_queue_version") ?: "0.2.9"}")
            }
        }
    }
}

/**
 * Represents the different JDA extensions that can be used in the project.
 */
enum class JDAExtension {
    KTX,
    LAVAPLAYER,
    UDP_QUEUE,
}
