/*
 * This file is part of BaseProject - https://github.com/FlorianMichael/BaseProject
 * Copyright (C) 2024-2026 FlorianMichael/EnZaXD <git@florianmichael.de> and contributors
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
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

fun Project.latestCommitHash(): String {
    return runGitCommand(listOf("rev-parse", "--short", "HEAD"))
}

fun Project.latestCommitMessage(): String {
    return runGitCommand(listOf("log", "-1", "--pretty=%B"))
}

fun Project.branchName(): String {
    return runGitCommand(listOf("rev-parse", "--abbrev-ref", "HEAD"))
}

fun Project.runGitCommand(args: List<String>): String {
    return providers.of(GitCommand::class.java) { parameters.args.set(args) }.getOrNull() ?: "unknown"
}

abstract class GitCommand : ValueSource<String, GitCommand.GitCommandParameters> {

    @get:Inject
    abstract val execOperations: ExecOperations

    interface GitCommandParameters : ValueSourceParameters {
        val args: ListProperty<String>
    }

    override fun obtain(): String? {
        try {
            val command = listOf("git") + parameters.args.get()
            val output = ByteArrayOutputStream()
            execOperations.exec {
                commandLine = command
                standardOutput = output
                isIgnoreExitValue = true
            }

            return output.toString(Charsets.UTF_8).trim().takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            return null
        }
    }
}
